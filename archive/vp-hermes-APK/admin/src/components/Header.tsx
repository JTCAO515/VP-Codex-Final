import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { checkAdmin, logout } from '../api/admin';

export default function Header() {
  const [user, setUser] = useState<{ email?: string; role?: string } | null>(null);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const token = localStorage.getItem('vp_token');
    if (!token) {
      setUser(null);
      return;
    }
    checkAdmin()
      .then((u) => setUser({ email: u.email, role: u.role }))
      .catch(() => setUser(null));
  }, [location.pathname]);

  const isLoginPage = location.pathname === '/login';

  return (
    <header
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        height: 48,
        background: '#1A1A1A',
        borderBottom: '1px solid #3A3A3A',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 1.5rem',
        zIndex: 100,
      }}
    >
      {/* Left: Brand */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        <span
          onClick={() => navigate('/')}
          style={{
            color: '#C9A96E',
            fontSize: '1rem',
            fontWeight: 600,
            cursor: 'pointer',
            userSelect: 'none',
          }}
        >
          🐼 VisePanda
        </span>
        <span style={{
          color: '#6C6A64',
          fontSize: '0.75rem',
          fontFamily: 'monospace',
          padding: '2px 6px',
          border: '1px solid #3A3A3A',
          borderRadius: 4,
        }}>
          v0.2.7
        </span>
      </div>

      {/* Right: Login / User */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        {user ? (
          <>
            <span style={{ color: '#9C9A94', fontSize: '0.85rem' }}>
              {user.email}
            </span>
            {!isLoginPage && (
              <button
                onClick={() => { logout(); navigate('/login'); }}
                style={{
                  background: 'none',
                  border: '1px solid #3A3A3A',
                  color: '#D4CEC4',
                  padding: '4px 12px',
                  borderRadius: 6,
                  cursor: 'pointer',
                  fontSize: '0.85rem',
                }}
              >
                Log Out
              </button>
            )}
          </>
        ) : (
          <button
            onClick={() => navigate('/login')}
            style={{
              background: '#C9A96E',
              border: 'none',
              color: '#0A0A0A',
              padding: '6px 16px',
              borderRadius: 6,
              cursor: 'pointer',
              fontSize: '0.85rem',
              fontWeight: 600,
            }}
          >
            登录系统
          </button>
        )}
      </div>
    </header>
  );
}
