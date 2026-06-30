"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { getPandaAvatar, PANDA_AVATAR_STORAGE_KEY } from "@/lib/account/avatars";
import { mockPhotos } from "@/lib/community/mockData";
import { getMemberTier } from "@/lib/community/membership";
import type { CommunityPhoto } from "@/lib/community/types";

const COMMUNITY_PHOTOS_KEY = "visepanda:community-photos";
const COMMUNITY_PHOTO_LIKES_KEY = "visepanda:community-photo-likes";

function readJson<T>(key: string, fallback: T): T {
  if (typeof window === "undefined") return fallback;
  try {
    const raw = window.localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

function createPhotoAuthor() {
  const avatarId = typeof window === "undefined" ? null : window.localStorage.getItem(PANDA_AVATAR_STORAGE_KEY);
  return {
    id: "local-user",
    displayName: "You",
    avatarInitials: "VP",
    avatarId: getPandaAvatar(avatarId).id,
    country: "🌏",
    memberTierId: "panda-explorer" as const,
  };
}

function PhotoCard({ photo, liked, onToggleLike }: { photo: CommunityPhoto; liked: boolean; onToggleLike: (photoId: string) => void }) {
  const avatar = getPandaAvatar(photo.author.avatarId);
  const tier = getMemberTier(photo.author.memberTierId);

  return (
    <article className="community-photo-card" aria-label={photo.caption}>
      <div className="community-photo-card__image" aria-hidden="true">
        <span className="community-photo-card__emoji">{photo.emoji}</span>
        <span className="community-photo-card__location">{photo.locationName}</span>
      </div>
      <div className="community-photo-card__info">
        <p className="community-photo-card__caption">{photo.caption}</p>
        <div className="community-photo-card__footer">
          <img alt="" className="community-photo-card__avatar-img" src={avatar.src} />
          <span className="community-photo-card__author">
            {photo.author.country} {photo.author.displayName}
          </span>
          <span className="community-photo-card__tier">{tier.shortName}</span>
          <span className="community-photo-card__city">{photo.city}</span>
          <button
            className={`community-photo-card__like${liked ? " active" : ""}`}
            type="button"
            aria-label="Like photo"
            onClick={() => onToggleLike(photo.id)}
          >
            Heart {photo.likes + (liked ? 1 : 0)}
          </button>
        </div>
      </div>
    </article>
  );
}

export function CommunityPhotos() {
  const [localPhotos, setLocalPhotos] = useState<CommunityPhoto[]>([]);
  const [likedPhotoIds, setLikedPhotoIds] = useState<Set<string>>(new Set());
  const [composerOpen, setComposerOpen] = useState(false);
  const [caption, setCaption] = useState("");
  const [city, setCity] = useState("Beijing");
  const [locationName, setLocationName] = useState("");
  const [emoji, setEmoji] = useState("📸");

  useEffect(() => {
    setLocalPhotos(readJson<CommunityPhoto[]>(COMMUNITY_PHOTOS_KEY, []));
    setLikedPhotoIds(new Set(readJson<string[]>(COMMUNITY_PHOTO_LIKES_KEY, [])));
  }, []);

  const photos = useMemo(() => [...localPhotos, ...mockPhotos], [localPhotos]);

  function toggleLike(photoId: string) {
    setLikedPhotoIds((current) => {
      const next = new Set(current);
      next.has(photoId) ? next.delete(photoId) : next.add(photoId);
      window.localStorage.setItem(COMMUNITY_PHOTO_LIKES_KEY, JSON.stringify([...next]));
      return next;
    });
  }

  function handlePhotoSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!caption.trim() || !locationName.trim()) return;
    const nextPhoto: CommunityPhoto = {
      id: `local-photo-${Date.now()}`,
      author: createPhotoAuthor(),
      caption: caption.trim(),
      city,
      locationName: locationName.trim(),
      emoji: emoji.trim() || "📸",
      likes: 0,
      createdAt: "Just now",
    };
    const nextPhotos = [nextPhoto, ...localPhotos];
    setLocalPhotos(nextPhotos);
    window.localStorage.setItem(COMMUNITY_PHOTOS_KEY, JSON.stringify(nextPhotos));
    setCaption("");
    setLocationName("");
    setEmoji("📸");
    setComposerOpen(false);
  }

  return (
    <div className="community-photos">
      <div className="community-photos__header">
        <p className="community-photos__subtitle">Travel photo wall · local MVP</p>
        <button className="community-photos__upload-btn" type="button" onClick={() => setComposerOpen((value) => !value)}>
          {composerOpen ? "Close photo form" : "+ Add Photo Card"}
        </button>
      </div>

      {composerOpen && (
        <form className="community-photo-form" onSubmit={handlePhotoSubmit}>
          <label>
            Caption
            <input value={caption} onChange={(event) => setCaption(event.target.value)} placeholder="Morning mist over West Lake" />
          </label>
          <label>
            City
            <select value={city} onChange={(event) => setCity(event.target.value)}>
              {["Beijing", "Shanghai", "Chengdu", "Hangzhou", "Xi'an", "Chongqing"].map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
          <label>
            Place
            <input value={locationName} onChange={(event) => setLocationName(event.target.value)} placeholder="The Bund" />
          </label>
          <label>
            Cover mark
            <input maxLength={2} value={emoji} onChange={(event) => setEmoji(event.target.value)} />
          </label>
          <button disabled={!caption.trim() || !locationName.trim()} type="submit">
            Publish photo card
          </button>
        </form>
      )}

      <div className="community-photos__grid" aria-label="Community photos">
        {photos.map((photo) => (
          <PhotoCard key={photo.id} liked={likedPhotoIds.has(photo.id)} onToggleLike={toggleLike} photo={photo} />
        ))}
      </div>

      <p className="community-photos__coming-soon">
        Real image upload, cropping, storage, and moderation are planned for Supabase Storage. This MVP publishes local photo cards only.
      </p>
    </div>
  );
}
