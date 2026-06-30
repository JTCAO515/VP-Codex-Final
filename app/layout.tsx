import type { Metadata } from "next";
import "./globals.css";
import { I18nProvider } from "@/lib/i18n/I18nContext";

export const metadata: Metadata = {
  title: "VisePanda - AI China Travel Butler",
  description: "Plan a China trip with an AI butler and a live itinerary canvas.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <I18nProvider>{children}</I18nProvider>
      </body>
    </html>
  );
}
