import type { Phrase, SpecialTerm } from "@/lib/translate/types";

export const phrases: Phrase[] = [
  // Greetings
  { id: "g1", category: "greetings", english: "Hello", chinese: "你好", pinyin: "nǐ hǎo" },
  { id: "g2", category: "greetings", english: "Thank you", chinese: "谢谢", pinyin: "xiè xie" },
  { id: "g3", category: "greetings", english: "Sorry / Excuse me", chinese: "对不起", pinyin: "duì bu qǐ" },
  { id: "g4", category: "greetings", english: "No problem", chinese: "没问题", pinyin: "méi wèn tí" },
  { id: "g5", category: "greetings", english: "I don't understand", chinese: "我听不懂", pinyin: "wǒ tīng bù dǒng" },
  { id: "g6", category: "greetings", english: "Do you speak English?", chinese: "你会说英文吗？", pinyin: "nǐ huì shuō yīng wén ma" },
  { id: "g7", category: "greetings", english: "Please speak slowly", chinese: "请说慢一点", pinyin: "qǐng shuō màn yī diǎn" },
  { id: "g8", category: "greetings", english: "Goodbye", chinese: "再见", pinyin: "zài jiàn" },

  // Dining
  { id: "d1", category: "dining", english: "I'm vegetarian", chinese: "我吃素", pinyin: "wǒ chī sù" },
  { id: "d2", category: "dining", english: "No spice please", chinese: "不要辣的", pinyin: "bù yào là de" },
  { id: "d3", category: "dining", english: "The bill please", chinese: "买单", pinyin: "mǎi dān" },
  { id: "d4", category: "dining", english: "Water please", chinese: "请给我水", pinyin: "qǐng gěi wǒ shuǐ" },
  { id: "d5", category: "dining", english: "Peanut allergy", chinese: "我对花生过敏", pinyin: "wǒ duì huā shēng guò mǐn", notes: "Show to restaurant staff" },
  { id: "d6", category: "dining", english: "No pork please", chinese: "不吃猪肉", pinyin: "bù chī zhū ròu" },
  { id: "d7", category: "dining", english: "Delicious!", chinese: "太好吃了！", pinyin: "tài hǎo chī le" },
  { id: "d8", category: "dining", english: "One more serving", chinese: "再来一份", pinyin: "zài lái yī fèn" },
  { id: "d9", category: "dining", english: "Takeaway please", chinese: "打包", pinyin: "dǎ bāo" },
  { id: "d10", category: "dining", english: "Is this seafood?", chinese: "这是海鲜吗？", pinyin: "zhè shì hǎi xiān ma" },

  // Transport
  { id: "t1", category: "transport", english: "Take me to this address", chinese: "带我去这个地址", pinyin: "dài wǒ qù zhè ge dì zhǐ", notes: "Show the address on screen" },
  { id: "t2", category: "transport", english: "How much?", chinese: "多少钱？", pinyin: "duō shǎo qián" },
  { id: "t3", category: "transport", english: "Too expensive", chinese: "太贵了", pinyin: "tài guì le" },
  { id: "t4", category: "transport", english: "Where is the metro?", chinese: "地铁站在哪里？", pinyin: "dì tiě zhàn zài nǎ lǐ" },
  { id: "t5", category: "transport", english: "Where is the bathroom?", chinese: "洗手间在哪里？", pinyin: "xǐ shǒu jiān zài nǎ lǐ" },
  { id: "t6", category: "transport", english: "I want to go to the airport", chinese: "我要去机场", pinyin: "wǒ yào qù jī chǎng" },
  { id: "t7", category: "transport", english: "Stop here please", chinese: "在这里停", pinyin: "zài zhè lǐ tíng" },
  { id: "t8", category: "transport", english: "Turn right / Turn left", chinese: "向右 / 向左", pinyin: "xiàng yòu / xiàng zuǒ" },

  // Shopping
  { id: "s1", category: "shopping", english: "How much does this cost?", chinese: "这个多少钱？", pinyin: "zhè ge duō shǎo qián" },
  { id: "s2", category: "shopping", english: "Can I try this on?", chinese: "可以试穿吗？", pinyin: "kě yǐ shì chuān ma" },
  { id: "s3", category: "shopping", english: "Do you have a smaller size?", chinese: "有小一号吗？", pinyin: "yǒu xiǎo yī hào ma" },
  { id: "s4", category: "shopping", english: "Can you give a discount?", chinese: "可以便宜一点吗？", pinyin: "kě yǐ pián yí yī diǎn ma" },
  { id: "s5", category: "shopping", english: "I'll take this one", chinese: "我要这个", pinyin: "wǒ yào zhè ge" },
  { id: "s6", category: "shopping", english: "Just looking, thanks", chinese: "我随便看看，谢谢", pinyin: "wǒ suí biàn kàn kàn, xiè xie" },

  // Emergency
  { id: "e1", category: "emergency", english: "Help!", chinese: "救命！", pinyin: "jiù mìng" },
  { id: "e2", category: "emergency", english: "Call the police", chinese: "叫警察", pinyin: "jiào jǐng chá" },
  { id: "e3", category: "emergency", english: "I need a doctor", chinese: "我需要看医生", pinyin: "wǒ xū yào kàn yī shēng" },
  { id: "e4", category: "emergency", english: "I lost my passport", chinese: "我护照丢了", pinyin: "wǒ hù zhào diū le" },
  { id: "e5", category: "emergency", english: "I was robbed", chinese: "我被抢劫了", pinyin: "wǒ bèi qiǎng jié le" },
  { id: "e6", category: "emergency", english: "Where is the nearest hospital?", chinese: "最近的医院在哪里？", pinyin: "zuì jìn de yī yuàn zài nǎ lǐ" },

  // Hotel
  { id: "h1", category: "hotel", english: "I have a reservation", chinese: "我有预订", pinyin: "wǒ yǒu yù dìng" },
  { id: "h2", category: "hotel", english: "Check-in please", chinese: "办理入住", pinyin: "bàn lǐ rù zhù" },
  { id: "h3", category: "hotel", english: "Check-out please", chinese: "办理退房", pinyin: "bàn lǐ tuì fáng" },
  { id: "h4", category: "hotel", english: "What is the Wi-Fi password?", chinese: "WiFi密码是多少？", pinyin: "WiFi mì mǎ shì duō shǎo" },
  { id: "h5", category: "hotel", english: "My room has a problem", chinese: "我的房间有问题", pinyin: "wǒ de fáng jiān yǒu wèn tí" },
  { id: "h6", category: "hotel", english: "Can I have an extra pillow?", chinese: "可以多给一个枕头吗？", pinyin: "kě yǐ duō gěi yī ge zhěn tóu ma" },
];

export const specialTerms: SpecialTerm[] = [
  // Attractions
  { id: "a1", termCategory: "attractions", english: "Forbidden City", chinese: "故宫", pinyin: "Gù Gōng", context: "Imperial palace in Beijing; buy tickets online in advance" },
  { id: "a2", termCategory: "attractions", english: "Great Wall", chinese: "长城", pinyin: "Cháng Chéng", context: "Mutianyu section recommended for first-timers; 1.5 hours from Beijing" },
  { id: "a3", termCategory: "attractions", english: "Temple of Heaven", chinese: "天坛", pinyin: "Tiān Tán", context: "Ming dynasty altar complex in Beijing; early morning crowds are locals doing tai chi" },
  { id: "a4", termCategory: "attractions", english: "West Lake", chinese: "西湖", pinyin: "Xī Hú", context: "Scenic lake in Hangzhou; best explored by walking, cycling, or rented boat" },
  { id: "a5", termCategory: "attractions", english: "The Bund", chinese: "外滩", pinyin: "Wài Tān", context: "Shanghai riverfront with colonial architecture; best at dusk for city light views" },
  { id: "a6", termCategory: "attractions", english: "Terracotta Army", chinese: "兵马俑", pinyin: "Bīng Mǎ Yǒng", context: "Thousands of life-sized clay soldiers near Xi'an; Pit 1 is the most impressive" },
  { id: "a7", termCategory: "attractions", english: "Panda Base", chinese: "大熊猫繁育研究基地", pinyin: "Dà Xióng Māo Jī Dì", context: "Giant panda research center in Chengdu; visit in the morning when pandas are most active" },
  { id: "a8", termCategory: "attractions", english: "Yu Garden", chinese: "豫园", pinyin: "Yù Yuán", context: "Classical Ming dynasty garden in Shanghai's old city; adjacent night market is equally famous" },

  // Dishes
  { id: "f1", termCategory: "dishes", english: "Peking Duck", chinese: "北京烤鸭", pinyin: "Běi Jīng Kǎo Yā", context: "Crispy-skin roast duck served with thin pancakes, hoisin, and spring onion" },
  { id: "f2", termCategory: "dishes", english: "Xiaolongbao", chinese: "小笼包", pinyin: "Xiǎo Lóng Bāo", context: "Soup dumplings with juicy broth inside; bite a small hole before eating to avoid burning your mouth" },
  { id: "f3", termCategory: "dishes", english: "Kung Pao Chicken", chinese: "宫保鸡丁", pinyin: "Gōng Bǎo Jī Dīng", context: "Spicy-sweet stir-fried chicken with peanuts; Sichuan original is much more numbing than overseas versions" },
  { id: "f4", termCategory: "dishes", english: "Mapo Tofu", chinese: "麻婆豆腐", pinyin: "Má Pó Dòu Fǔ", context: "Silky tofu in spicy Sichuan numbing sauce with minced pork; vegetarian versions available" },
  { id: "f5", termCategory: "dishes", english: "Hot Pot", chinese: "火锅", pinyin: "Huǒ Guō", context: "Communal boiling broth for cooking meats and vegetables; Chongqing version is the spiciest" },
  { id: "f6", termCategory: "dishes", english: "Dan Dan Noodles", chinese: "担担面", pinyin: "Dān Dān Miàn", context: "Noodles in spicy sesame-chili sauce from Sichuan; often served dry with sauce to mix in" },
  { id: "f7", termCategory: "dishes", english: "Dim Sum", chinese: "点心", pinyin: "Diǎn Xīn", context: "Cantonese small plates; traditionally ordered at brunch tea houses (yum cha 饮茶)" },
  { id: "f8", termCategory: "dishes", english: "Biang Biang Noodles", chinese: "Biang Biang 面", pinyin: "Biáng Biáng Miàn", context: "Flat wide hand-pulled noodles from Xi'an; name comes from the sound of slapping the dough" },
  { id: "f9", termCategory: "dishes", english: "Char Siu", chinese: "叉烧", pinyin: "Chā Shāo", context: "Cantonese honey-glazed BBQ pork; common in rice dishes and buns (叉烧包)" },
  { id: "f10", termCategory: "dishes", english: "Century Egg", chinese: "皮蛋", pinyin: "Pí Dàn", context: "Preserved duck egg with dark green yolk; served with tofu or porridge; acquired taste" },
  { id: "f11", termCategory: "dishes", english: "Tanghulu", chinese: "糖葫芦", pinyin: "Táng Hú Lu", context: "Skewered candied fruit (usually hawthorn berries); classic Beijing street snack" },
  { id: "f12", termCategory: "dishes", english: "Jianbing", chinese: "煎饼", pinyin: "Jiān Bǐng", context: "Savory Chinese crepe cooked fresh on a griddle; popular breakfast street food nationwide" },

  // Signs
  { id: "sg1", termCategory: "signs", english: "Exit", chinese: "出口", pinyin: "Chū Kǒu", context: "Green signs in metro stations and public buildings" },
  { id: "sg2", termCategory: "signs", english: "Entrance", chinese: "入口", pinyin: "Rù Kǒu" },
  { id: "sg3", termCategory: "signs", english: "No Entry", chinese: "禁止入内", pinyin: "Jìn Zhǐ Rù Nèi" },
  { id: "sg4", termCategory: "signs", english: "Restroom / Toilet", chinese: "洗手间 / 厕所", pinyin: "Xǐ Shǒu Jiān / Cè Suǒ" },
  { id: "sg5", termCategory: "signs", english: "Men / Women", chinese: "男 / 女", pinyin: "Nán / Nǚ" },
  { id: "sg6", termCategory: "signs", english: "No Photography", chinese: "禁止拍照", pinyin: "Jìn Zhǐ Pāi Zhào" },
  { id: "sg7", termCategory: "signs", english: "Emergency Exit", chinese: "紧急出口", pinyin: "Jǐn Jí Chū Kǒu" },
  { id: "sg8", termCategory: "signs", english: "Keep Out", chinese: "请勿入内", pinyin: "Qǐng Wù Rù Nèi" },
];

export const PHRASE_CATEGORY_LABELS: Record<string, string> = {
  greetings: "Greetings",
  dining: "Dining",
  transport: "Transport",
  shopping: "Shopping",
  emergency: "Emergency",
  hotel: "Hotel",
};

export const SPECIAL_TERM_CATEGORY_LABELS: Record<string, string> = {
  attractions: "Attractions",
  dishes: "Dishes",
  signs: "Signs",
};
