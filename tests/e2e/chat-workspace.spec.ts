import { expect, test } from "@playwright/test";

test("chat workspace updates the canvas", async ({ page }) => {
  await page.goto("/chat");
  await expect(page.getByText("VisePanda").first()).toBeVisible();
  await page.getByLabel(/ask visepanda/i).fill("I am visiting China for the first time for 5 days");
  await page.getByRole("button", { name: /send/i }).click();
  await expect(page.getByText(/Beijing/i).first()).toBeVisible();
  await expect(page.getByText(/Shanghai/i).first()).toBeVisible();
  await expect(page.getByText(/updated the canvas/i)).toBeVisible();
});
