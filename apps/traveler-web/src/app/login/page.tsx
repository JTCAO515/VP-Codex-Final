import { PageContainer } from '@/components/PageContainer';

export default function LoginPage() {
  return (
    <PageContainer title="登录或注册">
      <div className="rounded-xl border bg-white p-5 shadow-sm">
        <p className="text-sm text-zinc-600">
          当前先保留登录入口占位。下一步会接入 `POST /auth/login` 与 `POST /auth/register`，并支持“保存时再登录”。
        </p>
      </div>
    </PageContainer>
  );
}
