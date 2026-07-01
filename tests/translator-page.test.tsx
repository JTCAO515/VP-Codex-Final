import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TranslatorPage } from "@/components/translate/TranslatorPage";

describe("TranslatorPage", () => {
  it("shows one unified translator workspace", () => {
    render(<TranslatorPage />);

    expect(screen.getByRole("heading", { name: /china travel translator/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/unified translator workspace/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /upload image/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /take photo/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /record/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/common phrases and travel terms/i)).toBeInTheDocument();
  });

  it("does not show separated translator panels or audio upload/url controls", () => {
    render(<TranslatorPage />);
    expect(screen.queryByRole("button", { name: /^text$/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /^scan$/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("heading", { name: /voice translation/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /upload audio/i })).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/audio file url/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/coming soon/i)).not.toBeInTheDocument();
  });
});
