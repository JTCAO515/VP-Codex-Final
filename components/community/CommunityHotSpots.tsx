"use client";

import { useState } from "react";
import { mockHotSpots, HOT_SPOT_CITIES } from "@/lib/community/mockData";
import type { CityHotSpot } from "@/lib/community/types";

type CategoryFilter = "all" | "attraction" | "food" | "hidden";

const CATEGORY_LABELS: Record<CategoryFilter, string> = {
  all: "All",
  attraction: "Attractions",
  food: "Food",
  hidden: "Hidden Gems",
};

function StarRating({ rating }: { rating: number }) {
  const full = Math.floor(rating);
  const half = rating - full >= 0.5;
  return (
    <span className="community-hotspot__stars" aria-label={`${rating} stars`}>
      {"★".repeat(full)}
      {half ? "½" : ""}
      <span className="community-hotspot__rating-num">{rating}</span>
    </span>
  );
}

function HotSpotCard({ spot }: { spot: CityHotSpot }) {
  const categoryColors: Record<string, string> = {
    attraction: "attraction",
    food: "food",
    hidden: "hidden",
  };

  return (
    <article className={`community-hotspot-card community-hotspot-card--${categoryColors[spot.category]}`} aria-label={spot.name}>
      <div className="community-hotspot-card__emoji" aria-hidden="true">{spot.emoji}</div>
      <div className="community-hotspot-card__body">
        <div className="community-hotspot-card__head">
          <h4 className="community-hotspot-card__name">{spot.name}</h4>
          <span className="community-hotspot-card__chinese">{spot.chinese}</span>
        </div>
        <div className="community-hotspot-card__rating">
          <StarRating rating={spot.rating} />
          <span className="community-hotspot-card__review-count">({spot.reviewCount} reviews)</span>
        </div>
        <p className="community-hotspot-card__tip">💡 {spot.tip}</p>
        <div className="community-hotspot-card__footer">
          <button
            className="community-hotspot-card__add-btn"
            type="button"
            onClick={() => {
              if (typeof window === "undefined") return;
              window.location.href = `/chat?add=${encodeURIComponent(
                `Add ${spot.name} (${spot.chinese}) in ${spot.cityName} to my trip and rebalance the route.`
              )}`;
            }}
          >
            + Add to Trip
          </button>
        </div>
      </div>
    </article>
  );
}

export function CommunityHotSpots() {
  const [activeCityId, setActiveCityId] = useState(HOT_SPOT_CITIES[0].id);
  const [category, setCategory] = useState<CategoryFilter>("all");

  const citySpots = mockHotSpots.filter(
    (s) => s.cityId === activeCityId && (category === "all" || s.category === category)
  );

  return (
    <div className="community-hotspots">
      <div className="community-hotspots__header">
        <p className="community-hotspots__subtitle">Community picks · Traveler reviews</p>
      </div>

      <div className="community-hotspots__city-filters" aria-label="City filters">
        {HOT_SPOT_CITIES.map((city) => (
          <button
            className={`community-hotspots__city-btn${activeCityId === city.id ? " active" : ""}`}
            key={city.id}
            onClick={() => setActiveCityId(city.id)}
            type="button"
          >
            {city.name}
          </button>
        ))}
      </div>

      <div className="community-hotspots__cat-filters" role="group" aria-label="Category filters">
        {(Object.keys(CATEGORY_LABELS) as CategoryFilter[]).map((cat) => (
          <button
            className={`community-hotspots__cat-btn${category === cat ? " active" : ""}`}
            key={cat}
            onClick={() => setCategory(cat)}
            type="button"
          >
            {CATEGORY_LABELS[cat]}
          </button>
        ))}
      </div>

      {citySpots.length === 0 ? (
        <p className="community-hotspots__empty">No spots in this category yet.</p>
      ) : (
        <div className="community-hotspots__list" aria-label="Hot spots">
          {citySpots.map((spot) => (
            <HotSpotCard key={spot.id} spot={spot} />
          ))}
        </div>
      )}

      <p className="community-hotspots__coming-soon">
        Source: Community traveler reviews + Amap/Meituan integration (coming soon)
      </p>
    </div>
  );
}
