"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { getPandaAvatar, PANDA_AVATAR_STORAGE_KEY } from "@/lib/account/avatars";
import { mockPosts } from "@/lib/community/mockData";
import { getMemberTier } from "@/lib/community/membership";
import type { CommunityComment, CommunityPost, CommunityPostType } from "@/lib/community/types";

const COMMUNITY_POSTS_KEY = "visepanda:community-posts";
const COMMUNITY_LIKES_KEY = "visepanda:community-likes";
const COMMUNITY_SAVES_KEY = "visepanda:community-saves";
const COMMUNITY_COMMENTS_KEY = "visepanda:community-comments";

type TypeFilter = "all" | CommunityPostType;

const typeFilters: Array<{ key: TypeFilter; label: string }> = [
  { key: "all", label: "All" },
  { key: "trip", label: "Trips" },
  { key: "tip", label: "Tips" },
  { key: "question", label: "Questions" },
];

const cityFilters = ["All cities", "Beijing", "Shanghai", "Chengdu", "Hangzhou", "Xi'an", "Chongqing"];

function readJson<T>(key: string, fallback: T): T {
  if (typeof window === "undefined") return fallback;
  try {
    const raw = window.localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

function createLocalAuthor() {
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

function PostCard({
  post,
  liked,
  saved,
  comments,
  onToggleLike,
  onToggleSave,
  onAddComment,
}: {
  post: CommunityPost;
  liked: boolean;
  saved: boolean;
  comments: CommunityComment[];
  onToggleLike: (postId: string) => void;
  onToggleSave: (postId: string) => void;
  onAddComment: (postId: string, body: string) => void;
}) {
  const [expanded, setExpanded] = useState(false);
  const [comment, setComment] = useState("");
  const tier = getMemberTier(post.author.memberTierId);
  const avatar = getPandaAvatar(post.author.avatarId);
  const visibleBody = expanded ? post.body : `${post.body.slice(0, 170)}${post.body.length > 170 ? "..." : ""}`;
  const likeCount = post.likes + (liked ? 1 : 0);
  const saveCount = (post.saved ?? 0) + (saved ? 1 : 0);

  function handleCommentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!comment.trim()) return;
    onAddComment(post.id, comment.trim());
    setComment("");
    setExpanded(true);
  }

  return (
    <article className="community-post-card" aria-label={post.title}>
      <div className="community-post-card__cover" aria-hidden="true">
        {post.coverEmoji}
      </div>
      <div className="community-post-card__body">
        <div className="community-post-card__meta">
          <img alt="" className="community-post-card__avatar-img" src={avatar.src} />
          <span className="community-post-card__author">
            {post.author.country} {post.author.displayName}
          </span>
          <span className="community-post-card__tier">{tier.shortName}</span>
          <span className="community-post-card__date">{post.createdAt}</span>
          <span className="community-post-card__type-badge">{post.type}</span>
        </div>

        <h3 className="community-post-card__title">{post.title}</h3>

        <div className="community-post-card__cities">
          {post.cities.map((city) => (
            <span className="community-post-card__city-tag" key={city}>
              {city}
            </span>
          ))}
          {post.daysCount && <span className="community-post-card__city-tag">{post.daysCount} days</span>}
        </div>

        <p className="community-post-card__excerpt">{visibleBody}</p>

        <div className="community-post-card__tags">
          {post.tags.map((tag) => (
            <span className="community-post-card__tag" key={tag}>
              #{tag}
            </span>
          ))}
        </div>

        <div className="community-post-card__footer">
          <button
            className={`community-post-card__like-btn${liked ? " active" : ""}`}
            type="button"
            aria-label="Like this post"
            onClick={() => onToggleLike(post.id)}
          >
            Heart {likeCount}
          </button>
          <button
            className={`community-post-card__save-btn${saved ? " active" : ""}`}
            type="button"
            aria-label="Save post"
            onClick={() => onToggleSave(post.id)}
          >
            Save {saveCount}
          </button>
          <span className="community-post-card__comments">Comments {post.comments + comments.length}</span>
          <button className="community-post-card__read-btn" type="button" onClick={() => setExpanded((value) => !value)}>
            {expanded ? "Close" : "Read more"}
          </button>
        </div>

        {expanded && (
          <div className="community-post-card__detail">
            <p>
              Member trust: {tier.name}. {tier.audience}.
            </p>
            {comments.length > 0 && (
              <div className="community-post-card__comment-list" aria-label={`Comments for ${post.title}`}>
                {comments.map((item) => (
                  <p key={item.id}>
                    <strong>{item.author.displayName}:</strong> {item.body}
                  </p>
                ))}
              </div>
            )}
          </div>
        )}

        <form className="community-post-card__comment-form" onSubmit={handleCommentSubmit}>
          <input
            aria-label={`Comment on ${post.title}`}
            onChange={(event) => setComment(event.target.value)}
            placeholder="Add a useful note..."
            value={comment}
          />
          <button disabled={!comment.trim()} type="submit">
            Comment
          </button>
        </form>
      </div>
    </article>
  );
}

export function CommunityFeed() {
  const [localPosts, setLocalPosts] = useState<CommunityPost[]>([]);
  const [likedPostIds, setLikedPostIds] = useState<Set<string>>(new Set());
  const [savedPostIds, setSavedPostIds] = useState<Set<string>>(new Set());
  const [commentsByPost, setCommentsByPost] = useState<Record<string, CommunityComment[]>>({});
  const [typeFilter, setTypeFilter] = useState<TypeFilter>("all");
  const [cityFilter, setCityFilter] = useState("All cities");
  const [composerOpen, setComposerOpen] = useState(false);
  const [draftTitle, setDraftTitle] = useState("");
  const [draftCity, setDraftCity] = useState("Shanghai");
  const [draftBody, setDraftBody] = useState("");
  const [draftTags, setDraftTags] = useState("food, culture");
  const [draftType, setDraftType] = useState<CommunityPostType>("trip");

  useEffect(() => {
    setLocalPosts(readJson<CommunityPost[]>(COMMUNITY_POSTS_KEY, []));
    setLikedPostIds(new Set(readJson<string[]>(COMMUNITY_LIKES_KEY, [])));
    setSavedPostIds(new Set(readJson<string[]>(COMMUNITY_SAVES_KEY, [])));
    setCommentsByPost(readJson<Record<string, CommunityComment[]>>(COMMUNITY_COMMENTS_KEY, {}));
  }, []);

  const posts = useMemo(() => {
    return [...localPosts, ...mockPosts].filter((post) => {
      const typeMatches = typeFilter === "all" || post.type === typeFilter;
      const cityMatches = cityFilter === "All cities" || post.cities.includes(cityFilter);
      return typeMatches && cityMatches;
    });
  }, [cityFilter, localPosts, typeFilter]);

  function persistSet(key: string, next: Set<string>) {
    window.localStorage.setItem(key, JSON.stringify([...next]));
  }

  function toggleLike(postId: string) {
    setLikedPostIds((current) => {
      const next = new Set(current);
      next.has(postId) ? next.delete(postId) : next.add(postId);
      persistSet(COMMUNITY_LIKES_KEY, next);
      return next;
    });
  }

  function toggleSave(postId: string) {
    setSavedPostIds((current) => {
      const next = new Set(current);
      next.has(postId) ? next.delete(postId) : next.add(postId);
      persistSet(COMMUNITY_SAVES_KEY, next);
      return next;
    });
  }

  function addComment(postId: string, body: string) {
    setCommentsByPost((current) => {
      const next = {
        ...current,
        [postId]: [
          ...(current[postId] ?? []),
          {
            id: `comment-${Date.now()}`,
            postId,
            author: createLocalAuthor(),
            body,
            createdAt: "Just now",
          },
        ],
      };
      window.localStorage.setItem(COMMUNITY_COMMENTS_KEY, JSON.stringify(next));
      return next;
    });
  }

  function handlePostSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!draftTitle.trim() || !draftBody.trim()) return;
    const nextPost: CommunityPost = {
      id: `local-post-${Date.now()}`,
      type: draftType,
      author: createLocalAuthor(),
      title: draftTitle.trim(),
      body: draftBody.trim(),
      cities: [draftCity],
      daysCount: draftType === "trip" ? 5 : undefined,
      coverEmoji: draftType === "question" ? "?" : draftType === "tip" ? "!" : "VP",
      likes: 0,
      comments: 0,
      saved: 0,
      createdAt: "Just now",
      tags: draftTags
        .split(",")
        .map((tag) => tag.trim())
        .filter(Boolean),
    };
    const nextPosts = [nextPost, ...localPosts];
    setLocalPosts(nextPosts);
    window.localStorage.setItem(COMMUNITY_POSTS_KEY, JSON.stringify(nextPosts));
    setDraftTitle("");
    setDraftBody("");
    setDraftTags("food, culture");
    setComposerOpen(false);
  }

  return (
    <div className="community-feed">
      <div className="community-feed__header">
        <p className="community-feed__subtitle">Traveler stories · useful China tips</p>
        <button className="community-feed__share-btn" type="button" onClick={() => setComposerOpen((value) => !value)}>
          {composerOpen ? "Close composer" : "+ Share My Trip"}
        </button>
      </div>

      {composerOpen && (
        <form className="community-composer" onSubmit={handlePostSubmit}>
          <div className="community-composer__row">
            <label>
              Type
              <select value={draftType} onChange={(event) => setDraftType(event.target.value as CommunityPostType)}>
                <option value="trip">Trip</option>
                <option value="tip">Tip</option>
                <option value="question">Question</option>
              </select>
            </label>
            <label>
              City
              <select value={draftCity} onChange={(event) => setDraftCity(event.target.value)}>
                {cityFilters.slice(1).map((city) => (
                  <option key={city} value={city}>
                    {city}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <label>
            Title
            <input value={draftTitle} onChange={(event) => setDraftTitle(event.target.value)} placeholder="A relaxed Shanghai food route" />
          </label>
          <label>
            Story
            <textarea value={draftBody} onChange={(event) => setDraftBody(event.target.value)} placeholder="What should other travelers know?" />
          </label>
          <label>
            Tags
            <input value={draftTags} onChange={(event) => setDraftTags(event.target.value)} placeholder="food, family, slow-travel" />
          </label>
          <button disabled={!draftTitle.trim() || !draftBody.trim()} type="submit">
            Publish locally
          </button>
        </form>
      )}

      <div className="community-feed__filters" aria-label="Community filters">
        {typeFilters.map((item) => (
          <button
            className={typeFilter === item.key ? "active" : ""}
            key={item.key}
            onClick={() => setTypeFilter(item.key)}
            type="button"
          >
            {item.label}
          </button>
        ))}
        <select aria-label="Filter by city" value={cityFilter} onChange={(event) => setCityFilter(event.target.value)}>
          {cityFilters.map((city) => (
            <option key={city} value={city}>
              {city}
            </option>
          ))}
        </select>
      </div>

      <div className="community-feed__list" aria-label="Community posts">
        {posts.map((post) => (
          <PostCard
            comments={commentsByPost[post.id] ?? []}
            key={post.id}
            liked={likedPostIds.has(post.id)}
            onAddComment={addComment}
            onToggleLike={toggleLike}
            onToggleSave={toggleSave}
            post={post}
            saved={savedPostIds.has(post.id)}
          />
        ))}
      </div>

      <p className="community-feed__coming-soon">
        Current MVP stores community actions locally. Supabase posts, media, likes, comments, and moderation are planned next.
      </p>
    </div>
  );
}
