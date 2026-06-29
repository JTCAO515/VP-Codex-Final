import type { ToolCategory, ToolsProvider } from "@/lib/tools/types";

const categories: ToolCategory[] = [
  {
    id: "visa-and-entry",
    name: "Visa and entry",
    summary: "Check visa requirements and entry paperwork before you fly.",
    tips: [
      "Confirm your passport has at least 6 months of validity left and 2+ blank pages.",
      "Check whether your nationality qualifies for a visa-free transit policy, and note the exact day limit and the cities it covers.",
      "Have proof of onward/return travel and your first night of accommodation ready for entry checks.",
      "Always confirm current rules on the official embassy or consulate website — visa policy changes often.",
    ],
  },
  {
    id: "payment-setup",
    name: "Payment setup",
    summary: "Set up mobile payment so you are not stuck carrying only cash.",
    tips: [
      "Most local merchants expect Alipay or WeChat Pay; set one up before you arrive if possible.",
      "International cards usually work for the tourist version of Alipay, but daily/transaction limits apply.",
      "Carry a small amount of cash (RMB) as a backup for small vendors that don't take foreign cards.",
      "Notify your home bank of international travel to avoid your card being flagged for fraud.",
    ],
  },
  {
    id: "translate",
    name: "Translate",
    summary: "Bridge the language gap for menus, signs, and conversations.",
    tips: [
      "Download an offline translation app/dictionary before you arrive — connectivity isn't guaranteed everywhere.",
      "Save a few key phrases (greetings, directions, allergies) in Chinese characters so you can show them, not just say them.",
      "Camera-based translation works well for menus and street signs.",
    ],
  },
  {
    id: "currency",
    name: "Currency",
    summary: "Understand RMB cash and exchange basics.",
    tips: [
      "Exchange a small amount of RMB before you arrive, or use an airport counter for emergency cash.",
      "ATMs at major banks generally accept foreign cards for RMB withdrawals; check your home bank's foreign withdrawal fees first.",
      "Live exchange-rate conversion isn't built in yet — check your bank or a currency app for the current rate.",
    ],
  },
  {
    id: "metro",
    name: "Metro",
    summary: "Get around major cities without a car.",
    tips: [
      "Most metro systems accept the same mobile payment app (Alipay/WeChat) you set up for daily spending — look for a transit QR code feature.",
      "Single-ride paper tickets are still available at station machines if you prefer not to link payment.",
      "Download the city's official metro app or a maps app with transit directions before you head out.",
    ],
  },
  {
    id: "esim-vpn",
    name: "eSIM/VPN",
    summary: "Stay connected and reach the apps you rely on at home.",
    tips: [
      "Buy a China-compatible eSIM or local SIM before you need it — activation can take time.",
      "Some foreign apps and sites are not reachable without a VPN; check what you'll need access to before you travel.",
      "Test your eSIM/VPN setup before departure, not after you land.",
    ],
  },
  {
    id: "emergency",
    name: "Emergency",
    summary: "Know who to call and what to carry in case something goes wrong.",
    tips: [
      "Save your destination country's local emergency numbers and your embassy's contact information offline.",
      "Carry a photo of your passport and visa separate from the originals.",
      "Know the address of your accommodation in Chinese characters in case you need to show it to someone for help.",
    ],
  },
];

export function createStaticToolsProvider(): ToolsProvider {
  return {
    id: "static-tools",
    async listCategories() {
      return categories;
    },
  };
}
