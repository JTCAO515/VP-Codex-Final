import { LoginForm } from '@/components/LoginForm';
import { PageContainer } from '@/components/PageContainer';

export default async function LoginPage(props: { searchParams: Promise<{ next?: string }> }) {
  const searchParams = await props.searchParams;
  const nextPath = searchParams.next ?? '/trips';

  return (
    <PageContainer title="登录或注册">
      <LoginForm nextPath={nextPath} />
    </PageContainer>
  );
}
