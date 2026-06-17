import { useState } from 'react';

interface UserFiltersProps {
  onSearch: (search: string) => void;
  onRoleFilter: (role: string) => void;
  onStatusFilter: (status: string) => void;
}

export default function UserFilters({ onSearch, onRoleFilter, onStatusFilter }: UserFiltersProps) {
  const [search, setSearch] = useState('');

  return (
    <div style={{ display: 'flex', gap: 12, marginBottom: '1.5rem', flexWrap: 'wrap' }}>
      <input
        type="text"
        placeholder="Search by email..."
        value={search}
        onChange={(e) => {
          setSearch(e.target.value);
          onSearch(e.target.value);
        }}
        style={{
          background: '#232323',
          border: '1px solid #3A3A3A',
          borderRadius: 8,
          padding: '0.5rem 1rem',
          color: '#F5F0E8',
          fontSize: '0.9rem',
          minWidth: 250,
          outline: 'none',
        }}
      />
      <select
        onChange={(e) => onRoleFilter(e.target.value)}
        style={{
          background: '#232323',
          border: '1px solid #3A3A3A',
          borderRadius: 8,
          padding: '0.5rem 1rem',
          color: '#F5F0E8',
          fontSize: '0.9rem',
          outline: 'none',
        }}
      >
        <option value="">All Roles</option>
        <option value="user">User</option>
        <option value="admin">Admin</option>
      </select>
      <select
        onChange={(e) => onStatusFilter(e.target.value)}
        style={{
          background: '#232323',
          border: '1px solid #3A3A3A',
          borderRadius: 8,
          padding: '0.5rem 1rem',
          color: '#F5F0E8',
          fontSize: '0.9rem',
          outline: 'none',
        }}
      >
        <option value="">All Status</option>
        <option value="active">Active</option>
        <option value="pending">Pending</option>
        <option value="disabled">Disabled</option>
      </select>
    </div>
  );
}
