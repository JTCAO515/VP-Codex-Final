"use client";

import { useEffect, useState } from "react";
import { getExploreProvider } from "@/lib/explore";
import type { ExploreAttraction, ExploreCity, ExploreFoodSpot, ExploreStay } from "@/lib/explore";

export function ExploreBoard() {
  const [cities, setCities] = useState<ExploreCity[]>([]);
  const [activeCityId, setActiveCityId] = useState<string | null>(null);
  const [attractions, setAttractions] = useState<ExploreAttraction[]>([]);
  const [foodSpots, setFoodSpots] = useState<ExploreFoodSpot[]>([]);
  const [stays, setStays] = useState<ExploreStay[]>([]);

  useEffect(() => {
    const provider = getExploreProvider();
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

  return (
    <section className="explore-board" aria-labelledby="explore-title">
      <header className="explore-board__header">
        <p className="section-kicker">Explore</p>
        <h1 id="explore-title">Cities, attractions, food, and stays</h1>
        <p>Curated starting points for your China trip. Real provider data will connect here next.</p>
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
      <p className="explore-add-note">VisePanda will reopen Chat and rebalance the route before updating the canvas.</p>

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

          <div className="explore-board__columns">
            <section aria-labelledby="explore-attractions-title">
              <h3 id="explore-attractions-title">Attractions</h3>
              <ul>
                {attractions.map((attraction) => (
                  <li key={attraction.id}>
                    <strong>{attraction.name}</strong>
                    <span>{attraction.description}</span>
                    <button
                      type="button"
                      className="explore-add-button"
                      onClick={() => addToTrip(buildAddToTripMessage(attraction.name, activeCity.name))}
                    >
                      Add to Trip
                    </button>
                  </li>
                ))}
              </ul>
            </section>

            <section aria-labelledby="explore-food-title">
              <h3 id="explore-food-title">Food</h3>
              <ul>
                {foodSpots.map((spot) => (
                  <li key={spot.id}>
                    <strong>{spot.name}</strong>
                    <span>{spot.dish} — {spot.description}</span>
                    <button
                      type="button"
                      className="explore-add-button"
                      onClick={() =>
                        addToTrip(buildAddToTripMessage(spot.name, activeCity.name, spot.dish))
                      }
                    >
                      Add to Trip
                    </button>
                  </li>
                ))}
              </ul>
            </section>

            <section aria-labelledby="explore-stays-title">
              <h3 id="explore-stays-title">Stays</h3>
              <ul>
                {stays.map((stay) => (
                  <li key={stay.id}>
                    <strong>{stay.name}</strong>
                    <span>{stay.area} — {stay.description}</span>
                    <button
                      type="button"
                      className="explore-add-button"
                      onClick={() =>
                        addToTrip(buildAddToTripMessage(stay.name, activeCity.name, stay.area))
                      }
                    >
                      Add to Trip
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
