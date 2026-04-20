package tcpforwarder.tcpforwarder.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tcpforwarder.tcpforwarder.audit.AuditLogger;
import tcpforwarder.tcpforwarder.model.ConnectionSnapshot;
import tcpforwarder.tcpforwarder.model.ForwardingRule;
import tcpforwarder.tcpforwarder.stats.RuleStats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ConnectionSession {

    private static final Logger log = LoggerFactory.getLogger(ConnectionSession.class);
    private static final int BUFFER_SIZE = 8192;

    final String sessionId = UUID.randomUUID().toString();
    private final Socket clientSocket;
    private final Socket targetSocket;
    private final ForwardingRule rule;
    private final String clientIp;
    private final Instant startTime = Instant.now();
    private final AtomicLong bytesIn = new AtomicLong(0);
    private final AtomicLong bytesOut = new AtomicLong(0);
    private final RuleStats ruleStats;
    private final AuditLogger auditLogger;
    private final Consumer<String> onEnd;
    private final AtomicBoolean ended = new AtomicBoolean(false);

    public ConnectionSession(Socket clientSocket, Socket targetSocket, ForwardingRule rule,
                             RuleStats ruleStats, AuditLogger auditLogger, Consumer<String> onEnd) {
        this.clientSocket = clientSocket;
        this.targetSocket = targetSocket;
        this.rule = rule;
        this.clientIp = clientSocket.getInetAddress().getHostAddress();
        this.ruleStats = ruleStats;
        this.auditLogger = auditLogger;
        this.onEnd = onEnd;

        RuleStats.StatEntry stat = ruleStats.getOrCreate(rule.getId());
        stat.totalConnections.incrementAndGet();
        stat.activeConnections.incrementAndGet();
        stat.lastConnectedAt = startTime;

        auditLogger.logConnect(rule, clientIp, sessionId);
    }

    public void pipeClientToTarget() {
        pipe(clientSocket, targetSocket, bytesIn);
    }

    public void pipeTargetToClient() {
        pipe(targetSocket, clientSocket, bytesOut);
    }

    private void pipe(Socket src, Socket dst, AtomicLong counter) {
        try {
            InputStream in = src.getInputStream();
            OutputStream out = dst.getOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while (!src.isClosed() && (read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                out.flush();
                counter.addAndGet(read);
            }
        } catch (SocketException e) {
            if (!"Socket closed".equals(e.getMessage()) && !"Connection reset".equals(e.getMessage())) {
                log.debug("Socket exception in session {}: {}", sessionId, e.getMessage());
            }
        } catch (IOException e) {
            log.debug("IO exception in session {}: {}", sessionId, e.getMessage());
        } finally {
            closeQuietly(src);
            closeQuietly(dst);
            onSessionEnd();
        }
    }

    private void onSessionEnd() {
        // Guard: both pipe threads call this on completion — only run once
        if (!ended.compareAndSet(false, true)) return;

        long durationMs = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        RuleStats.StatEntry stat = ruleStats.getOrCreate(rule.getId());
        stat.activeConnections.decrementAndGet();
        stat.totalBytesIn.addAndGet(bytesIn.get());
        stat.totalBytesOut.addAndGet(bytesOut.get());
        auditLogger.logDisconnect(rule, clientIp, sessionId, bytesIn.get(), bytesOut.get(), durationMs);
        onEnd.accept(sessionId);
    }

    public void close() {
        closeQuietly(clientSocket);
        closeQuietly(targetSocket);
    }

    private void closeQuietly(Socket s) {
        if (s != null && !s.isClosed()) {
            try { s.close(); } catch (IOException ignored) {}
        }
    }

    public ConnectionSnapshot toSnapshot() {
        return new ConnectionSnapshot(
                sessionId,
                rule.getId(),
                rule.getName(),
                clientIp,
                bytesIn.get(),
                bytesOut.get(),
                startTime,
                Instant.now().toEpochMilli() - startTime.toEpochMilli()
        );
    }
}
