# System Design & Architecture: TCP Forwarder

The **TCP Forwarder** is a production-grade utility designed to tunnel TCP traffic from a local listening port to a remote target host and port. It is primarily used in QA infrastructure to allow developers to access databases or services that are not directly reachable from office networks.

## 1. High-Level Architecture

The system follows a **client-server architecture** consisting of a Spring Boot backend that handles the heavy lifting of TCP forwarding and a React-based frontend for management.

### Tech Stack
- **Backend:** Java 11, Spring Boot 2.7
- **Frontend:** React, TypeScript
- **Persistence:** Flat-file YAML (`rules.yml`)
- **Build System:** Maven (with `frontend-maven-plugin` to bundle the React UI into the fat JAR)

---

## 2. Component Design

### A. The Forwarding Engine (`ForwardingEngine`)
The engine is the central orchestrator of the system.
- **Lifecycle Management:** It starts, stops, and restarts forwarding rules.
- **Threading Model:** It maintains a single `CachedThreadPool`. This ensures that all forwarding activities (across all rules) share a managed pool of threads, preventing the system from being overwhelmed by an unbounded number of threads.

### B. The Listener (`RuleListener`)
For every enabled rule, the engine spawns one `RuleListener`.
- **Accept Loop:** It binds to a `ServerSocket` on the `listenPort` and runs a continuous loop to accept incoming client connections.
- **Session Initiation:** When a client connects, the listener immediately opens a second socket to the `targetHost:targetPort`. If successful, it hands both sockets over to a `ConnectionSession`.

### C. The Session (`ConnectionSession`)
The session is where the actual data transfer happens.
- **Bidirectional Piping:** Since TCP is full-duplex, the session creates two separate "pipe" tasks:
    1. **Client -> Target:** Reads from the client socket and writes to the target.
    2. **Target -> Client:** Reads from the target socket and writes to the client.
- **Resource Cleanup:** The session ensures that when one side of the connection closes, both sockets are closed quietly to prevent leaked handles.
- **Telemetry:** It tracks bytes transferred in both directions and calculates the total session duration.

### D. Persistence & Configuration (`RuleRepository`)
To avoid the overhead of a database, the system uses a **YAML-based flat-file store**.
- **In-Memory Cache:** Rules are loaded into a `ConcurrentHashMap` at startup for O(1) access.
- **Atomic Writes:** When a rule is updated, the repository writes the entire rule set to a `.tmp` file first and then performs an atomic move to the final `rules.yml` file. This prevents file corruption during crashes.

---

## 3. Features & Observability

### Live Monitoring & Stats
The system provides deep visibility into the traffic it handles:
- **RuleStats:** Tracks per-rule metrics including total connection counts, current active connections, and total throughput (bytes in/out).
- **Active Sessions:** The UI can query the `ForwardingEngine` to get a snapshot of all current tunnels, including the client's IP and the duration of their current session.

### Audit Logging
Every connection is recorded by the `AuditLogger`. When a session ends, the system logs:
- The rule used.
- The client's source IP.
- Total bytes transferred.
- Total connection time.

### Security
Access to the management API is protected by a simple but effective **API Key Filter**.
- All requests to `/api/**` must include an `X-API-Key` header.
- The key is configured via `application.yml`.

---

## 4. Data Flow Summary

1. **Configuration:** User defines a rule (e.g., `8000` -> `db.internal:5432`) via the React UI.
2. **Deployment:** `RuleRepository` saves to `rules.yml` -> `ForwardingEngine` starts a `RuleListener` on port `8000`.
3. **Connection:** A developer connects to `localhost:8000`.
4. **Forwarding:** `RuleListener` opens a socket to `db.internal:5432` -> `ConnectionSession` pipes bytes bidirectionally.
5. **Observability:** `RuleStats` increments active connections -> `AuditLogger` records the start -> UI shows the active session.
6. **Teardown:** Developer disconnects -> Session closes sockets -> `AuditLogger` records total bytes and duration.
