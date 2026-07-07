import { Link, useLocation, useNavigate } from 'react-router-dom';
import { logout } from '../api/admin';

const navItems = [
  { path: '/', label: 'Dashboard', icon: '📊' },
  { path: '/users', label: 'Users', icon: '👥' },
];

export default function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#0A0A0A' }}>
      {/* Sidebar */}
      <aside
        style={{
          width: 240,
          background: '#1A1A1A',
          borderRight: '1px solid #3A3A3A',
          padding: '1.5rem 0',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <div style={{ padding: '0 1.5rem 1.5rem', borderBottom: '1px solid #3A3A3A', marginBottom: 1 }}>
          <h1 style={{ color: '#C9A96E', fontSize: '1.3rem', margin: 0 }}>VisePanda Admin</h1>
          <p style={{ color: '#9C9A94', fontSize: '0.8rem', margin: '4px 0 0' }}>Management Console</p>
        </div>

        <nav style={{ flex: 1, padding: '0.5rem 0' }}>
          {navItems.map((item) => {
            const active = item.path === '/' ? location.pathname === '/' : location.pathname.startsWith(item.path);
            return (
              <Link
                key={item.path}
                to={item.path}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 10,
                  padding: '0.75rem 1.5rem',
                  color: active ? '#C9A96E' : '#D4CEC4',
                  background: active ? '#232323' : 'transparent',
                  borderRight: active ? '3px solid #C9A96E' : '3px solid transparent',
                  textDecoration: 'none',
                  fontSize: '0.95rem',
                }}
              >
                <span>{item.icon}</span>
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div style={{ padding: '1rem 1.5rem', borderTop: '1px solid #3A3A3A' }}>
          <button
            onClick={() => { logout(); navigate('/login'); }}
            style={{
              background: 'none',
              border: '1px solid #3A3A3A',
              color: '#D4CEC4',
              padding: '0.5rem 1rem',
              borderRadius: 6,
              cursor: 'pointer',
              width: '100%',
              fontSize: '0.9rem',
            }}
          >
            Log Out
          </button>
        </div>
      </aside>

      {/* Main */}
      <main style={{ flex: 1, padding: '2rem', overflow: 'auto' }}>
        {children}
      </main>
    </div>
  );
}
