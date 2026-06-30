import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TranslatorPage } from "@/components/translate/TranslatorPage";

describe("TranslatorPage", () => {
  it("shows all four translation panels simultaneously on one page", () => {
    render(<TranslatorPage />);

    expect(screen.getByRole("heading", { name: /china travel translator/i })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: /voice translation/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/audio file url/i)).toBeInTheDocument();
  });

  it("does not show page-level tab navigation for Text/Scan/Voice/Phrases", () => {
    render(<TranslatorPage />);
    expect(screen.queryByRole("button", { name: /^text$/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /^scan$/i })).not.toBeInTheDocument();
    expect(screen.queryByText(/coming soon/i)).not.toBeInTheDocument();
  });
});
