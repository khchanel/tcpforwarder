package tcpforwarder.tcpforwarder.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tcpforwarder.tcpforwarder.audit.AuditLogger;
import tcpforwarder.tcpforwarder.model.ConnectionSnapshot;
import tcpforwarder.tcpforwarder.model.ForwardingRule;
import tcpforwarder.tcpforwarder.persistence.RuleRepository;
import tcpforwarder.tcpforwarder.stats.RuleStats;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ForwardingEngine {

    private static final Logger log = LoggerFactory.getLogger(ForwardingEngine.class);

    private final RuleRepository repository;
    private final RuleStats ruleStats;
    private final AuditLogger auditLogger;
    private final Map<String, RuleListener> listeners = new ConcurrentHashMap<>();

    private final AtomicInteger threadCounter = new AtomicInteger(0);
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setName("tcpfw-" + threadCounter.getAndIncrement());
        t.setDaemon(true);
        return t;
    });

    public ForwardingEngine(RuleRepository repository, RuleStats ruleStats, AuditLogger auditLogger) {
        this.repository = repository;
        this.ruleStats = ruleStats;
        this.auditLogger = auditLogger;
    }

    public void startAll() {
        repository.findAll().stream()
                .filter(ForwardingRule::isEnabled)
                .forEach(rule -> {
                    try {
                        startRule(rule);
                    } catch (IOException e) {
                        log.error("Failed to start rule '{}' on port {}: {}", rule.getName(), rule.getListenPort(), e.getMessage());
                    }
                });
    }

    public synchronized void startRule(ForwardingRule rule) throws IOException {
        if (listeners.containsKey(rule.getId())) {
            log.warn("Rule {} is already running, skipping start", rule.getId());
            return;
        }
        RuleListener listener = new RuleListener(rule, executor, ruleStats, auditLogger);
        listeners.put(rule.getId(), listener);
        executor.submit(listener::runAcceptLoop);
    }

    public synchronized void stopRule(String ruleId) {
        RuleListener listener = listeners.remove(ruleId);
        if (listener != null) {
            listener.stop();
        }
    }

    public synchronized void restartRule(ForwardingRule rule) throws IOException {
        stopRule(rule.getId());
        if (rule.isEnabled()) {
            startRule(rule);
        }
    }

    public boolean isRunning(String ruleId) {
        return listeners.containsKey(ruleId);
    }

    public List<ConnectionSnapshot> getActiveSessions() {
        List<ConnectionSnapshot> result = new ArrayList<>();
        listeners.values().forEach(listener ->
                listener.getActiveSessions().forEach(session -> result.add(session.toSnapshot()))
        );
        return result;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ForwardingEngine...");
        new ArrayList<>(listeners.keySet()).forEach(this::stopRule);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("ForwardingEngine shutdown complete");
    }
}
