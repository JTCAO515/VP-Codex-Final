interface StatCardProps {
  label: string;
  value: number;
  color?: string;
}

export default function StatCard({ label, value, color = '#C9A96E' }: StatCardProps) {
  return (
    <div
      style={{
        background: '#232323',
        border: '1px solid #3A3A3A',
        borderRadius: 10,
        padding: '1.25rem 1.5rem',
        minWidth: 180,
      }}
    >
      <p style={{ margin: 0, color: '#9C9A94', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
        {label}
      </p>
      <p style={{ margin: '8px 0 0', fontSize: '2rem', fontWeight: 700, color }}>
        {value.toLocaleString()}
      </p>
    </div>
  );
}
