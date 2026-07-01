export interface PandaAvatar {
  id: string;
  name: string;
  src: string;
}

export const PANDA_AVATAR_STORAGE_KEY = "visepanda:selected-avatar";

export const pandaAvatars: PandaAvatar[] = [
  { id: "bamboo-hat", name: "Bamboo Hat", src: "/avatars/panda-orbit-blue.png" },
  { id: "red-scarf", name: "Red Scarf", src: "/avatars/panda-medic-silver.png" },
  { id: "compass", name: "Compass Panda", src: "/avatars/panda-halo-cyan.png" },
  { id: "tea-cup", name: "Tea Panda", src: "/avatars/panda-aurora.png" },
  { id: "backpack", name: "Backpack Panda", src: "/avatars/panda-line-tech.png" },
  { id: "ink-brush", name: "Ink Brush Panda", src: "/avatars/panda-cyan-guard.png" },
];

export function getPandaAvatar(id?: string | null): PandaAvatar {
  return pandaAvatars.find((avatar) => avatar.id === id) ?? pandaAvatars[0];
}
