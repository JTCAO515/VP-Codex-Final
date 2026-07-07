import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getUsers } from '../api/admin';
import UserTable from '../components/UserTable';
import UserFilters from '../components/UserFilters';

export default function UserListPage() {
  const [search, setSearch] = useState('');
  const [role, setRole] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users', search, role, status, page],
    queryFn: () => getUsers({ search, role, status, page, page_size: 20 }),
  });

  return (
    <div>
      <h2 style={{ margin: '0 0 1.5rem', color: '#F5F0E8', fontSize: '1.5rem' }}>Users</h2>

      <UserFilters
        onSearch={(s) => { setSearch(s); setPage(1); }}
        onRoleFilter={(r) => { setRole(r); setPage(1); }}
        onStatusFilter={(s) => { setStatus(s); setPage(1); }}
      />

      {isLoading ? (
        <p style={{ color: '#9C9A94' }}>Loading...</p>
      ) : (
        <>
          <UserTable users={data?.users || []} />

          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginTop: '1.5rem',
            color: '#9C9A94',
            fontSize: '0.85rem',
          }}>
            <span>Showing {(data?.users || []).length} of {data?.total || 0}</span>
            <div style={{ display: 'flex', gap: 8 }}>
              <button
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={page <= 1}
                style={{
                  padding: '0.4rem 0.8rem',
                  background: '#232323',
                  border: '1px solid #3A3A3A',
                  borderRadius: 6,
                  color: page <= 1 ? '#3A3A3A' : '#D4CEC4',
                  cursor: page <= 1 ? 'not-allowed' : 'pointer',
                }}
              >
                Previous
              </button>
              <button
                onClick={() => setPage((p) => p + 1)}
                style={{
                  padding: '0.4rem 0.8rem',
                  background: '#232323',
                  border: '1px solid #3A3A3A',
                  borderRadius: 6,
                  color: '#D4CEC4',
                  cursor: 'pointer',
                }}
              >
                Next
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
