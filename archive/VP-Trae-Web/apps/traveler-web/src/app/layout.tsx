import './globals.css';

export const metadata = {
  title: 'VisePanda Traveler Web',
  description: '游客可浏览内容，保存时再登录。',
};

export default function RootLayout(props: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>{props.children}</body>
    </html>
  );
}
