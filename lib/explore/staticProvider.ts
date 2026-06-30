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
  {
    id: "guangzhou",
    name: "Guangzhou",
    region: "South China",
    tagline: "Cantonese food, river views, old trading history, and easy Greater Bay links.",
    bestFor: ["Food travelers", "Business travelers", "Warm weather"],
  },
  {
    id: "hangzhou",
    name: "Hangzhou",
    region: "East China",
    tagline: "West Lake scenery, tea fields, temples, and relaxed day trips from Shanghai.",
    bestFor: ["Scenic walks", "Tea culture", "Couples"],
  },
  {
    id: "suzhou",
    name: "Suzhou",
    region: "East China",
    tagline: "Classical gardens, canals, silk history, and soft Jiangnan pacing.",
    bestFor: ["Gardens", "Photography", "Slow travel"],
  },
  {
    id: "chongqing",
    name: "Chongqing",
    region: "Southwest China",
    tagline: "Mountain-city views, hotpot, river nightscapes, and dramatic urban layers.",
    bestFor: ["Food travelers", "Night views", "Urban explorers"],
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
  { id: "guangzhou-chen-clan", cityId: "guangzhou", name: "Chen Clan Ancestral Hall", category: "Heritage", description: "Intricate Lingnan architecture with carved wood, brick, and ceramic details." },
  { id: "guangzhou-canton-tower", cityId: "guangzhou", name: "Canton Tower", category: "Landmark", description: "A skyline icon with Pearl River views, best around sunset or after dark." },
  { id: "hangzhou-west-lake", cityId: "hangzhou", name: "West Lake", category: "Scenic", description: "Hangzhou's classic lakefront for walking, cycling, boats, and temple-side views." },
  { id: "hangzhou-longjing", cityId: "hangzhou", name: "Longjing Tea Village", category: "Tea culture", description: "Green tea terraces and village lanes west of the lake." },
  { id: "suzhou-humble-administrator", cityId: "suzhou", name: "Humble Administrator's Garden", category: "Garden", description: "A landmark classical garden with ponds, pavilions, and layered views." },
  { id: "suzhou-pingjiang", cityId: "suzhou", name: "Pingjiang Road", category: "Canal street", description: "A canal-side historic street for easy walks, snacks, and small shops." },
  { id: "chongqing-hongya", cityId: "chongqing", name: "Hongya Cave", category: "Night view", description: "Layered riverside architecture that glows at night near Jiefangbei." },
  { id: "chongqing-ciqikou", cityId: "chongqing", name: "Ciqikou Ancient Town", category: "Old town", description: "Historic lanes, snacks, teahouses, and a slower break from downtown hills." },
];

const foodSpots: ExploreFoodSpot[] = [
  { id: "beijing-peking-duck", cityId: "beijing", name: "Da Dong Roast Duck", dish: "Peking duck", description: "A well-known spot for Beijing's signature roast duck." },
  { id: "shanghai-xiaolongbao", cityId: "shanghai", name: "Din Tai Fung", dish: "Xiaolongbao", description: "Soup dumplings done with precision." },
  { id: "chengdu-hotpot", cityId: "chengdu", name: "Shu Jiu Xiang Hotpot", dish: "Sichuan hotpot", description: "Spicy, numbing hotpot in a classic Chengdu setting." },
  { id: "xian-noodles", cityId: "xian", name: "De Fa Chang", dish: "Biang biang noodles", description: "Hand-pulled wide noodles, a Xi'an street food staple." },
  { id: "guangzhou-dim-sum", cityId: "guangzhou", name: "Tao Tao Ju", dish: "Dim sum", description: "Classic Cantonese tea-house dining with a broad dim sum menu." },
  { id: "hangzhou-louwailou", cityId: "hangzhou", name: "Lou Wai Lou", dish: "West Lake fish", description: "A historic lakeside restaurant for Hangzhou classics." },
  { id: "suzhou-noodles", cityId: "suzhou", name: "Tong De Xing", dish: "Suzhou noodles", description: "Seasonal noodle bowls with a delicate Jiangnan style." },
  { id: "chongqing-hotpot", cityId: "chongqing", name: "Pei Jie Hotpot", dish: "Chongqing hotpot", description: "Bold, spicy hotpot close to the city's signature flavor." },
];

const stays: ExploreStay[] = [
  { id: "beijing-wangfujing", cityId: "beijing", name: "Wangfujing area", area: "Dongcheng District", description: "Central, walkable, close to the Forbidden City." },
  { id: "shanghai-jingan", cityId: "shanghai", name: "Jing'an area", area: "Jing'an District", description: "Convenient metro access and a mix of local and international dining." },
  { id: "chengdu-kuanzhai-stay", cityId: "chengdu", name: "Kuanzhai Alley area", area: "Qingyang District", description: "Walkable old-town base close to teahouses and food streets." },
  { id: "xian-bell-tower", cityId: "xian", name: "Bell Tower area", area: "Beilin District", description: "Central base inside the city wall, close to the Muslim Quarter." },
  { id: "guangzhou-tianhe", cityId: "guangzhou", name: "Tianhe area", area: "Tianhe District", description: "Business-friendly base with malls, metro access, and easy dining." },
  { id: "hangzhou-west-lake-stay", cityId: "hangzhou", name: "West Lake east shore", area: "Shangcheng District", description: "Convenient for lake walks, shopping streets, and first-time stays." },
  { id: "suzhou-gusu", cityId: "suzhou", name: "Gusu old town", area: "Gusu District", description: "Best for gardens, canals, and short taxi or walking distances." },
  { id: "chongqing-jiefangbei", cityId: "chongqing", name: "Jiefangbei area", area: "Yuzhong District", description: "Central for night views, food streets, metro access, and riverfront walks." },
];

export function createStaticExploreProvider(): ExploreProvider {
  return {
    id: "static",
    async getProviderStatus() {
      return {
        id: "static-explore",
        label: "Static curated provider",
        mode: "static",
        coverage: "8 cities with curated attractions, food, and stay areas.",
        candidates: ["Amap", "Trip.com", "Meituan", "Tripadvisor"],
        nextIntegration: "POI search and place-detail verification should be validated first.",
        limitations: [
          "No live opening hours, ticket availability, booking inventory, or map routing yet.",
          "Static content is written for information architecture and itinerary-planning tests.",
        ],
      };
    },
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
