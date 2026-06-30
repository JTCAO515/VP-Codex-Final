import type { ToolCategory, ToolsProvider } from "@/lib/tools/types";

function withToolDetails(category: Omit<ToolCategory, "sections" | "offlineTips" | "apiPriority">): ToolCategory {
  const sharedOfflineTips = [
    "Screenshot this checklist before you leave Wi-Fi.",
    "Keep hotel addresses and emergency contacts saved in Chinese characters.",
  ];

  const detailMap: Record<string, Pick<ToolCategory, "sections" | "offlineTips" | "apiPriority">> = {
    "visa-and-entry": {
      sections: [
        {
          title: "Before departure",
          items: [
            "Confirm visa-free, visa, or transit eligibility against an official embassy or consulate source.",
            "Save your passport photo page, visa page, first hotel, and onward or return ticket.",
          ],
        },
        {
          title: "At arrival",
          items: [
            "Keep your first hotel address and phone number ready in English and Chinese.",
            "Use the same itinerary details you gave the airline if border staff ask about your route.",
          ],
        },
      ],
      offlineTips: [...sharedOfflineTips, "Save embassy or consulate contact details for each destination city."],
      apiPriority: "Next later: verified visa-rule lookup once an official or trusted provider is selected.",
    },
    "payment-setup": {
      sections: [
        {
          title: "Before departure",
          items: [
            "Install Alipay or WeChat Pay and link an international card before you arrive.",
            "Tell your bank you are traveling so card verification and top-ups are less likely to fail.",
          ],
        },
        {
          title: "Backup plan",
          items: [
            "Carry a small amount of RMB for taxis, small vendors, or temporary app issues.",
            "Keep one physical card separate from your phone.",
          ],
        },
      ],
      offlineTips: [...sharedOfflineTips, "Write down your card issuer support number outside the payment app."],
      apiPriority: "Later: payment setup remains checklist-first; no payment transaction API is planned yet.",
    },
    translate: {
      sections: [
        {
          title: "Useful phrases",
          items: [
            "Save allergies, dietary limits, hotel address, and route questions as Chinese text.",
            "Use camera translation for menus and signs, then confirm key details with staff.",
          ],
        },
        {
          title: "Conversation",
          items: [
            "Keep sentences short and ask one thing at a time.",
            "Show written Chinese for addresses rather than relying on pronunciation.",
          ],
        },
      ],
      offlineTips: [...sharedOfflineTips, "Download an offline translation pack before boarding."],
      apiPriority: "Next later: machine translation API can be connected after language provider selection.",
    },
    currency: {
      sections: [
        {
          title: "Cash basics",
          items: [
            "RMB cash is useful as backup even if mobile payment works most of the time.",
            "Use bank ATMs when possible and check home-bank foreign withdrawal fees.",
          ],
        },
        {
          title: "Rate checks",
          items: [
            "Live exchange-rate conversion is not wired in yet.",
            "Confirm current rates with your bank or a trusted currency app before large exchanges.",
          ],
        },
      ],
      offlineTips: [...sharedOfflineTips, "Save a rough mental conversion for common RMB amounts."],
      apiPriority: "Next later: real-time exchange-rate API is the first Tools data integration candidate.",
    },
    metro: {
      sections: [
        {
          title: "Tickets and QR codes",
          items: [
            "Look for the transit QR feature inside Alipay or WeChat Pay.",
            "Station machines usually still sell single-ride tickets if app setup fails.",
          ],
        },
        {
          title: "Route reading",
          items: [
            "Save your destination station in Chinese and English.",
            "Check the last-train time before late dinners or night views.",
          ],
        },
      ],
      offlineTips: [...sharedOfflineTips, "Download metro maps for your planned cities before the trip."],
      apiPriority: "Later: live transit routing can be added after map or transit provider validation.",
    },
    "esim-vpn": {
      sections: [
        {
          title: "Connectivity",
          items: [
            "Confirm your eSIM works in mainland China before departure.",
            "Keep the QR activation code somewhere accessible offline.",
          ],
        },
        {
          title: "App access",
          items: [
            "Check which home-country apps you must reach during the trip.",
            "Test your VPN setup before relying on it for travel documents or messaging.",
          ],
        },
      ],
      offlineTips: [...sharedOfflineTips, "Save eSIM support instructions and VPN login backup codes offline."],
      apiPriority: "Later: provider comparison may be content-led first; no live telecom API is planned yet.",
    },
    emergency: {
      sections: [
        {
          title: "Contacts",
          items: [
            "Save local emergency numbers, embassy contact, insurance hotline, and hotel front desk.",
            "Share your itinerary and hotel addresses with a trusted contact.",
          ],
        },
        {
          title: "Documents",
          items: [
            "Carry passport and visa photos separately from the originals.",
            "Keep medication names and allergy notes in English and Chinese.",
          ],
        },
      ],
      offlineTips: [...sharedOfflineTips, "Pin your hotel address and nearest hospital in your maps app."],
      apiPriority: "Not planned yet: emergency content should stay conservative unless verified local data is available.",
    },
  };

  return {
    ...category,
    ...detailMap[category.id],
  };
}

const categories: ToolCategory[] = [
  withToolDetails({
    id: "visa-and-entry",
    name: "Visa and entry",
    summary: "Check visa requirements and entry paperwork before you fly.",
    tips: [
      "Confirm your passport has at least 6 months of validity left and 2+ blank pages.",
      "Check whether your nationality qualifies for a visa-free transit policy, and note the exact day limit and the cities it covers.",
      "Have proof of onward or return travel and your first night of accommodation ready for entry checks.",
      "Always confirm current rules on the official embassy or consulate website because visa policy changes often.",
    ],
  }),
  withToolDetails({
    id: "payment-setup",
    name: "Payment setup",
    summary: "Set up mobile payment so you are not stuck carrying only cash.",
    tips: [
      "Most local merchants expect Alipay or WeChat Pay; set one up before you arrive if possible.",
      "International cards usually work for the tourist version of Alipay, but daily and transaction limits apply.",
      "Carry a small amount of cash (RMB) as a backup for small vendors that do not take foreign cards.",
      "Notify your home bank of international travel to avoid your card being flagged for fraud.",
    ],
  }),
  withToolDetails({
    id: "translate",
    name: "Translate",
    summary: "Bridge the language gap for menus, signs, and conversations.",
    tips: [
      "Download an offline translation app or dictionary before you arrive because connectivity is not guaranteed everywhere.",
      "Save a few key phrases (greetings, directions, allergies) in Chinese characters so you can show them, not just say them.",
      "Camera-based translation works well for menus and street signs.",
    ],
    cta: { label: "打开翻译工具 Open Translator →", href: "/translate" },
  }),
  withToolDetails({
    id: "currency",
    name: "Currency",
    summary: "Understand RMB cash and exchange basics.",
    tips: [
      "Exchange a small amount of RMB before you arrive, or use an airport counter for emergency cash.",
      "ATMs at major banks generally accept foreign cards for RMB withdrawals; check your home bank's foreign withdrawal fees first.",
      "Live exchange-rate conversion isn't built in yet; check your bank or a currency app for the current rate.",
    ],
  }),
  withToolDetails({
    id: "metro",
    name: "Metro",
    summary: "Get around major cities without a car.",
    tips: [
      "Most metro systems accept the same mobile payment app you set up for daily spending; look for a transit QR code feature.",
      "Single-ride paper tickets are still available at station machines if you prefer not to link payment.",
      "Download the city's official metro app or a maps app with transit directions before you head out.",
    ],
  }),
  withToolDetails({
    id: "esim-vpn",
    name: "eSIM/VPN",
    summary: "Stay connected and reach the apps you rely on at home.",
    tips: [
      "Buy a China-compatible eSIM or local SIM before you need it; activation can take time.",
      "Some foreign apps and sites may not be reachable without a VPN; check what you need access to before travel.",
      "Test your eSIM/VPN setup before departure, not after you land.",
    ],
  }),
  withToolDetails({
    id: "emergency",
    name: "Emergency",
    summary: "Know who to call and what to carry in case something goes wrong.",
    tips: [
      "Save local emergency numbers and your embassy contact information offline.",
      "Carry a photo of your passport and visa separate from the originals.",
      "Know the address of your accommodation in Chinese characters in case you need to show it to someone for help.",
    ],
  }),
];

export function createStaticToolsProvider(): ToolsProvider {
  return {
    id: "static-tools",
    async getProviderStatus() {
      return {
        id: "static-tools",
        label: "Static travel tools provider",
        mode: "static",
        coverage: "7 travel tool categories with practical checklists and offline notes.",
        candidates: ["Exchange-rate API", "Machine translation API", "Visa rules API", "Transit data API"],
        nextIntegration: "Exchange-rate API should be validated first because it is low-risk and clearly scoped.",
        limitations: [
          "No real-time exchange rate, translation, visa-rule, or transit feed is connected yet.",
          "Static guidance must stay conservative and avoid implying official or real-time status.",
        ],
      };
    },
    async listCategories() {
      return categories;
    },
  };
}
