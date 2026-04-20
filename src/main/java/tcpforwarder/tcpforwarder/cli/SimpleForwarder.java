package tcpforwarder.tcpforwarder.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tcpforwarder.tcpforwarder.audit.AuditLogger;
import tcpforwarder.tcpforwarder.engine.ConnectionSession;
import tcpforwarder.tcpforwarder.engine.RuleListener;
import tcpforwarder.tcpforwarder.model.ForwardingRule;
import tcpforwarder.tcpforwarder.stats.RuleStats;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimpleForwarder {

    private static final Logger log = LoggerFactory.getLogger(SimpleForwarder.class);

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java -jar tcpforwarder.jar <listenPort> <targetHost> <targetPort>");
            System.exit(1);
        }

        int listenPort;
        String targetHost;
        int targetPort;

        try {
            listenPort = Integer.parseInt(args[0]);
            targetHost = args[1];
            targetPort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Error: Port must be a valid integer.");
            System.exit(1);
            return;
        }

        String ruleId = "simple-" + UUID.randomUUID().toString().substring(0, 8);
        ForwardingRule rule = new ForwardingRule();
        rule.setId(ruleId);
        rule.setName("Simple Forwarder (" + listenPort + "->" + targetHost + ":" + targetPort + ")");
        rule.setListenPort(listenPort);
        rule.setTargetHost(targetHost);
        rule.setTargetPort(targetPort);
        rule.setEnabled(true);

        RuleStats ruleStats = new RuleStats();
        AuditLogger auditLogger = new AuditLogger(); // In a real app, this might need config
        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            log.info("Starting simple forwarder: {} -> {}:{}", listenPort, targetHost, targetPort);
            RuleListener listener = new RuleListener(rule, executor, ruleStats, auditLogger);

            // Add shutdown hook for graceful exit
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down simple forwarder...");
                listener.stop();
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
                log.info("Shutdown complete.");
            }));

            listener.runAcceptLoop();

        } catch (IOException e) {
            log.error("Failed to start simple forwarder: {}", e.getMessage());
            System.exit(1);
        }
    }
}
