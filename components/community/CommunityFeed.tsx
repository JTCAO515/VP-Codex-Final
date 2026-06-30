"use client";

import { mockPosts } from "@/lib/community/mockData";
import type { CommunityPost } from "@/lib/community/types";

function PostCard({ post }: { post: CommunityPost }) {
  return (
    <article className="community-post-card" aria-label={post.title}>
      <div className="community-post-card__cover" aria-hidden="true">
        {post.coverEmoji}
      </div>
      <div className="community-post-card__body">
        <div className="community-post-card__meta">
          <span className="community-post-card__avatar" aria-hidden="true">
            {post.author.avatarInitials}
          </span>
          <span className="community-post-card__author">
            {post.author.country} {post.author.displayName}
          </span>
          <span className="community-post-card__date">{post.createdAt}</span>
          {post.type === "tip" && <span className="community-post-card__type-badge">💡 Tip</span>}
          {post.type === "trip" && <span className="community-post-card__type-badge">🗺️ Trip</span>}
        </div>

        <h3 className="community-post-card__title">{post.title}</h3>

        <div className="community-post-card__cities">
          {post.cities.map((city) => (
            <span className="community-post-card__city-tag" key={city}>{city}</span>
          ))}
          {post.daysCount && (
            <span className="community-post-card__city-tag">{post.daysCount} days</span>
          )}
        </div>

        <p className="community-post-card__excerpt">{post.body.slice(0, 180)}…</p>

        <div className="community-post-card__tags">
          {post.tags.map((tag) => (
            <span className="community-post-card__tag" key={tag}>#{tag}</span>
          ))}
        </div>

        <div className="community-post-card__footer">
          <button className="community-post-card__like-btn" type="button" aria-label="Like this post">
            ♡ {post.likes}
          </button>
          <span className="community-post-card__comments">💬 {post.comments}</span>
          <button className="community-post-card__read-btn" type="button">
            读更多 Read more →
          </button>
        </div>
      </div>
    </article>
  );
}

export function CommunityFeed() {
  return (
    <div className="community-feed">
      <div className="community-feed__header">
        <p className="community-feed__subtitle">旅行者分享 · 真实体验</p>
        <button className="community-feed__share-btn" type="button">
          + 分享我的行程 Share My Trip
        </button>
      </div>

      <div className="community-feed__list" aria-label="Community posts">
        {mockPosts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
      </div>

      <p className="community-feed__coming-soon">
        登录后可以发布行程、上传照片和点赞 · Sign in to post trips, upload photos, and like
      </p>
    </div>
  );
}
