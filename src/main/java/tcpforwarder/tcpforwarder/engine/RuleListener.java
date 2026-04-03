package tcpforwarder.tcpforwarder.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tcpforwarder.tcpforwarder.audit.AuditLogger;
import tcpforwarder.tcpforwarder.model.ForwardingRule;
import tcpforwarder.tcpforwarder.stats.RuleStats;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class RuleListener {

    private static final Logger log = LoggerFactory.getLogger(RuleListener.class);

    private final ForwardingRule rule;
    private final ExecutorService executor;
    private final RuleStats ruleStats;
    private final AuditLogger auditLogger;
    private final ServerSocket serverSocket;
    private final ConcurrentHashMap<String, ConnectionSession> sessions = new ConcurrentHashMap<>();
    private volatile boolean accepting = true;

    public RuleListener(ForwardingRule rule, ExecutorService executor,
                        RuleStats ruleStats, AuditLogger auditLogger) throws IOException {
        this.rule = rule;
        this.executor = executor;
        this.ruleStats = ruleStats;
        this.auditLogger = auditLogger;
        this.serverSocket = new ServerSocket(rule.getListenPort());
        log.info("Listening on port {} → {}:{}", rule.getListenPort(), rule.getTargetHost(), rule.getTargetPort());
    }

    public void runAcceptLoop() {
        while (accepting) {
            try {
                Socket clientSocket = serverSocket.accept();
                log.debug("Incoming connection from {} on port {}", clientSocket.getInetAddress(), rule.getListenPort());

                Socket targetSocket = new Socket(rule.getTargetHost(), rule.getTargetPort());

                ConnectionSession session = new ConnectionSession(
                        clientSocket, targetSocket, rule, ruleStats, auditLogger,
                        sessionId -> sessions.remove(sessionId)
                );
                sessions.put(session.sessionId, session);

                executor.submit(session::pipeClientToTarget);
                executor.submit(session::pipeTargetToClient);

            } catch (IOException e) {
                if (accepting) {
                    log.warn("Error accepting connection on port {}: {}", rule.getListenPort(), e.getMessage());
                }
            }
        }
    }

    public void stop() {
        accepting = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.warn("Error closing server socket on port {}: {}", rule.getListenPort(), e.getMessage());
        }
        sessions.values().forEach(ConnectionSession::close);
        sessions.clear();
        log.info("Stopped listener on port {}", rule.getListenPort());
    }

    public Collection<ConnectionSession> getActiveSessions() {
        return sessions.values();
    }
}
