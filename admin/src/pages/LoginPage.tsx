import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginAdmin } from '../api/admin';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await loginAdmin(email, password);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: '#0A0A0A',
      }}
    >
      <form
        onSubmit={handleSubmit}
        style={{
          background: '#1A1A1A',
          border: '1px solid #3A3A3A',
          borderRadius: 12,
          padding: '2.5rem',
          width: '100%',
          maxWidth: 400,
        }}
      >
        <h1 style={{ color: '#C9A96E', margin: '0 0 0.25rem', fontSize: '1.5rem' }}>VisePanda Admin</h1>
        <p style={{ color: '#9C9A94', margin: '0 0 2rem', fontSize: '0.9rem' }}>Sign in to manage your users</p>

        {error && (
          <div style={{
            background: '#E5595922',
            border: '1px solid #E55959',
            borderRadius: 8,
            padding: '0.75rem 1rem',
            color: '#E55959',
            marginBottom: '1rem',
            fontSize: '0.85rem',
          }}>
            {error}
          </div>
        )}

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ color: '#D4CEC4', fontSize: '0.85rem', display: 'block', marginBottom: 6 }}>Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              background: '#232323',
              border: '1px solid #3A3A3A',
              borderRadius: 8,
              color: '#F5F0E8',
              fontSize: '0.95rem',
              outline: 'none',
            }}
          />
        </div>

        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ color: '#D4CEC4', fontSize: '0.85rem', display: 'block', marginBottom: 6 }}>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              background: '#232323',
              border: '1px solid #3A3A3A',
              borderRadius: 8,
              color: '#F5F0E8',
              fontSize: '0.95rem',
              outline: 'none',
            }}
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          style={{
            width: '100%',
            padding: '0.75rem',
            background: loading ? '#B89255' : '#C9A96E',
            border: 'none',
            borderRadius: 8,
            color: '#0A0A0A',
            fontSize: '1rem',
            fontWeight: 600,
            cursor: loading ? 'not-allowed' : 'pointer',
          }}
        >
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>
    </div>
  );
}
