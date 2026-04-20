import React, { useEffect, useState } from 'react';
import client from '../api/client';
import { ConnectionSession } from '../types';

function formatBytes(b: number): string {
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  return `${(b / (1024 * 1024)).toFixed(2)} MB`;
}

function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
  return `${Math.floor(ms / 60000)}m ${Math.floor((ms % 60000) / 1000)}s`;
}

export default function ConnectionsPage() {
  const [sessions, setSessions] = useState<ConnectionSession[]>([]);

  useEffect(() => {
    async function fetch() {
      const res = await client.get<ConnectionSession[]>('/connections');
      setSessions(res.data);
    }
    fetch();
    const id = setInterval(fetch, 2000);
    return () => clearInterval(id);
  }, []);

  return (
    <div>
      <div style={styles.header}>
        <h2 style={styles.heading}>Active Connections</h2>
        <span style={styles.badge}>{sessions.length} active</span>
      </div>
      <table style={styles.table}>
        <thead>
          <tr>
            {['Rule', 'Client IP', 'Bytes In', 'Bytes Out', 'Duration', 'Session ID'].map(h => (
              <th key={h} style={styles.th}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {sessions.map(s => (
            <tr key={s.sessionId} style={styles.tr}>
              <td style={styles.td}>{s.ruleName}</td>
              <td style={styles.td}>{s.clientIp}</td>
              <td style={styles.td}>{formatBytes(s.bytesIn)}</td>
              <td style={styles.td}>{formatBytes(s.bytesOut)}</td>
              <td style={styles.td}>{formatDuration(s.durationMs)}</td>
              <td style={{ ...styles.td, fontFamily: 'monospace', fontSize: 12, color: '#6b7280' }}>{s.sessionId.substring(0, 8)}</td>
            </tr>
          ))}
          {sessions.length === 0 && (
            <tr><td colSpan={6} style={{ ...styles.td, textAlign: 'center', color: '#999' }}>No active connections</td></tr>
          )}
        </tbody>
      </table>
      <p style={styles.refresh}>Auto-refreshes every 2 seconds</p>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  header: { display: 'flex', alignItems: 'center', gap: 16, marginBottom: 20 },
  heading: { fontSize: 20, fontWeight: 600 },
  badge: { background: '#dbeafe', color: '#1e40af', padding: '2px 12px', borderRadius: 12, fontSize: 13, fontWeight: 600 },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: { textAlign: 'left', padding: '10px 12px', background: '#f1f5f9', borderBottom: '2px solid #e2e8f0', fontSize: 13, fontWeight: 600, color: '#475569' },
  tr: { borderBottom: '1px solid #f1f5f9' },
  td: { padding: '12px 12px', fontSize: 14 },
  refresh: { marginTop: 12, color: '#9ca3af', fontSize: 12 },
};
