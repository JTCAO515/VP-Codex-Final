"use client";

import { useMemo, useState } from "react";
import type { ToolCategory, ToolInteractiveDescriptor } from "@/lib/tools/types";

interface ToolWidgetProps {
  category: ToolCategory;
}

function parseLiveRates(category: ToolCategory): Record<string, number> {
  const liveSection = category.sections.find((section) => section.title.toLowerCase().includes("live rates"));
  if (!liveSection) return {};
  const rates: Record<string, number> = {};
  for (const item of liveSection.items) {
    const match = item.match(/1 CNY ≈ ([0-9.]+) ([A-Z]{3})/);
    if (match) rates[match[2]] = Number(match[1]);
  }
  return rates;
}

function CurrencyConverter({ category, descriptor }: { category: ToolCategory; descriptor: Extract<ToolInteractiveDescriptor, { type: "currency-converter" }> }) {
  const liveRates = useMemo(() => parseLiveRates(category), [category]);
  const rates = Object.keys(liveRates).length ? liveRates : descriptor.fallbackRates;
  const currencies = Object.keys(rates);
  const [amount, setAmount] = useState(100);
  const [target, setTarget] = useState(currencies.includes(descriptor.defaultTarget) ? descriptor.defaultTarget : currencies[0]);
  const converted = amount * (rates[target] ?? 0);
  const hasLiveRates = Object.keys(liveRates).length > 0;

  return (
    <div className="tool-widget" data-widget="currency-converter">
      <div className="tool-widget__head">
        <strong>Quick RMB converter</strong>
        <span>{hasLiveRates ? "Live rate" : "Offline estimate"}</span>
      </div>
      <div className="tool-widget__controls">
        <label>
          RMB amount
          <input min="0" onChange={(event) => setAmount(Number(event.target.value) || 0)} type="number" value={amount} />
        </label>
        <label>
          Convert to
          <select onChange={(event) => setTarget(event.target.value)} value={target}>
            {currencies.map((currency) => (
              <option key={currency} value={currency}>
                {currency}
              </option>
            ))}
          </select>
        </label>
      </div>
      <output className="tool-widget__result">
        ¥{amount.toLocaleString()} ≈ {converted.toLocaleString(undefined, { maximumFractionDigits: 2 })} {target}
      </output>
      <div className="tool-widget__chips" aria-label="Common RMB amounts">
        {descriptor.commonAmounts.map((value) => (
          <button key={value} onClick={() => setAmount(value)} type="button">
            ¥{value}
          </button>
        ))}
      </div>
    </div>
  );
}

function VisaChecker({ descriptor }: { descriptor: Extract<ToolInteractiveDescriptor, { type: "visa-checker" }> }) {
  const [nationalityId, setNationalityId] = useState(descriptor.nationalities[0]?.id ?? "other");
  const [days, setDays] = useState(7);
  const [transitOnly, setTransitOnly] = useState(false);
  const selected = descriptor.nationalities.find((item) => item.id === nationalityId) ?? descriptor.nationalities[0];

  let status = "Confirm with an official embassy or consulate before booking.";
  if (selected?.visaFreeDays && !transitOnly && days <= selected.visaFreeDays) {
    status = `Likely short-stay visa-free for up to ${selected.visaFreeDays} days, but confirm current official rules.`;
  } else if (transitOnly && selected?.transitHours) {
    status = `May fit a transit-without-visa route up to ${selected.transitHours} hours if the itinerary qualifies.`;
  } else if (selected?.visaFreeDays && days > selected.visaFreeDays) {
    status = `Your ${days}-day stay is longer than the ${selected.visaFreeDays}-day planning threshold shown here. Plan for a visa check.`;
  }

  return (
    <div className="tool-widget" data-widget="visa-checker">
      <div className="tool-widget__head">
        <strong>Entry planning check</strong>
        <span>Conservative guidance</span>
      </div>
      <div className="tool-widget__controls">
        <label>
          Passport nationality
          <select onChange={(event) => setNationalityId(event.target.value)} value={nationalityId}>
            {descriptor.nationalities.map((nationality) => (
              <option key={nationality.id} value={nationality.id}>
                {nationality.label}
              </option>
            ))}
          </select>
        </label>
        <label>
          Days in mainland China
          <input min="1" onChange={(event) => setDays(Number(event.target.value) || 1)} type="number" value={days} />
        </label>
      </div>
      <label className="tool-widget__toggle">
        <input checked={transitOnly} onChange={(event) => setTransitOnly(event.target.checked)} type="checkbox" />
        Transit-only route with onward travel
      </label>
      <output className="tool-widget__result">{status}</output>
      {selected?.note ? <p className="tool-widget__note">{selected.note}</p> : null}
    </div>
  );
}

function PaymentWizard({ descriptor }: { descriptor: Extract<ToolInteractiveDescriptor, { type: "payment-wizard" }> }) {
  const [walletId, setWalletId] = useState(descriptor.wallets[0]?.id ?? "alipay");
  const [cardId, setCardId] = useState(descriptor.cardBrands[0]?.id ?? "visa");
  const wallet = descriptor.wallets.find((item) => item.id === walletId) ?? descriptor.wallets[0];
  const card = descriptor.cardBrands.find((item) => item.id === cardId) ?? descriptor.cardBrands[0];

  const steps = [
    `Install ${wallet?.appName ?? "the wallet app"} before departure and create your account on Wi-Fi.`,
    `Add your ${card?.label ?? "card"} and complete issuer verification while your home phone number still works.`,
    "Save your passport name spelling exactly as it appears on your travel document.",
    "Carry a small RMB cash backup and one physical card in a separate place.",
  ];

  return (
    <div className="tool-widget" data-widget="payment-wizard">
      <div className="tool-widget__head">
        <strong>Payment setup path</strong>
        <span>Pre-trip</span>
      </div>
      <div className="tool-widget__controls">
        <label>
          Wallet
          <select onChange={(event) => setWalletId(event.target.value)} value={walletId}>
            {descriptor.wallets.map((item) => (
              <option key={item.id} value={item.id}>
                {item.label}
              </option>
            ))}
          </select>
        </label>
        <label>
          Card brand
          <select onChange={(event) => setCardId(event.target.value)} value={cardId}>
            {descriptor.cardBrands.map((item) => (
              <option key={item.id} value={item.id}>
                {item.label}
              </option>
            ))}
          </select>
        </label>
      </div>
      <ol className="tool-widget__steps">
        {steps.map((step) => (
          <li key={step}>{step}</li>
        ))}
      </ol>
      {card?.note ? <p className="tool-widget__note">{card.note}</p> : null}
    </div>
  );
}

export function ToolWidget({ category }: ToolWidgetProps) {
  const descriptor = category.interactive;
  if (!descriptor) return null;
  if (descriptor.type === "currency-converter") return <CurrencyConverter category={category} descriptor={descriptor} />;
  if (descriptor.type === "visa-checker") return <VisaChecker descriptor={descriptor} />;
  return <PaymentWizard descriptor={descriptor} />;
}
