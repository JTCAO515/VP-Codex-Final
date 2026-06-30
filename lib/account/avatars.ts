export interface PandaAvatar {
  id: string;
  name: string;
  src: string;
}

export const PANDA_AVATAR_STORAGE_KEY = "visepanda:selected-avatar";

export const pandaAvatars: PandaAvatar[] = [
  { id: "bamboo-hat", name: "Bamboo Hat", src: "/avatars/panda-bamboo-hat.svg" },
  { id: "red-scarf", name: "Red Scarf", src: "/avatars/panda-red-scarf.svg" },
  { id: "compass", name: "Compass Panda", src: "/avatars/panda-compass.svg" },
  { id: "tea-cup", name: "Tea Panda", src: "/avatars/panda-tea.svg" },
  { id: "backpack", name: "Backpack Panda", src: "/avatars/panda-backpack.svg" },
  { id: "ink-brush", name: "Ink Brush Panda", src: "/avatars/panda-ink-brush.svg" },
];

export function getPandaAvatar(id?: string | null): PandaAvatar {
  return pandaAvatars.find((avatar) => avatar.id === id) ?? pandaAvatars[0];
}
