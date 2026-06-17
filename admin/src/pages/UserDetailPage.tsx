import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getUser, updateUser } from '../api/admin';

export default function UserDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: user, isLoading } = useQuery({
    queryKey: ['admin-user', id],
    queryFn: () => getUser(id!),
    enabled: !!id,
  });

  const [editMode, setEditMode] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [role, setRole] = useState('');
  const [status, setStatus] = useState('');

  const mutation = useMutation({
    mutationFn: () => updateUser(id!, { display_name: displayName, role, status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-user', id] });
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      setEditMode(false);
    },
  });

  const startEdit = () => {
    if (user) {
      setDisplayName(user.display_name || '');
      setRole(user.role);
      setStatus(user.status);
      setEditMode(true);
    }
  };

  if (isLoading) return <p style={{ color: '#9C9A94' }}>Loading...</p>;
  if (!user) return <p style={{ color: '#E55959' }}>User not found</p>;

  const fields = [
    { label: 'ID', value: user.id },
    { label: 'Email', value: user.email },
    { label: 'Display Name', value: editMode ? (
      <input
        value={displayName}
        onChange={(e) => setDisplayName(e.target.value)}
        style={{
          background: '#232323', border: '1px solid #3A3A3A', borderRadius: 6, padding: '0.4rem 0.6rem', color: '#F5F0E8', width: '100%',
        }}
      />
    ) : (user.display_name || '—') },
    { label: 'Role', value: editMode ? (
      <select value={role} onChange={(e) => setRole(e.target.value)} style={{
        background: '#232323', border: '1px solid #3A3A3A', borderRadius: 6, padding: '0.4rem 0.6rem', color: '#F5F0E8',
      }}>
        <option value="user">User</option>
        <option value="admin">Admin</option>
      </select>
    ) : user.role },
    { label: 'Status', value: editMode ? (
      <select value={status} onChange={(e) => setStatus(e.target.value)} style={{
        background: '#232323', border: '1px solid #3A3A3A', borderRadius: 6, padding: '0.4rem 0.6rem', color: '#F5F0E8',
      }}>
        <option value="active">Active</option>
        <option value="pending">Pending</option>
        <option value="disabled">Disabled</option>
      </select>
    ) : user.status },
    { label: 'Created At', value: new Date(user.created_at).toLocaleString() },
    { label: 'Last Login', value: user.last_login_at ? new Date(user.last_login_at).toLocaleString() : 'Never' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: '1.5rem' }}>
        <button onClick={() => navigate('/users')} style={{
          background: 'none', border: 'none', color: '#C9A96E', cursor: 'pointer', fontSize: '1.2rem',
        }}>
          ←
        </button>
        <h2 style={{ margin: 0, color: '#F5F0E8', fontSize: '1.5rem' }}>User Detail</h2>
      </div>

      <div style={{
        background: '#1A1A1A', border: '1px solid #3A3A3A', borderRadius: 10, padding: '1.5rem', maxWidth: 600,
      }}>
        {fields.map((f) => (
          <div key={f.label} style={{ display: 'flex', padding: '0.7rem 0', borderBottom: '1px solid #232323' }}>
            <div style={{ width: 140, color: '#9C9A94', fontSize: '0.85rem', flexShrink: 0 }}>{f.label}</div>
            <div style={{ color: '#F5F0E8', fontSize: '0.9rem', flex: 1 }}>{f.value}</div>
          </div>
        ))}

        <div style={{ marginTop: '1.5rem', display: 'flex', gap: 8 }}>
          {editMode ? (
            <>
              <button onClick={() => mutation.mutate()} disabled={mutation.isPending} style={{
                padding: '0.6rem 1.5rem', background: '#C9A96E', border: 'none', borderRadius: 8, color: '#0A0A0A', fontWeight: 600, cursor: 'pointer',
              }}>
                {mutation.isPending ? 'Saving...' : 'Save'}
              </button>
              <button onClick={() => setEditMode(false)} style={{
                padding: '0.6rem 1.5rem', background: '#232323', border: '1px solid #3A3A3A', borderRadius: 8, color: '#D4CEC4', cursor: 'pointer',
              }}>
                Cancel
              </button>
            </>
          ) : (
            <button onClick={startEdit} style={{
              padding: '0.6rem 1.5rem', background: '#232323', border: '1px solid #3A3A3A', borderRadius: 8, color: '#C9A96E', cursor: 'pointer',
            }}>
              Edit User
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
