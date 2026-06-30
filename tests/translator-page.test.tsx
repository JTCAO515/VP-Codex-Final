import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { TranslatorPage } from "@/components/translate/TranslatorPage";

describe("TranslatorPage", () => {
  it("offers a Qwen voice tab instead of a coming-soon STT placeholder", async () => {
    render(<TranslatorPage />);

    expect(screen.getByRole("tab", { name: /voice/i })).toBeInTheDocument();
    expect(screen.queryByText(/coming soon/i)).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("tab", { name: /voice/i }));

    expect(screen.getByRole("heading", { name: /voice translation/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/audio file url/i)).toBeInTheDocument();
  });
});
