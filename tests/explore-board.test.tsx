import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ExploreBoard } from "@/components/explore/ExploreBoard";

describe("ExploreBoard", () => {
  it("shows attractions, food, and stays for the default city and switches on city change", async () => {
    render(<ExploreBoard />);

    expect(await screen.findByRole("heading", { name: /beijing/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText("Forbidden City")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /^chengdu$/i }));

    expect(await screen.findByRole("heading", { name: /chengdu/i, level: 2 })).toBeInTheDocument();
    expect(await screen.findByText("Chengdu Research Base of Giant Panda Breeding")).toBeInTheDocument();
  });
});
