import type { ExploreAttraction, ExploreCity, ExploreFoodSpot, ExploreProvider, ExploreStay } from "@/lib/explore/types";

const cities: ExploreCity[] = [
  {
    id: "beijing",
    name: "Beijing",
    region: "North China",
    tagline: "Imperial history, the Great Wall, and wide boulevards.",
    bestFor: ["First-time visitors", "History lovers", "Architecture"],
  },
  {
    id: "shanghai",
    name: "Shanghai",
    region: "East China",
    tagline: "Riverfront skyline, French Concession streets, and global dining.",
    bestFor: ["Business travelers", "Nightlife", "Shopping"],
  },
  {
    id: "chengdu",
    name: "Chengdu",
    region: "Southwest China",
    tagline: "Sichuan food, pandas, and a slower pace of life.",
    bestFor: ["Food travelers", "Solo travelers", "Relaxed pace"],
  },
  {
    id: "xian",
    name: "Xi'an",
    region: "Northwest China",
    tagline: "Ancient city walls and the Terracotta Army.",
    bestFor: ["History lovers", "Day-trip planners"],
  },
];

const attractions: ExploreAttraction[] = [
  { id: "beijing-forbidden-city", cityId: "beijing", name: "Forbidden City", category: "Heritage", description: "The former imperial palace at the heart of Beijing." },
  { id: "beijing-great-wall", cityId: "beijing", name: "Great Wall (Mutianyu)", category: "Heritage", description: "A well-restored, visitor-friendly section of the Great Wall." },
  { id: "shanghai-the-bund", cityId: "shanghai", name: "The Bund", category: "Landmark", description: "Riverside promenade facing Shanghai's modern skyline." },
  { id: "shanghai-yu-garden", cityId: "shanghai", name: "Yu Garden", category: "Heritage", description: "Classical Ming-dynasty garden in the old city." },
  { id: "chengdu-panda-base", cityId: "chengdu", name: "Chengdu Research Base of Giant Panda Breeding", category: "Wildlife", description: "Home to giant pandas in a forested research setting." },
  { id: "chengdu-kuanzhai", cityId: "chengdu", name: "Kuanzhai Alley", category: "Heritage", description: "Restored Qing-dynasty lanes with teahouses and shops." },
  { id: "xian-terracotta-army", cityId: "xian", name: "Terracotta Army", category: "Heritage", description: "Thousands of life-sized terracotta soldiers guarding an ancient emperor's tomb." },
  { id: "xian-city-wall", cityId: "xian", name: "Xi'an City Wall", category: "Heritage", description: "One of China's best-preserved ancient city walls, open for walking and cycling." },
];

const foodSpots: ExploreFoodSpot[] = [
  { id: "beijing-peking-duck", cityId: "beijing", name: "Da Dong Roast Duck", dish: "Peking duck", description: "A well-known spot for Beijing's signature roast duck." },
  { id: "shanghai-xiaolongbao", cityId: "shanghai", name: "Din Tai Fung", dish: "Xiaolongbao", description: "Soup dumplings done with precision." },
  { id: "chengdu-hotpot", cityId: "chengdu", name: "Shu Jiu Xiang Hotpot", dish: "Sichuan hotpot", description: "Spicy, numbing hotpot in a classic Chengdu setting." },
  { id: "xian-noodles", cityId: "xian", name: "De Fa Chang", dish: "Biang biang noodles", description: "Hand-pulled wide noodles, a Xi'an street food staple." },
];

const stays: ExploreStay[] = [
  { id: "beijing-wangfujing", cityId: "beijing", name: "Wangfujing area", area: "Dongcheng District", description: "Central, walkable, close to the Forbidden City." },
  { id: "shanghai-jingan", cityId: "shanghai", name: "Jing'an area", area: "Jing'an District", description: "Convenient metro access and a mix of local and international dining." },
  { id: "chengdu-kuanzhai-stay", cityId: "chengdu", name: "Kuanzhai Alley area", area: "Qingyang District", description: "Walkable old-town base close to teahouses and food streets." },
  { id: "xian-bell-tower", cityId: "xian", name: "Bell Tower area", area: "Beilin District", description: "Central base inside the city wall, close to the Muslim Quarter." },
];

export function createStaticExploreProvider(): ExploreProvider {
  return {
    id: "static",
    async listCities() {
      return cities;
    },
    async listAttractions(cityId: string) {
      return attractions.filter((attraction) => attraction.cityId === cityId);
    },
    async listFoodSpots(cityId: string) {
      return foodSpots.filter((spot) => spot.cityId === cityId);
    },
    async listStays(cityId: string) {
      return stays.filter((stay) => stay.cityId === cityId);
    },
  };
}
