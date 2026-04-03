package tcpforwarder.tcpforwarder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "forwarder")
public class AppProperties {

    private String apiKey = "changeme";
    private String rulesFile = "./rules.yml";
    private String auditLogFile = "./logs/audit.log";

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getRulesFile() { return rulesFile; }
    public void setRulesFile(String rulesFile) { this.rulesFile = rulesFile; }

    public String getAuditLogFile() { return auditLogFile; }
    public void setAuditLogFile(String auditLogFile) { this.auditLogFile = auditLogFile; }
}
