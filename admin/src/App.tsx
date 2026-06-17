import { Routes, Route, Navigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { checkAdmin } from './api/admin';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import UserListPage from './pages/UserListPage';
import UserDetailPage from './pages/UserDetailPage';
import Layout from './components/Layout';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const [authorized, setAuthorized] = useState<boolean | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('vp_token');
    if (!token) {
      setAuthorized(false);
      return;
    }
    checkAdmin()
      .then((user) => setAuthorized(user.role === 'admin' && user.status === 'active'))
      .catch(() => setAuthorized(false));
  }, []);

  if (authorized === null) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#1A1A1A' }}>
        <div style={{ color: '#C9A96E', fontSize: '1.2rem' }}>Loading...</div>
      </div>
    );
  }

  if (!authorized) return <Navigate to="/login" replace />;
  return <Layout>{children}</Layout>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/users"
        element={
          <ProtectedRoute>
            <UserListPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/users/:id"
        element={
          <ProtectedRoute>
            <UserDetailPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}
