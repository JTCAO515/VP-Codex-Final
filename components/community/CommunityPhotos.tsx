"use client";

import { mockPhotos } from "@/lib/community/mockData";
import type { CommunityPhoto } from "@/lib/community/types";

function PhotoCard({ photo }: { photo: CommunityPhoto }) {
  return (
    <article className="community-photo-card" aria-label={photo.caption}>
      <div className="community-photo-card__image" aria-hidden="true">
        <span className="community-photo-card__emoji">{photo.emoji}</span>
        <span className="community-photo-card__location">{photo.locationName}</span>
      </div>
      <div className="community-photo-card__info">
        <p className="community-photo-card__caption">{photo.caption}</p>
        <div className="community-photo-card__footer">
          <span className="community-photo-card__avatar" aria-hidden="true">
            {photo.author.avatarInitials}
          </span>
          <span className="community-photo-card__author">
            {photo.author.country} {photo.author.displayName}
          </span>
          <span className="community-photo-card__city">{photo.city}</span>
          <button className="community-photo-card__like" type="button" aria-label="Like photo">
            ♡ {photo.likes}
          </button>
        </div>
      </div>
    </article>
  );
}

export function CommunityPhotos() {
  return (
    <div className="community-photos">
      <div className="community-photos__header">
        <p className="community-photos__subtitle">旅途照片 · 真实风景</p>
        <button className="community-photos__upload-btn" type="button">
          + 上传照片 Upload Photo
        </button>
      </div>

      <div className="community-photos__grid" aria-label="Community photos">
        {mockPhotos.map((photo) => (
          <PhotoCard key={photo.id} photo={photo} />
        ))}
      </div>

      <p className="community-photos__coming-soon">
        照片上传功能即将开放 · Photo upload coming soon
      </p>
    </div>
  );
}
