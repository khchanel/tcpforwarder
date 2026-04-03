import React, { useCallback, useEffect, useState } from 'react';
import client from '../api/client';
import { ForwardingRule, RuleRequest } from '../types';

const emptyForm: RuleRequest = { name: '', listenPort: 0, targetHost: '', targetPort: 0, enabled: true };

export default function RulesPage() {
  const [rules, setRules] = useState<ForwardingRule[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState<RuleRequest>(emptyForm);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    const res = await client.get<ForwardingRule[]>('/rules');
    setRules(res.data);
  }, []);

  useEffect(() => { load(); }, [load]);

  function openAdd() { setForm(emptyForm); setEditingId(null); setError(''); setShowForm(true); }
  function openEdit(r: ForwardingRule) {
    setForm({ name: r.name, listenPort: r.listenPort, targetHost: r.targetHost, targetPort: r.targetPort, enabled: r.enabled });
    setEditingId(r.id); setError(''); setShowForm(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    try {
      if (editingId) {
        await client.put(`/rules/${editingId}`, form);
      } else {
        await client.post('/rules', form);
      }
      setShowForm(false);
      load();
    } catch (err: any) {
      setError(err.response?.data?.error ?? 'Failed to save rule');
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm('Delete this rule?')) return;
    await client.delete(`/rules/${id}`);
    load();
  }

  async function toggleEnabled(r: ForwardingRule) {
    await client.post(`/rules/${r.id}/${r.enabled ? 'disable' : 'enable'}`);
    load();
  }

  return (
    <div>
      <div style={styles.header}>
        <h2 style={styles.heading}>Forwarding Rules</h2>
        <button style={styles.addBtn} onClick={openAdd}>+ Add Rule</button>
      </div>

      {showForm && (
        <div style={styles.formCard}>
          <h3 style={styles.formTitle}>{editingId ? 'Edit Rule' : 'New Rule'}</h3>
          <form onSubmit={handleSubmit}>
            <div style={styles.grid}>
              <label style={styles.label}>Name</label>
              <input style={styles.input} value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
              <label style={styles.label}>Listen Port</label>
              <input style={styles.input} type="number" min={1} max={65535} value={form.listenPort || ''} onChange={e => setForm({ ...form, listenPort: +e.target.value })} required />
              <label style={styles.label}>Target Host</label>
              <input style={styles.input} value={form.targetHost} onChange={e => setForm({ ...form, targetHost: e.target.value })} required />
              <label style={styles.label}>Target Port</label>
              <input style={styles.input} type="number" min={1} max={65535} value={form.targetPort || ''} onChange={e => setForm({ ...form, targetPort: +e.target.value })} required />
              <label style={styles.label}>Enabled</label>
              <input type="checkbox" checked={form.enabled} onChange={e => setForm({ ...form, enabled: e.target.checked })} />
            </div>
            {error && <p style={styles.error}>{error}</p>}
            <div style={styles.formActions}>
              <button type="button" style={styles.cancelBtn} onClick={() => setShowForm(false)}>Cancel</button>
              <button type="submit" style={styles.saveBtn}>Save</button>
            </div>
          </form>
        </div>
      )}

      <table style={styles.table}>
        <thead>
          <tr>
            {['Name', 'Listen Port', 'Target', 'Status', 'Actions'].map(h => (
              <th key={h} style={styles.th}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rules.map(r => (
            <tr key={r.id} style={styles.tr}>
              <td style={styles.td}>{r.name}</td>
              <td style={styles.td}>{r.listenPort}</td>
              <td style={styles.td}>{r.targetHost}:{r.targetPort}</td>
              <td style={styles.td}>
                <span style={{ ...styles.badge, background: r.enabled ? '#d1fae5' : '#fee2e2', color: r.enabled ? '#065f46' : '#991b1b' }}>
                  {r.enabled ? 'Active' : 'Disabled'}
                </span>
              </td>
              <td style={styles.td}>
                <button style={styles.actionBtn} onClick={() => toggleEnabled(r)}>{r.enabled ? 'Disable' : 'Enable'}</button>
                <button style={styles.actionBtn} onClick={() => openEdit(r)}>Edit</button>
                <button style={{ ...styles.actionBtn, color: '#e53e3e' }} onClick={() => handleDelete(r.id)}>Delete</button>
              </td>
            </tr>
          ))}
          {rules.length === 0 && (
            <tr><td colSpan={5} style={{ ...styles.td, textAlign: 'center', color: '#999' }}>No rules configured. Add one to get started.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 },
  heading: { fontSize: 20, fontWeight: 600 },
  addBtn: { padding: '8px 16px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600 },
  formCard: { background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 8, padding: 24, marginBottom: 24 },
  formTitle: { marginBottom: 16, fontSize: 16, fontWeight: 600 },
  grid: { display: 'grid', gridTemplateColumns: '140px 1fr', gap: '10px 16px', alignItems: 'center', marginBottom: 12 },
  label: { fontWeight: 500, fontSize: 14, color: '#374151' },
  input: { padding: '8px 10px', border: '1px solid #d1d5db', borderRadius: 6, fontSize: 14 },
  error: { color: '#e53e3e', fontSize: 14, marginBottom: 8 },
  formActions: { display: 'flex', gap: 12, justifyContent: 'flex-end' },
  cancelBtn: { padding: '8px 16px', border: '1px solid #d1d5db', borderRadius: 6, background: '#fff', cursor: 'pointer' },
  saveBtn: { padding: '8px 16px', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 6, cursor: 'pointer', fontWeight: 600 },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: { textAlign: 'left', padding: '10px 12px', background: '#f1f5f9', borderBottom: '2px solid #e2e8f0', fontSize: 13, fontWeight: 600, color: '#475569' },
  tr: { borderBottom: '1px solid #f1f5f9' },
  td: { padding: '12px 12px', fontSize: 14 },
  badge: { padding: '2px 10px', borderRadius: 12, fontSize: 12, fontWeight: 600 },
  actionBtn: { marginRight: 8, padding: '4px 10px', fontSize: 13, border: '1px solid #d1d5db', borderRadius: 4, background: '#fff', cursor: 'pointer' },
};
