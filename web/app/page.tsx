import { HomeDashboard } from "@/components/features/home-dashboard";
import { AuthGate } from "@/components/features/auth-gate";

export default function Page() {
  return (
    <AuthGate>
      <HomeDashboard />
    </AuthGate>
  );
}
