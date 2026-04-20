package tcpforwarder.tcpforwarder.model;

import java.time.Instant;

public class ConnectionSnapshot {
    private final String sessionId;
    private final String ruleId;
    private final String ruleName;
    private final String clientIp;
    private final long bytesIn;
    private final long bytesOut;
    private final Instant startTime;
    private final long durationMs;

    public ConnectionSnapshot(String sessionId, String ruleId, String ruleName,
                              String clientIp, long bytesIn, long bytesOut,
                              Instant startTime, long durationMs) {
        this.sessionId = sessionId;
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.clientIp = clientIp;
        this.bytesIn = bytesIn;
        this.bytesOut = bytesOut;
        this.startTime = startTime;
        this.durationMs = durationMs;
    }

    public String getSessionId() { return sessionId; }
    public String getRuleId() { return ruleId; }
    public String getRuleName() { return ruleName; }
    public String getClientIp() { return clientIp; }
    public long getBytesIn() { return bytesIn; }
    public long getBytesOut() { return bytesOut; }
    public Instant getStartTime() { return startTime; }
    public long getDurationMs() { return durationMs; }
}
