---
# beans-qc6g
title: Integrate Agent Communication Protocol (ACP) as agent communication layer
status: draft
type: feature
priority: normal
tags:
    - idea
created_at: 2026-03-10T11:22:05Z
updated_at: 2026-03-12T07:33:29Z
order: zw
---

Add ACP (Agent Communication Protocol) support to beans-serve, enabling standardized REST+SSE communication with agents alongside the existing GraphQL API.

## Context

ACP (https://agentcommunicationprotocol.dev/) is an open protocol (v0.2.0, Apache 2.0, Linux Foundation) that standardizes how clients communicate with AI agents via REST. Currently, beans-serve talks to agents exclusively through a custom GraphQL API backed by Claude Code CLI processes. Adding ACP support would allow any ACP-compatible client to interact with beans-managed agents, and potentially allow beans to orchestrate agents hosted on external ACP servers.

## ACP Protocol Summary

**Endpoints:**
- `GET /agents` — List available agents (with manifests describing capabilities)
- `GET /agents/{name}` — Get agent manifest
- `POST /runs` — Create a run (sync, async, or streaming via SSE)
- `GET /runs/{run_id}` — Get run status
- `POST /runs/{run_id}` — Resume a paused/awaiting run
- `POST /runs/{run_id}/cancel` — Cancel a run
- `GET /runs/{run_id}/events` — List run events
- `GET /session/{session_id}` — Get session details

**Key Concepts:**
- **Agent Manifests**: Describe agent capabilities, supported content types, metadata
- **Runs**: A single agent execution with input messages, supporting sync/async/stream modes
- **Messages**: Multipart (text, images, files) with roles (`user`, `agent`, `agent/{name}`)
- **Sessions**: Stateful conversation context across multiple runs
- **Await**: Agent pauses execution requesting external input (maps to beans' pending interactions — permission requests, plan mode switches, ask-user)
- **Streaming**: SSE events (`message.created`, `message.part`, `message.completed`, `run.created`, `run.in-progress`, `run.awaiting`, `run.completed`, `run.failed`, etc.)
- **Trajectory Metadata**: Tool execution and reasoning step tracking

**OpenAPI Spec:** https://github.com/i-am-bee/acp/blob/main/docs/spec/openapi.yaml

## Mapping to beans' Current Architecture

| ACP Concept | beans Equivalent |
|---|---|
| Agent | Bean worktree agent session (keyed by beanID) |
| Run | A message-send-to-response cycle within a session |
| Message | AgentMessage (user/assistant/tool) |
| Await | PendingInteraction (permission_request, ask_user, plan mode) |
| Session | Session (with JSONL persistence, --resume) |
| SSE streaming | GraphQL subscriptions (pub/sub channels) |
| Agent manifest | Could describe Claude Code capabilities per-worktree |

## Approach: beans as ACP Client

The primary goal is to make beans-serve an **ACP client**, replacing the current tight coupling to Claude Code CLI process management with standardized HTTP REST + SSE communication.

### Current Architecture
```
Frontend -> GraphQL -> agent.Manager -> spawns claude CLI -> parses stream-json
```

### Target Architecture
```
Frontend -> GraphQL -> agent.Manager -> ACP Client (HTTP/SSE) -> ACP Server(s)
                                                                  |-- claude-agent-acp
                                                                  |-- Other ACP agents
```

## Existing ACP Servers (no need to build our own)

- **@zed-industries/claude-agent-acp** (1128 stars) - Official Zed-maintained ACP server wrapping the Claude Agent SDK. Supports tool calls with permission requests, streaming, sessions, images, slash commands, MCP servers. Available as `npx @zed-industries/claude-agent-acp` or pre-built binaries. https://github.com/zed-industries/claude-code-acp
- **Python ACP SDK** (`acp_sdk`) - Server framework with examples for OpenAI, LangGraph, LlamaIndex, CrewAI. https://github.com/i-am-bee/acp
- **ACP-MCP Adapter** - Bridges ACP agents into MCP-compatible clients. https://github.com/i-am-bee/acp-mcp

## Scope

### Phase 1: ACP Client in beans-serve
- [ ] Implement ACP client (HTTP REST + SSE) in Go (`internal/acp/`)
- [ ] Refactor `agent.Manager` to use ACP client instead of direct process spawning
- [ ] Map ACP run lifecycle to beans session model (`POST /runs` -> streaming -> `run.completed`)
- [ ] Map ACP `await`/`resume` to beans pending interactions (permissions, ask-user)
- [ ] Map ACP SSE events to GraphQL subscription updates
- [ ] Configuration for ACP server URL(s) in beans config

### Phase 2: Multi-Agent Support
- [ ] Agent discovery via `GET /agents` - UI for selecting which agent to assign per bean/worktree
- [ ] Support multiple ACP servers (different agents on different servers)
- [ ] Agent manifest display in UI (capabilities, description, status)

### Phase 3: Remove Direct Claude Code Integration
- [ ] Deprecate and remove `internal/agent/claude.go` and `internal/agent/parse.go`
- [ ] Document how to run `claude-agent-acp` alongside beans-serve

## Detailed ACP-to-beans Mapping

| ACP Concept | beans Equivalent | Notes |
|---|---|---|
| `POST /runs` (mode: stream) | `SendMessage()` | Creates a run per message turn |
| SSE `message.part` | Streaming text deltas | Maps to GraphQL subscription updates |
| SSE `run.awaiting` | `PendingInteraction` | Permission requests, ask-user |
| `POST /runs/{id}` (resume) | `ResolvePermission()` | Send `await_resume` payload |
| `POST /runs/{id}/cancel` | `StopSession()` | Cancel running agent |
| `session_id` | Session persistence | Maps to --resume / JSONL persistence |
| Agent manifest | Agent metadata | Name, capabilities, content types |
| Trajectory metadata | Tool invocations | Tool name, input, output tracking |

## Open Questions
- How should beans manage the lifecycle of ACP servers? Spawn as sidecar vs. expect user to run separately?
- How to map beans-specific features (plan mode, yolo mode, allowed-tools) to ACP await/resume payloads?
- Should the ACP server URL be per-project, per-bean, or global?
- How does agent authentication work when beans talks to remote ACP servers?

## References
- ACP Protocol spec: https://agentcommunicationprotocol.dev/
- ACP OpenAPI spec: https://github.com/i-am-bee/acp/blob/main/docs/spec/openapi.yaml
- ACP GitHub: https://github.com/i-am-bee/acp
- Claude Agent ACP (Zed): https://github.com/zed-industries/claude-code-acp
- ACP-MCP Adapter: https://github.com/i-am-bee/acp-mcp
