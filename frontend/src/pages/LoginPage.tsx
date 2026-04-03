import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { setApiKey } from '../api/client';
import client from '../api/client';

export default function LoginPage() {
  const [key, setKey] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setApiKey(key);
    try {
      await client.get('/rules');
      navigate('/');
    } catch {
      setError('Invalid API key');
    }
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>TCP Forwarder</h1>
        <p style={styles.subtitle}>Enter your API key to continue</p>
        <form onSubmit={handleSubmit}>
          <input
            type="password"
            value={key}
            onChange={e => setKey(e.target.value)}
            placeholder="API Key"
            style={styles.input}
            autoFocus
          />
          {error && <p style={styles.error}>{error}</p>}
          <button type="submit" style={styles.button}>Login</button>
        </form>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: { display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', background: '#f0f2f5' },
  card: { background: '#fff', padding: 40, borderRadius: 8, boxShadow: '0 2px 16px rgba(0,0,0,0.1)', width: 360 },
  title: { fontSize: 24, fontWeight: 700, marginBottom: 8, color: '#1a1a2e' },
  subtitle: { color: '#666', marginBottom: 24 },
  input: { width: '100%', padding: '10px 14px', border: '1px solid #ddd', borderRadius: 6, fontSize: 14, marginBottom: 12 },
  error: { color: '#e53e3e', marginBottom: 12, fontSize: 14 },
  button: { width: '100%', padding: '10px 0', background: '#2563eb', color: '#fff', border: 'none', borderRadius: 6, fontSize: 14, fontWeight: 600, cursor: 'pointer' },
};
