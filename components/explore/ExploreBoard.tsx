"use client";

import { useEffect, useState } from "react";
import { getExploreProvider } from "@/lib/explore";
import type {
  ExploreAttraction,
  ExploreCity,
  ExploreFoodSpot,
  ExploreProviderStatus,
  ExploreRichMeta,
  ExploreStay,
} from "@/lib/explore";
import { useTranslation } from "@/lib/i18n/I18nContext";

export function ExploreBoard() {
  const [cities, setCities] = useState<ExploreCity[]>([]);
  const [activeCityId, setActiveCityId] = useState<string | null>(null);
  const [attractions, setAttractions] = useState<ExploreAttraction[]>([]);
  const [foodSpots, setFoodSpots] = useState<ExploreFoodSpot[]>([]);
  const [stays, setStays] = useState<ExploreStay[]>([]);
  const [providerStatus, setProviderStatus] = useState<ExploreProviderStatus | null>(null);
  const { t } = useTranslation();

  useEffect(() => {
    const provider = getExploreProvider();
    provider.getProviderStatus().then(setProviderStatus);
    provider.listCities().then((loaded) => {
      setCities(loaded);
      setActiveCityId((current) => current ?? loaded[0]?.id ?? null);
    });
  }, []);

  useEffect(() => {
    if (!activeCityId) return;
    const provider = getExploreProvider();
    provider.listAttractions(activeCityId).then(setAttractions);
    provider.listFoodSpots(activeCityId).then(setFoodSpots);
    provider.listStays(activeCityId).then(setStays);
  }, [activeCityId]);

  const activeCity = cities.find((city) => city.id === activeCityId) ?? null;

  function addToTrip(message: string) {
    if (typeof window === "undefined") return;
    window.location.href = `/chat?add=${encodeURIComponent(message)}`;
  }

  function buildAddToTripMessage(name: string, cityName: string, context?: string) {
    const detail = context ? ` (${context})` : "";
    return `Add ${name}${detail} in ${cityName} to my trip and ask VisePanda to rebalance the route around it.`;
  }

  function renderRichMeta(item: ExploreRichMeta) {
    const meta = [
      item.rating ? `★ ${item.rating}` : "",
      item.priceLevel ?? "",
      item.openHours ? "Open hours" : "",
      item.businessArea ?? "",
    ].filter(Boolean);

    if (meta.length === 0 && !item.tel && !item.photoUrl) return null;

    return (
      <div className="explore-poi-meta">
        {item.photoUrl ? <img alt="" loading="lazy" src={item.photoUrl} /> : null}
        <div>
          {meta.length > 0 ? (
            <p aria-label="POI details">
              {meta.map((entry) => (
                <span key={entry}>{entry}</span>
              ))}
            </p>
          ) : null}
          {item.pricePerPerson ? <small>Approx. ¥{item.pricePerPerson}/person</small> : null}
          {item.openHours ? <small>{item.openHours}</small> : null}
          {item.tel ? <small>{item.tel}</small> : null}
          {item.sourceLabel ? <em>{item.sourceLabel}</em> : null}
        </div>
      </div>
    );
  }

  return (
    <section className="explore-board" aria-labelledby="explore-title">
      <header className="explore-board__header">
        <p className="section-kicker">{t.explore.kicker}</p>
        <h1 id="explore-title">{t.explore.heading}</h1>
        <p>{t.explore.subtitle}</p>
      </header>

      <div className="explore-city-filters" aria-label="Explore city picker">
        {cities.map((city) => (
          <button
            aria-pressed={city.id === activeCityId}
            data-active={city.id === activeCityId ? "true" : "false"}
            key={city.id}
            onClick={() => setActiveCityId(city.id)}
            type="button"
          >
            {city.name}
          </button>
        ))}
      </div>
      <p className="explore-add-note">{t.explore.addToTrip} — VisePanda will reopen Chat and rebalance the route before updating the canvas.</p>

      {activeCity && (
        <div className="explore-board__body">
          <article className="explore-city-summary">
            <h2>{activeCity.name}</h2>
            <p className="explore-city-summary__region">{activeCity.region}</p>
            <p>{activeCity.tagline}</p>
            <ul aria-label={`${activeCity.name} best for`}>
              {activeCity.bestFor.map((tag) => (
                <li key={tag}>{tag}</li>
              ))}
            </ul>
          </article>
          {providerStatus && (
            <aside className="provider-status" aria-label="Explore provider status">
              <strong>{providerStatus.label}</strong>
              <span>{providerStatus.coverage}</span>
              <p>{providerStatus.nextIntegration}</p>
              <small>{providerStatus.candidates.join(" / ")}</small>
            </aside>
          )}

          <div className="explore-board__columns">
            <section aria-labelledby="explore-attractions-title">
              <h3 id="explore-attractions-title">{t.explore.attractions}</h3>
              <ul>
                {attractions.map((attraction) => (
                  <li key={attraction.id}>
                    <strong>{attraction.name}</strong>
                    <span>{attraction.description}</span>
                    {renderRichMeta(attraction)}
                    <button
                      type="button"
                      className="explore-add-button"
                      onClick={() => addToTrip(buildAddToTripMessage(attraction.name, activeCity.name))}
                    >
                      {t.explore.addToTrip}
                    </button>
                  </li>
                ))}
              </ul>
            </section>

            <section aria-labelledby="explore-food-title">
              <h3 id="explore-food-title">{t.explore.food}</h3>
              <ul>
                {foodSpots.map((spot) => (
                  <li key={spot.id}>
                    <strong>{spot.name}</strong>
                    <span>{spot.dish} — {spot.description}</span>
                    {renderRichMeta(spot)}
                    <button
                      type="button"
                      className="explore-add-button"
                      onClick={() =>
                        addToTrip(buildAddToTripMessage(spot.name, activeCity.name, spot.dish))
                      }
                    >
                      {t.explore.addToTrip}
                    </button>
                  </li>
                ))}
              </ul>
            </section>

            <section aria-labelledby="explore-stays-title">
              <h3 id="explore-stays-title">{t.explore.stays}</h3>
              <ul>
                {stays.map((stay) => (
                  <li key={stay.id}>
                    <strong>{stay.name}</strong>
                    <span>{stay.area} — {stay.description}</span>
                    {renderRichMeta(stay)}
                    <button
                      type="button"
                      className="explore-add-button"
                      onClick={() =>
                        addToTrip(buildAddToTripMessage(stay.name, activeCity.name, stay.area))
                      }
                    >
                      {t.explore.addToTrip}
                    </button>
                  </li>
                ))}
              </ul>
            </section>
          </div>
        </div>
      )}
    </section>
  );
}
