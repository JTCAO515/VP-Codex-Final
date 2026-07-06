'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';

import { AUTH_COOKIE_ACCESS_TOKEN, AUTH_COOKIE_ROLE, type AuthSessionPayload } from '@/lib/auth';

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  requestId: string;
  error?: { code: string; message: string };
};

type Mode = 'login' | 'register';

function setCookie(name: string, value: string, maxAgeSeconds: number) {
  document.cookie = `${name}=${encodeURIComponent(value)}; path=/; max-age=${maxAgeSeconds}`;
}

export function LoginForm(props: { nextPath: string }) {
  const router = useRouter();

  const [mode, setMode] = useState<Mode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const endpoint = mode === 'login' ? '/api/auth/login' : '/api/auth/register';

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({
          email,
          password,
          ...(mode === 'register' ? { displayName } : {}),
        }),
      });

      const payload = (await response.json()) as ApiEnvelope<AuthSessionPayload>;

      if (!response.ok || !payload.success) {
        setError(payload.error?.message ?? '登录失败');
        return;
      }

      setCookie(AUTH_COOKIE_ACCESS_TOKEN, payload.data.accessToken, payload.data.expiresIn);
      setCookie(AUTH_COOKIE_ROLE, payload.data.user.role, payload.data.expiresIn);
      localStorage.setItem(AUTH_COOKIE_ACCESS_TOKEN, payload.data.accessToken);
      localStorage.setItem(AUTH_COOKIE_ROLE, payload.data.user.role);

      router.replace(props.nextPath);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : '登录失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto w-full max-w-md rounded-xl border bg-white p-6 shadow-sm">
      <h2 className="text-xl font-semibold">{mode === 'login' ? '登录后继续' : '注册后继续'}</h2>
      <p className="mt-2 text-sm text-zinc-600">
        浏览内容和使用 AI 不需要先登录；只有保存行程和查看我的行程时才会要求登录。
      </p>

      <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
        {mode === 'register' ? (
          <label className="block">
            <span className="text-sm text-zinc-700">昵称</span>
            <input
              className="mt-1 w-full rounded-md border px-3 py-2"
              value={displayName}
              onChange={(event) => setDisplayName(event.target.value)}
              placeholder="比如：小王"
            />
          </label>
        ) : null}

        <label className="block">
          <span className="text-sm text-zinc-700">邮箱</span>
          <input
            className="mt-1 w-full rounded-md border px-3 py-2"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
            placeholder="traveler@example.com"
          />
        </label>

        <label className="block">
          <span className="text-sm text-zinc-700">密码</span>
          <input
            className="mt-1 w-full rounded-md border px-3 py-2"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            type="password"
            autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
            placeholder="请输入密码"
          />
        </label>

        {error ? <div className="rounded-md border border-red-200 bg-red-50 p-2 text-sm text-red-700">{error}</div> : null}

        <button
          className="w-full rounded-md bg-zinc-900 px-3 py-2 text-sm font-medium text-white hover:bg-zinc-800 disabled:opacity-50"
          disabled={loading}
          type="submit"
        >
          {loading ? '提交中…' : mode === 'login' ? '登录' : '注册'}
        </button>
      </form>

      <button
        className="mt-4 text-sm text-zinc-600 underline underline-offset-2"
        disabled={loading}
        onClick={() => setMode((current) => (current === 'login' ? 'register' : 'login'))}
        type="button"
      >
        {mode === 'login' ? '没有账号？去注册' : '已有账号？去登录'}
      </button>
    </div>
  );
}
