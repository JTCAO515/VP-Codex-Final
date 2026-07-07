import api from './client';

export function loginAdmin(email: string, password: string) {
  return api.post('/api/auth/login', { email, password }).then((res) => {
    localStorage.setItem('vp_token', res.data.access_token);
    return res.data;
  });
}

export function checkAdmin() {
  const token = localStorage.getItem('vp_token');
  if (!token) return Promise.reject('No token');
  return api.get('/api/admin/me', {
    headers: { Authorization: `Bearer ${token}` },
  }).then((res) => res.data);
}

export function logout() {
  localStorage.removeItem('vp_token');
}

export function getStats() {
  return api.get('/api/admin/stats', {
    headers: { Authorization: `Bearer ${localStorage.getItem('vp_token')}` },
  }).then((res) => res.data);
}

export function getUsers(params: {
  search?: string;
  role?: string;
  status?: string;
  page?: number;
  page_size?: number;
}) {
  const clean: Record<string, string> = {};
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== '') clean[k] = String(v);
  });
  return api.get('/api/admin/users', {
    headers: { Authorization: `Bearer ${localStorage.getItem('vp_token')}` },
    params: clean,
  }).then((res) => res.data);
}

export function getUser(id: string) {
  return api.get(`/api/admin/users/${id}`, {
    headers: { Authorization: `Bearer ${localStorage.getItem('vp_token')}` },
  }).then((res) => res.data);
}

export function updateUser(id: string, data: Record<string, string>) {
  return api.patch(`/api/admin/users/${id}`, data, {
    headers: { Authorization: `Bearer ${localStorage.getItem('vp_token')}` },
  }).then((res) => res.data);
}
