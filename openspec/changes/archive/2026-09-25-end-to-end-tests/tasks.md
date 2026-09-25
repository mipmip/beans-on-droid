## 1. Test server

- [x] 1.1 Smart HTTP endpoints backed by JGit's `UploadPack`
- [x] 1.2 Decompress gzipped request bodies
- [x] 1.3 Handle chunked request bodies
- [x] 1.4 Optional HTTP basic auth answering 401
- [x] 1.5 Request logging for diagnosis

## 2. End-to-end tests

- [x] 2.1 Add a repository over HTTP and reach a populated list
- [x] 2.2 Search by body text, then filter by type
- [x] 2.3 Open a bean and follow its parent link to the right bean
- [x] 2.4 Pull to refresh and see a bean committed after the clone
- [x] 2.5 A rejected token surfaces the authentication error
- [x] 2.6 The accepted token clones successfully

## 3. Data layer over HTTP

- [x] 3.1 Instrumented test: shallow clone and refresh over HTTP

## 4. CI

- [x] 4.1 `scripts/e2e.sh` booting a headless emulator, running the tests and
      shutting it down
- [x] 4.2 Script passes shellcheck in `nix flake check`
