package tcpforwarder.tcpforwarder.model;

import java.time.Instant;

public class ForwardingRule {

    private String id;
    private String name;
    private int listenPort;
    private String targetHost;
    private int targetPort;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public ForwardingRule() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getListenPort() { return listenPort; }
    public void setListenPort(int listenPort) { this.listenPort = listenPort; }

    public String getTargetHost() { return targetHost; }
    public void setTargetHost(String targetHost) { this.targetHost = targetHost; }

    public int getTargetPort() { return targetPort; }
    public void setTargetPort(int targetPort) { this.targetPort = targetPort; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
