package io.github.mipmip.beansondroid

import android.util.Base64
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.PacketLineOut
import org.eclipse.jgit.transport.RefAdvertiser
import org.eclipse.jgit.transport.UploadPack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

/**
 * Serves a git repository over the smart HTTP protocol on loopback, so the
 * end-to-end tests clone the way a real repository is cloned, shallow fetch
 * included. Optionally requires HTTP basic auth, which is how the bad-token
 * path is tested.
 */
class GitHttpServer(
    private val workTree: File,
    private val requiredToken: String? = null,
) {
    private val socket = ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))
    private val pool = Executors.newCachedThreadPool()

    @Volatile
    private var running = true

    val log: MutableList<String> = java.util.Collections.synchronizedList(mutableListOf<String>())

    val url: String get() = "http://127.0.0.1:${socket.localPort}/repo.git"

    fun start(): GitHttpServer {
        pool.submit {
            while (running) {
                val client = try {
                    socket.accept()
                } catch (_: Exception) {
                    break
                }
                pool.submit { runCatching { handle(client) } }
            }
        }
        return this
    }

    fun stop() {
        running = false
        runCatching { socket.close() }
        pool.shutdownNow()
    }

    private fun handle(client: Socket) = client.use { connection ->
        val input = connection.getInputStream()
        val requestLine = readLine(input) ?: return@use
        val headers = readHeaders(input)

        if (requiredToken != null && !authorised(headers)) {
            respond(connection, 401, "text/plain", "unauthorized".toByteArray(), authRequired = true)
            return@use
        }

        log += "$requestLine | ${headers.entries.joinToString(", ") { "${it.key}=${it.value}" }}"

        val method = requestLine.substringBefore(' ')
        val target = requestLine.substringAfter(' ').substringBefore(' ')
        val path = target.substringBefore('?').removePrefix("/repo.git")
        val queryService = target.substringAfter("service=", "").substringBefore('&')

        when {
            method == "GET" && path == "/info/refs" && queryService == "git-upload-pack" ->
                respond(
                    connection,
                    200,
                    "application/x-git-upload-pack-advertisement",
                    advertise(),
                )

            method == "POST" && path == "/git-upload-pack" -> {
                log += "post body handling"
                val raw = when {
                    headers["transfer-encoding"]?.contains("chunked") == true ->
                        readChunkedBody(input)

                    else -> readBody(input, headers["content-length"]?.toIntOrNull() ?: 0)
                }
                val body = when {
                    headers["content-encoding"]?.contains("gzip") == true -> gunzip(raw)
                    else -> raw
                }
                log += "post body ${body.size} bytes"
                val result = try {
                    uploadPack(body)
                } catch (e: Exception) {
                    log += "uploadPack threw ${e::class.java.name}: ${e.message}"
                    throw e
                }
                log += "result ${result.size} bytes"
                respond(connection, 200, "application/x-git-upload-pack-result", result)
            }

            else -> {
                log += "unhandled $method $path service=$queryService"
                respond(connection, 404, "text/plain", "not found".toByteArray())
            }
        }
    }

    private fun advertise(): ByteArray = withRepository { repository ->
        val out = ByteArrayOutputStream()
        val packet = PacketLineOut(out)
        packet.writeString("# service=git-upload-pack\n")
        packet.end()

        val upload = UploadPack(repository)
        upload.setBiDirectionalPipe(false)
        upload.sendAdvertisedRefs(RefAdvertiser.PacketLineOutRefAdvertiser(PacketLineOut(out)))
        out.toByteArray()
    }

    private fun uploadPack(body: ByteArray): ByteArray = withRepository { repository ->
        val out = ByteArrayOutputStream()
        val upload = UploadPack(repository)
        upload.setBiDirectionalPipe(false)
        upload.upload(ByteArrayInputStream(body), out, null)
        out.toByteArray()
    }

    private fun <T> withRepository(block: (Repository) -> T): T =
        Git.open(workTree).use { block(it.repository) }

    private fun authorised(headers: Map<String, String>): Boolean {
        val header = headers["authorization"] ?: return false
        if (!header.startsWith("Basic ")) return false
        val decoded = String(Base64.decode(header.removePrefix("Basic "), Base64.DEFAULT))
        return decoded.substringBefore(':') == requiredToken
    }

    private fun readHeaders(input: InputStream): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        while (true) {
            val line = readLine(input) ?: break
            if (line.isEmpty()) break
            headers[line.substringBefore(':').trim().lowercase()] =
                line.substringAfter(':').trim()
        }
        return headers
    }

    private fun readBody(input: InputStream, length: Int): ByteArray {
        val body = ByteArray(length)
        var read = 0
        while (read < length) {
            val count = input.read(body, read, length - read)
            if (count < 0) break
            read += count
        }
        return body
    }

    private fun gunzip(data: ByteArray): ByteArray =
        java.util.zip.GZIPInputStream(ByteArrayInputStream(data)).use { it.readBytes() }

    private fun readChunkedBody(input: InputStream): ByteArray {
        val body = ByteArrayOutputStream()
        while (true) {
            val size = readLine(input)?.substringBefore(';')?.trim()?.toIntOrNull(16) ?: break
            if (size == 0) {
                readLine(input)
                break
            }
            body.write(readBody(input, size))
            readLine(input)
        }
        return body.toByteArray()
    }

    private fun readLine(input: InputStream): String? {
        val buffer = StringBuilder()
        while (true) {
            val byte = input.read()
            if (byte == -1) return buffer.takeIf { it.isNotEmpty() }?.toString()
            if (byte == '\n'.code) return buffer.toString().removeSuffix("\r")
            buffer.append(byte.toChar())
        }
    }

    private fun respond(
        client: Socket,
        status: Int,
        contentType: String,
        body: ByteArray,
        authRequired: Boolean = false,
    ) {
        val out = BufferedOutputStream(client.getOutputStream())
        val head = buildString {
            append("HTTP/1.1 $status ${reason(status)}\r\n")
            append("Content-Type: $contentType\r\n")
            append("Content-Length: ${body.size}\r\n")
            append("Cache-Control: no-cache\r\n")
            if (authRequired) append("WWW-Authenticate: Basic realm=\"beans\"\r\n")
            append("Connection: close\r\n\r\n")
        }
        out.write(head.toByteArray())
        out.write(body)
        out.flush()
    }

    private fun reason(status: Int) = when (status) {
        200 -> "OK"
        401 -> "Unauthorized"
        else -> "Not Found"
    }
}
