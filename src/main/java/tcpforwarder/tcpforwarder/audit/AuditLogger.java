package tcpforwarder.tcpforwarder.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tcpforwarder.tcpforwarder.model.ForwardingRule;

import java.time.Instant;

@Component
public class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT_LOGGER");

    public void logConnect(ForwardingRule rule, String clientIp, String sessionId) {
        AUDIT.info("{\"timestamp\":\"{}\",\"event\":\"CONNECTED\",\"ruleId\":\"{}\",\"ruleName\":\"{}\",\"clientIp\":\"{}\",\"sessionId\":\"{}\"}",
                Instant.now(), rule.getId(), rule.getName(), clientIp, sessionId);
    }

    public void logDisconnect(ForwardingRule rule, String clientIp, String sessionId,
                              long bytesIn, long bytesOut, long durationMs) {
        AUDIT.info("{\"timestamp\":\"{}\",\"event\":\"DISCONNECTED\",\"ruleId\":\"{}\",\"ruleName\":\"{}\",\"clientIp\":\"{}\",\"sessionId\":\"{}\",\"bytesIn\":{},\"bytesOut\":{},\"durationMs\":{}}",
                Instant.now(), rule.getId(), rule.getName(), clientIp, sessionId, bytesIn, bytesOut, durationMs);
    }
}
