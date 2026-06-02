"use client";

import { LogIn, LogOut } from "lucide-react";
import { useEffect, useMemo, useState } from "react";

import { Button } from "@/components/ui/button";
import { createClient } from "@/lib/supabase/client";
import type { User } from "@supabase/supabase-js";

export function AuthGate({ children }: { children: React.ReactNode }) {
  const supabase = useMemo(() => createClient(), []);
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    supabase.auth.getUser().then(({ data }) => {
      setUser(data.user);
      setLoading(false);
    });

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange((_event, session) => {
      setUser(session?.user ?? null);
    });

    return () => subscription.unsubscribe();
  }, [supabase]);

  async function signInWithGoogle() {
    const origin = window.location.origin;
    await supabase.auth.signInWithOAuth({
      provider: "google",
      options: {
        redirectTo: `${origin}/auth/callback?next=/`,
      },
    });
  }

  async function signOut() {
    await supabase.auth.signOut();
  }

  if (loading) {
    return (
      <main className="grid min-h-screen place-items-center bg-background px-6 text-foreground">
        <div className="h-20 w-full max-w-sm animate-pulse rounded-2xl bg-muted" />
      </main>
    );
  }

  if (!user) {
    return (
      <main className="grid min-h-screen place-items-center bg-background px-6 text-foreground">
        <section className="w-full max-w-sm space-y-8">
          <div className="space-y-3">
            <p className="text-sm text-muted-foreground">PennyRush</p>
            <h1 className="text-3xl font-bold tracking-normal">
              Every penny, in a rush to be tracked.
            </h1>
            <p className="text-base text-muted-foreground">
              Sign in to keep your accounts, imports, insights, and budgets
              ready across devices.
            </p>
          </div>
          <Button className="h-12 w-full" onClick={signInWithGoogle}>
            <LogIn className="mr-2 size-4" />
            Continue with Google
          </Button>
          <p className="text-center text-xs leading-5 text-muted-foreground">
            By continuing, you agree to the{" "}
            <a className="font-semibold text-foreground underline-offset-4 hover:underline" href="/terms">
              Terms
            </a>{" "}
            and{" "}
            <a className="font-semibold text-foreground underline-offset-4 hover:underline" href="/privacy">
              Privacy Policy
            </a>
            .
          </p>
        </section>
      </main>
    );
  }

  return (
    <div>
      <div className="fixed right-4 top-4 z-50">
        <Button variant="secondary" size="sm" onClick={signOut}>
          <LogOut className="mr-2 size-4" />
          Sign out
        </Button>
      </div>
      {children}
    </div>
  );
}
