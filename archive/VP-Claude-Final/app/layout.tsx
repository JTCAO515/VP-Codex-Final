import type { Metadata } from "next";
import { Caveat } from "next/font/google";
import "./globals.css";
import { TopNav } from "@/components/TopNav";
import { SideOrnament } from "@/components/SideOrnament";

const caveat = Caveat({ subsets: ["latin"], variable: "--font-display" });

export const metadata: Metadata = {
  title: "VisePanda",
  description: "AI travel butler for visiting China",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={caveat.variable}>
      <body className="flex h-screen flex-col bg-ink-cream">
        <TopNav />
        <SideOrnament />
        <main className="flex-1 overflow-hidden md:pl-40">{children}</main>
      </body>
    </html>
  );
}
