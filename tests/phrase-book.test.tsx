import { render, screen, fireEvent } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { PhraseBook } from "@/components/translate/PhraseBook";

describe("PhraseBook", () => {
  it("renders phrase book tabs", () => {
    render(<PhraseBook />);
    expect(screen.getByText(/常用短语 Phrases/i)).toBeInTheDocument();
    expect(screen.getByText(/特殊词语 Special Terms/i)).toBeInTheDocument();
  });

  it("shows greetings category by default", () => {
    render(<PhraseBook />);
    expect(screen.getAllByText(/Greetings/i).length).toBeGreaterThan(0);
  });

  it("switches to special terms tab", () => {
    render(<PhraseBook />);
    const termsBtn = screen.getByText(/特殊词语 Special Terms/i);
    fireEvent.click(termsBtn);
    expect(screen.getByText(/Attractions/i)).toBeInTheDocument();
    expect(screen.getByText(/Dishes/i)).toBeInTheDocument();
  });

  it("renders speak buttons for phrases", () => {
    render(<PhraseBook />);
    const speakBtns = screen.getAllByRole("button", { name: /朗读/i });
    expect(speakBtns.length).toBeGreaterThan(0);
  });

  it("switches phrase category when tab is clicked", () => {
    render(<PhraseBook />);
    const diningTab = screen.getByRole("tab", { name: /Dining/i });
    fireEvent.click(diningTab);
    expect(screen.getByText(/vegetarian/i)).toBeInTheDocument();
  });
});
