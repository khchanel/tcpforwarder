export interface ForwardingRule {
  id: string;
  name: string;
  listenPort: number;
  targetHost: string;
  targetPort: number;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RuleRequest {
  name: string;
  listenPort: number;
  targetHost: string;
  targetPort: number;
  enabled: boolean;
}

export interface ConnectionSession {
  sessionId: string;
  ruleId: string;
  ruleName: string;
  clientIp: string;
  bytesIn: number;
  bytesOut: number;
  startTime: string;
  durationMs: number;
}

export interface RuleStat {
  totalBytesIn: number;
  totalBytesOut: number;
  totalConnections: number;
  activeConnections: number;
  connectionErrors: number;
  lastConnectedAt: string | null;
  lastError: string | null;
  lastErrorAt: string | null;
}

export interface AuditEntry {
  timestamp: string;
  event: 'CONNECTED' | 'DISCONNECTED';
  ruleId: string;
  ruleName: string;
  clientIp: string;
  sessionId: string;
  bytesIn?: number;
  bytesOut?: number;
  durationMs?: number;
}
