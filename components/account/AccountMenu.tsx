"use client";

import { FormEvent, useEffect, useState } from "react";
import { getPandaAvatar, PANDA_AVATAR_STORAGE_KEY, pandaAvatars } from "@/lib/account/avatars";
import {
  signInWithGoogle,
  signInWithPassword,
  signOut,
  signUpWithPassword,
  updateDisplayName,
  updatePassword,
} from "@/lib/supabase/auth";
import { useSupabaseSession } from "@/lib/supabase/useSupabaseSession";

type AuthMode = "sign-in" | "sign-up";
type ProfileForm = "none" | "name" | "password";

export function AccountMenu() {
  const { configured, loading, session } = useSupabaseSession();
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState<AuthMode>("sign-in");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [profileForm, setProfileForm] = useState<ProfileForm>("none");
  const [name, setName] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [status, setStatus] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [avatarId, setAvatarId] = useState(pandaAvatars[0].id);

  useEffect(() => {
    const stored = window.localStorage.getItem(PANDA_AVATAR_STORAGE_KEY);
    if (stored) setAvatarId(getPandaAvatar(stored).id);
  }, []);

  function toggleOpen() {
    setOpen((current) => !current);
    setStatus(null);
  }

  async function handleAuthSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!email.trim() || !password.trim()) return;
    setBusy(true);
    const result =
      mode === "sign-in"
        ? await signInWithPassword(email.trim(), password)
        : await signUpWithPassword(email.trim(), password);
    setStatus(result.message);
    setBusy(false);
  }

  async function handleGoogleSignIn() {
    setBusy(true);
    const result = await signInWithGoogle();
    setStatus(result.message);
    setBusy(false);
  }

  async function handleNameSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!name.trim()) return;
    setBusy(true);
    const result = await updateDisplayName(name.trim());
    setStatus(result.message);
    setBusy(false);
    if (result.ok) setProfileForm("none");
  }

  async function handlePasswordSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!newPassword.trim()) return;
    setBusy(true);
    const result = await updatePassword(newPassword.trim());
    setStatus(result.message);
    setBusy(false);
    if (result.ok) {
      setProfileForm("none");
      setNewPassword("");
    }
  }

  async function handleSignOut() {
    setBusy(true);
    await signOut();
    setStatus("Signed out.");
    setBusy(false);
    setProfileForm("none");
  }

  function handleAvatarSelect(nextAvatarId: string) {
    const nextAvatar = getPandaAvatar(nextAvatarId);
    setAvatarId(nextAvatar.id);
    window.localStorage.setItem(PANDA_AVATAR_STORAGE_KEY, nextAvatar.id);
    setStatus(`${nextAvatar.name} selected.`);
  }

  const displayName =
    (session?.user.user_metadata?.full_name as string | undefined)?.trim() || session?.user.email || "";
  const activeAvatar = getPandaAvatar(avatarId);

  return (
    <div className="account-menu">
      <button
        aria-expanded={open}
        aria-haspopup="dialog"
        aria-label="Account"
        className="account-menu__trigger"
        onClick={toggleOpen}
        type="button"
      >
        <img alt="" className="account-menu__trigger-avatar" src={activeAvatar.src} />
      </button>

      {open && (
        <div className="account-menu__popover" role="dialog" aria-label="Account menu">
          <div className="account-menu__profile-head">
            <img alt="" className="account-menu__profile-avatar" src={activeAvatar.src} />
            <div>
              <p className="account-menu__profile-title">{displayName || "Guest traveler"}</p>
              <p className="account-menu__profile-subtitle">Panda avatar, stored locally</p>
            </div>
          </div>

          <div className="account-menu__avatar-picker" aria-label="Choose panda avatar">
            {pandaAvatars.map((avatar) => (
              <button
                aria-pressed={avatar.id === activeAvatar.id}
                className="account-menu__avatar-option"
                key={avatar.id}
                onClick={() => handleAvatarSelect(avatar.id)}
                type="button"
              >
                <img alt="" src={avatar.src} />
                <span>{avatar.name}</span>
              </button>
            ))}
          </div>

          {!configured && (
            <p>
              Supabase is not configured for this deployment yet. Guest mode stays available, and sign-in will
              activate once Supabase project keys are added.
            </p>
          )}

          {configured && loading && <p>Checking your session...</p>}

          {configured && !loading && session && (
            <div className="account-menu__signed-in">
              <p>Signed in as {displayName}.</p>

              {profileForm === "none" && (
                <div className="account-menu__actions">
                  <button type="button" onClick={() => setProfileForm("name")}>
                    Change name
                  </button>
                  <button type="button" onClick={() => setProfileForm("password")}>
                    Change password
                  </button>
                  <button disabled={busy} type="button" onClick={handleSignOut}>
                    Log out
                  </button>
                </div>
              )}

              {profileForm === "name" && (
                <form onSubmit={handleNameSubmit}>
                  <label htmlFor="account-name">New name</label>
                  <input
                    id="account-name"
                    onChange={(event) => setName(event.target.value)}
                    type="text"
                    value={name}
                  />
                  <div className="account-menu__form-actions">
                    <button disabled={busy || !name.trim()} type="submit">
                      Save
                    </button>
                    <button type="button" onClick={() => setProfileForm("none")}>
                      Cancel
                    </button>
                  </div>
                </form>
              )}

              {profileForm === "password" && (
                <form onSubmit={handlePasswordSubmit}>
                  <label htmlFor="account-new-password">New password</label>
                  <input
                    id="account-new-password"
                    minLength={6}
                    onChange={(event) => setNewPassword(event.target.value)}
                    type="password"
                    value={newPassword}
                  />
                  <div className="account-menu__form-actions">
                    <button disabled={busy || !newPassword.trim()} type="submit">
                      Save
                    </button>
                    <button type="button" onClick={() => setProfileForm("none")}>
                      Cancel
                    </button>
                  </div>
                </form>
              )}
            </div>
          )}

          {configured && !loading && !session && (
            <div className="account-menu__sign-in">
              <div className="account-menu__mode-toggle">
                <button data-active={mode === "sign-in"} onClick={() => setMode("sign-in")} type="button">
                  Sign in
                </button>
                <button data-active={mode === "sign-up"} onClick={() => setMode("sign-up")} type="button">
                  Sign up
                </button>
              </div>

              <form onSubmit={handleAuthSubmit}>
                <label htmlFor="account-email">Email</label>
                <input
                  id="account-email"
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="you@example.com"
                  required
                  type="email"
                  value={email}
                />
                <label htmlFor="account-password">Password</label>
                <input
                  id="account-password"
                  minLength={6}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                  type="password"
                  value={password}
                />
                <button disabled={busy || !email.trim() || !password.trim()} type="submit">
                  {mode === "sign-in" ? "Sign in" : "Sign up"}
                </button>
              </form>

              <button className="account-menu__google" disabled={busy} onClick={handleGoogleSignIn} type="button">
                Continue with Google
              </button>
            </div>
          )}

          {status && (
            <p aria-live="polite" className="account-menu__status" role="status">
              {status}
            </p>
          )}
        </div>
      )}
    </div>
  );
}
