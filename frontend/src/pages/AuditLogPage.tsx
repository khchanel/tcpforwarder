import React, { useCallback, useEffect, useState } from 'react';
import client from '../api/client';
import { AuditEntry, ForwardingRule } from '../types';

function formatBytes(b?: number): string {
  if (b === undefined) return '-';
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  return `${(b / (1024 * 1024)).toFixed(2)} MB`;
}

export default function AuditLogPage() {
  const [entries, setEntries] = useState<AuditEntry[]>([]);
  const [rules, setRules] = useState<ForwardingRule[]>([]);
  const [filterRuleId, setFilterRuleId] = useState('');
  const [limit, setLimit] = useState(100);

  const load = useCallback(async () => {
    const params: Record<string, string | number> = { limit };
    if (filterRuleId) params.ruleId = filterRuleId;
    const res = await client.get<AuditEntry[]>('/audit', { params });
    setEntries(res.data);
  }, [limit, filterRuleId]);

  useEffect(() => {
    client.get<ForwardingRule[]>('/rules').then(r => setRules(r.data));
  }, []);

  useEffect(() => { load(); }, [load]);

  return (
    <div>
      <div style={styles.header}>
        <h2 style={styles.heading}>Audit Log</h2>
        <div style={styles.filters}>
          <select style={styles.select} value={filterRuleId} onChange={e => setFilterRuleId(e.target.value)}>
            <option value="">All rules</option>
            {rules.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
          </select>
          <select style={styles.select} value={limit} onChange={e => setLimit(+e.target.value)}>
            <option value={50}>Last 50</option>
            <option value={100}>Last 100</option>
            <option value={500}>Last 500</option>
          </select>
          <button style={styles.refreshBtn} onClick={load}>Refresh</button>
        </div>
      </div>

      <table style={styles.table}>
        <thead>
          <tr>
            {['Timestamp', 'Event', 'Rule', 'Client IP', 'Bytes In', 'Bytes Out', 'Duration'].map(h => (
              <th key={h} style={styles.th}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {entries.map((e, i) => (
            <tr key={i} style={styles.tr}>
              <td style={{ ...styles.td, fontFamily: 'monospace', fontSize: 12 }}>
                {new Date(e.timestamp).toLocaleString()}
              </td>
              <td style={styles.td}>
                <span style={{ ...styles.badge, background: e.event === 'CONNECTED' ? '#d1fae5' : '#f1f5f9', color: e.event === 'CONNECTED' ? '#065f46' : '#475569' }}>
                  {e.event}
                </span>
              </td>
              <td style={styles.td}>{e.ruleName}</td>
              <td style={styles.td}>{e.clientIp}</td>
              <td style={styles.td}>{formatBytes(e.bytesIn)}</td>
              <td style={styles.td}>{formatBytes(e.bytesOut)}</td>
              <td style={styles.td}>{e.durationMs !== undefined ? `${e.durationMs}ms` : '-'}</td>
            </tr>
          ))}
          {entries.length === 0 && (
            <tr><td colSpan={7} style={{ ...styles.td, textAlign: 'center', color: '#999' }}>No audit entries found</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20, flexWrap: 'wrap', gap: 12 },
  heading: { fontSize: 20, fontWeight: 600 },
  filters: { display: 'flex', gap: 10, alignItems: 'center' },
  select: { padding: '6px 10px', border: '1px solid #d1d5db', borderRadius: 6, fontSize: 13 },
  refreshBtn: { padding: '6px 14px', background: '#f1f5f9', border: '1px solid #d1d5db', borderRadius: 6, cursor: 'pointer', fontSize: 13 },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: { textAlign: 'left', padding: '10px 12px', background: '#f1f5f9', borderBottom: '2px solid #e2e8f0', fontSize: 13, fontWeight: 600, color: '#475569' },
  tr: { borderBottom: '1px solid #f1f5f9' },
  td: { padding: '10px 12px', fontSize: 13 },
  badge: { padding: '2px 8px', borderRadius: 10, fontSize: 11, fontWeight: 600 },
};
