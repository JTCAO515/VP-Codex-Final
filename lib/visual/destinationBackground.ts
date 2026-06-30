export interface DestinationScene {
  id: "default-ink" | "beijing-imperial" | "shanghai-jiangnan" | "jiangnan-lake" | "mountain-river";
  label: string;
  cssValue: string;
}

const scenes: DestinationScene[] = [
  {
    id: "beijing-imperial",
    label: "Beijing imperial ink",
    cssValue: "beijing-imperial",
  },
  {
    id: "shanghai-jiangnan",
    label: "Shanghai river and garden ink",
    cssValue: "shanghai-jiangnan",
  },
  {
    id: "jiangnan-lake",
    label: "Jiangnan lake ink",
    cssValue: "jiangnan-lake",
  },
  {
    id: "mountain-river",
    label: "Mountain river ink",
    cssValue: "mountain-river",
  },
  {
    id: "default-ink",
    label: "China ink landscape",
    cssValue: "default-ink",
  },
];

const destinationSceneMap: Array<{ sceneId: DestinationScene["id"]; matches: string[] }> = [
  { sceneId: "beijing-imperial", matches: ["beijing"] },
  { sceneId: "shanghai-jiangnan", matches: ["shanghai"] },
  { sceneId: "jiangnan-lake", matches: ["hangzhou", "suzhou"] },
  { sceneId: "mountain-river", matches: ["chongqing"] },
];

export function getDestinationScene(destinations: string[]): DestinationScene {
  const normalized = destinations.map((destination) => destination.trim().toLowerCase()).filter(Boolean);
  const match = destinationSceneMap.find((entry) =>
    entry.matches.some((candidate) => normalized.some((destination) => destination.includes(candidate))),
  );

  return scenes.find((scene) => scene.id === match?.sceneId) ?? scenes[scenes.length - 1];
}
