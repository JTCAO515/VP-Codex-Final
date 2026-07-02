"use client";

import { useLayoutEffect, useRef } from "react";

/**
 * Attach the returned ref to a DOM node to replay a CSS animation class every
 * time `trigger` changes — including consecutive changes to the same day that
 * would otherwise not restart a CSS animation because the class name never
 * actually toggles off. Uses the standard "remove class, force reflow,
 * re-add class" technique instead of remounting the node (which would lose
 * scroll-ref identity and any other local DOM state).
 */
export function useReplayableAnimation<T extends HTMLElement>(trigger: number, animationClass: string) {
  const ref = useRef<T | null>(null);
  const isFirstRun = useRef(true);

  useLayoutEffect(() => {
    if (isFirstRun.current) {
      isFirstRun.current = false;
      return;
    }
    const el = ref.current;
    if (!el) return;
    el.classList.remove(animationClass);
    // Reading offsetWidth forces a synchronous reflow, so the browser treats
    // the class re-add below as a fresh animation start rather than a no-op.
    void el.offsetWidth;
    el.classList.add(animationClass);
  }, [trigger, animationClass]);

  return ref;
}
