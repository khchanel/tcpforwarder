import React, { useCallback, useEffect, useState } from 'react';
import client from '../api/client';
import { ForwardingRule, RuleStat } from '../types';

function formatBytes(b: number): string {
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  return `${(b / (1024 * 1024)).toFixed(2)} MB`;
}

export default function StatsPage() {
  const [stats, setStats] = useState<Record<string, RuleStat>>({});
  const [rules, setRules] = useState<ForwardingRule[]>([]);

  const load = useCallback(async () => {
    const [statsRes, rulesRes] = await Promise.all([
      client.get<Record<string, RuleStat>>('/stats'),
      client.get<ForwardingRule[]>('/rules'),
    ]);
    setStats(statsRes.data);
    setRules(rulesRes.data);
  }, []);

  useEffect(() => {
    load();
    const id = setInterval(load, 5000);
    return () => clearInterval(id);
  }, [load]);

  function getRuleName(id: string): string {
    return rules.find(r => r.id === id)?.name ?? id.substring(0, 8);
  }

  const entries = Object.entries(stats);

  return (
    <div>
      <h2 style={styles.heading}>Statistics</h2>
      {entries.length === 0 && <p style={styles.empty}>No stats yet. Statistics appear once a rule has received at least one connection.</p>}
      <div style={styles.grid}>
        {entries.map(([ruleId, stat]) => (
          <div key={ruleId} style={styles.card}>
            <h3 style={styles.cardTitle}>{getRuleName(ruleId)}</h3>
            <div style={styles.statRow}>
              <span style={styles.statLabel}>Active connections</span>
              <span style={styles.statValue}>{stat.activeConnections}</span>
            </div>
            <div style={styles.statRow}>
              <span style={styles.statLabel}>Total connections</span>
              <span style={styles.statValue}>{stat.totalConnections}</span>
            </div>
            <div style={styles.statRow}>
              <span style={styles.statLabel}>Bytes in</span>
              <span style={styles.statValue}>{formatBytes(stat.totalBytesIn)}</span>
            </div>
            <div style={styles.statRow}>
              <span style={styles.statLabel}>Bytes out</span>
              <span style={styles.statValue}>{formatBytes(stat.totalBytesOut)}</span>
            </div>
            {stat.lastConnectedAt && (
              <div style={styles.statRow}>
                <span style={styles.statLabel}>Last connection</span>
                <span style={{ ...styles.statValue, fontSize: 12 }}>{new Date(stat.lastConnectedAt).toLocaleString()}</span>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  heading: { fontSize: 20, fontWeight: 600, marginBottom: 20 },
  empty: { color: '#9ca3af', fontSize: 14 },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 20 },
  card: { background: '#fff', border: '1px solid #e2e8f0', borderRadius: 8, padding: 20 },
  cardTitle: { fontSize: 15, fontWeight: 600, marginBottom: 16, color: '#1e293b', borderBottom: '1px solid #f1f5f9', paddingBottom: 10 },
  statRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 0' },
  statLabel: { fontSize: 13, color: '#64748b' },
  statValue: { fontSize: 14, fontWeight: 600, color: '#1e293b' },
};
