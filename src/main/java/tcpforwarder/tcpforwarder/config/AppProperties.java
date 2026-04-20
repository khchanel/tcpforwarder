package tcpforwarder.tcpforwarder.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "forwarder")
public class AppProperties {

    private static final Logger logger = LoggerFactory.getLogger(AppProperties.class);
    private String apiKey; // no default - must be explicitly set
    private String rulesFile = "./rules.yml";
    private String auditLogFile = "./logs/audit.log";

    @PostConstruct
    public void validate() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                    "FORWARDER_API_KEY environment variable must be set. " +
                            "Generate one with: openssl rand -hex 32"
            );
        }
        logger.info("API key configured (length: {} chars)", apiKey.length());
    }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getRulesFile() { return rulesFile; }
    public void setRulesFile(String rulesFile) { this.rulesFile = rulesFile; }

    public String getAuditLogFile() { return auditLogFile; }
    public void setAuditLogFile(String auditLogFile) { this.auditLogFile = auditLogFile; }
}
