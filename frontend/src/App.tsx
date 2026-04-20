import React from 'react';
import { BrowserRouter, Link, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { getApiKey } from './api/client';
import LoginPage from './pages/LoginPage';
import RulesPage from './pages/RulesPage';
import ConnectionsPage from './pages/ConnectionsPage';
import StatsPage from './pages/StatsPage';
import AuditLogPage from './pages/AuditLogPage';

function RequireAuth({ children }: { children: JSX.Element }) {
  const key = getApiKey();
  if (!key) return <Navigate to="/login" replace />;
  return children;
}

function NavLink({ to, label }: { to: string; label: string }) {
  const { pathname } = useLocation();
  const active = pathname === to || (to !== '/' && pathname.startsWith(to));
  return (
    <Link to={to} style={{ ...styles.navLink, ...(active ? styles.navLinkActive : {}) }}>
      {label}
    </Link>
  );
}

function Layout({ children }: { children: React.ReactNode }) {
  return (
    <div style={styles.layout}>
      <nav style={styles.nav}>
        <span style={styles.navBrand}>TCP Forwarder</span>
        <div style={styles.navLinks}>
          <NavLink to="/" label="Rules" />
          <NavLink to="/connections" label="Connections" />
          <NavLink to="/stats" label="Stats" />
          <NavLink to="/audit" label="Audit Log" />
        </div>
      </nav>
      <main style={styles.main}>{children}</main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<RequireAuth><Layout><RulesPage /></Layout></RequireAuth>} />
        <Route path="/connections" element={<RequireAuth><Layout><ConnectionsPage /></Layout></RequireAuth>} />
        <Route path="/stats" element={<RequireAuth><Layout><StatsPage /></Layout></RequireAuth>} />
        <Route path="/audit" element={<RequireAuth><Layout><AuditLogPage /></Layout></RequireAuth>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

const styles: Record<string, React.CSSProperties> = {
  layout: { minHeight: '100vh', background: '#f8fafc' },
  nav: { background: '#1e293b', padding: '0 32px', display: 'flex', alignItems: 'center', gap: 32, height: 56 },
  navBrand: { color: '#f1f5f9', fontWeight: 700, fontSize: 16, marginRight: 16 },
  navLinks: { display: 'flex', gap: 4 },
  navLink: { color: '#94a3b8', textDecoration: 'none', padding: '6px 14px', borderRadius: 6, fontSize: 14, fontWeight: 500 },
  navLinkActive: { color: '#f1f5f9', background: '#334155' },
  main: { maxWidth: 1200, margin: '0 auto', padding: '32px 24px' },
};
