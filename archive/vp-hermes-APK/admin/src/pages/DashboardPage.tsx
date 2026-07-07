import { useQuery } from '@tanstack/react-query';
import { getStats } from '../api/admin';
import StatCard from '../components/StatCard';

export default function DashboardPage() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: getStats,
  });

  return (
    <div>
      <h2 style={{ margin: '0 0 1.5rem', color: '#F5F0E8', fontSize: '1.5rem' }}>Dashboard</h2>

      {isLoading ? (
        <p style={{ color: '#9C9A94' }}>Loading...</p>
      ) : (
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          <StatCard label="Total Users" value={stats?.total || 0} />
          <StatCard label="Active" value={stats?.active || 0} color="#5B9A5B" />
          <StatCard label="Pending" value={stats?.pending || 0} color="#C9A96E" />
          <StatCard label="Disabled" value={stats?.disabled || 0} color="#E55959" />
        </div>
      )}
    </div>
  );
}
