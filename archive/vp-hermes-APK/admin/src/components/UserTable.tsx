import { useNavigate } from 'react-router-dom';

interface UserTableProps {
  users: any[];
}

export default function UserTable({ users }: UserTableProps) {
  const navigate = useNavigate();

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid #3A3A3A', color: '#9C9A94', fontSize: '0.8rem', textTransform: 'uppercase' }}>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'left' }}>Email</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'left' }}>Display Name</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'left' }}>Role</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'left' }}>Status</th>
            <th style={{ padding: '0.75rem 1rem', textAlign: 'left' }}>Created</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user: any) => (
            <tr
              key={user.id}
              onClick={() => navigate(`/users/${user.id}`)}
              style={{
                borderBottom: '1px solid #2A2A2A',
                cursor: 'pointer',
                transition: 'background 0.15s',
              }}
              onMouseEnter={(e) => (e.currentTarget.style.background = '#232323')}
              onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
            >
              <td style={{ padding: '0.75rem 1rem', color: '#F5F0E8' }}>{user.email}</td>
              <td style={{ padding: '0.75rem 1rem', color: '#D4CEC4' }}>{user.display_name || '—'}</td>
              <td style={{ padding: '0.75rem 1rem' }}>
                <span style={{
                  padding: '2px 8px',
                  borderRadius: 999,
                  fontSize: '0.8rem',
                  background: user.role === 'admin' ? '#C9A96E22' : '#2A2A2A',
                  color: user.role === 'admin' ? '#C9A96E' : '#D4CEC4',
                }}>
                  {user.role}
                </span>
              </td>
              <td style={{ padding: '0.75rem 1rem' }}>
                <StatusBadge status={user.status} />
              </td>
              <td style={{ padding: '0.75rem 1rem', color: '#9C9A94', fontSize: '0.85rem' }}>
                {new Date(user.created_at).toLocaleDateString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  const colors: Record<string, string> = {
    active: '#5B9A5B',
    pending: '#C9A96E',
    disabled: '#E55959',
  };
  return (
    <span style={{
      padding: '2px 8px',
      borderRadius: 999,
      fontSize: '0.8rem',
      background: `${colors[status] || '#3A3A3A'}22`,
      color: colors[status] || '#D4CEC4',
    }}>
      {status}
    </span>
  );
}
