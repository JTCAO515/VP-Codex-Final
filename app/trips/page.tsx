import { AppShell } from "@/components/shell/AppShell";
import { TripsDashboard } from "@/components/trips/TripsDashboard";

export default function TripsPage() {
  return (
    <AppShell activeTab="trips">
      <TripsDashboard />
    </AppShell>
  );
}
