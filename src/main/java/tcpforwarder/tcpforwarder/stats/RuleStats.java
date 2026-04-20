package tcpforwarder.tcpforwarder.stats;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RuleStats {

    private final ConcurrentHashMap<String, StatEntry> stats = new ConcurrentHashMap<>();

    public StatEntry getOrCreate(String ruleId) {
        return stats.computeIfAbsent(ruleId, id -> new StatEntry());
    }

    public StatEntry get(String ruleId) {
        return stats.get(ruleId);
    }

    public Map<String, StatEntry> getAll() {
        return stats;
    }

    public void remove(String ruleId) {
        stats.remove(ruleId);
    }

    public static class StatEntry {
        public final AtomicLong totalBytesIn = new AtomicLong(0);
        public final AtomicLong totalBytesOut = new AtomicLong(0);
        public final AtomicLong totalConnections = new AtomicLong(0);
        public final AtomicLong activeConnections = new AtomicLong(0);
        public final AtomicLong connectionErrors = new AtomicLong(0);
        public volatile Instant lastConnectedAt;
        public volatile String lastError;
        public volatile Instant lastErrorAt;

        public void recordError(String message) {
            connectionErrors.incrementAndGet();
            lastError = message;
            lastErrorAt = Instant.now();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> m = new HashMap<>();
            m.put("totalBytesIn", totalBytesIn.get());
            m.put("totalBytesOut", totalBytesOut.get());
            m.put("totalConnections", totalConnections.get());
            m.put("activeConnections", activeConnections.get());
            m.put("connectionErrors", connectionErrors.get());
            m.put("lastConnectedAt", lastConnectedAt);
            m.put("lastError", lastError);
            m.put("lastErrorAt", lastErrorAt);
            return m;
        }
    }
}
