import type { Metadata, Viewport } from "next";
import "./globals.css";
import { I18nProvider } from "@/lib/i18n/I18nContext";

export const metadata: Metadata = {
  title: "VisePanda - AI China Travel Butler",
  description: "Plan a China trip with an AI butler and a live itinerary canvas.",
};

// Width-only viewport — no height constraint, so mobile browsers size the
// page by actual content/viewport height rather than a fixed value.
export const viewport: Viewport = {
  width: "device-width",
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
