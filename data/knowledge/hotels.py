"""Hotel price ranges for major Chinese cities — budget, mid-range, and luxury"""

HOTELS = {
    "beijing": {
        "name_zh": "北京",
        "name_en": "Beijing",
        "budget": {"range": "¥150-350/晚", "desc": "青年旅舍/如家/汉庭", "areas": "前门/西单/东四"},
        "mid": {"range": "¥400-800/晚", "desc": "全季/亚朵/希尔顿花园", "areas": "王府井/三里屯/国贸"},
        "luxury": {"range": "¥1000-3000+/晚", "desc": "半岛/华尔道夫/四季/王府井文华东方", "areas": "王府井/国贸/金融街"},
        "tip": "建议住二环内（前门/王府井/东四），离景点近"
    },
    "shanghai": {
        "name_zh": "上海",
        "name_en": "Shanghai",
        "budget": {"range": "¥150-350/晚", "desc": "青年旅舍/汉庭/如家", "areas": "人民广场/南京东路"},
        "mid": {"range": "¥400-900/晚", "desc": "全季/亚朵/和颐", "areas": "外滩/静安寺/淮海路"},
        "luxury": {"range": "¥1200-4000+/晚", "desc": "和平饭店/半岛/浦东丽思卡尔顿/W酒店", "areas": "外滩/陆家嘴/静安"},
        "tip": "游客首选人民广场/南京东路；夜景控住外滩或陆家嘴"
    },
    "chengdu": {
        "name_zh": "成都",
        "name_en": "Chengdu",
        "budget": {"range": "¥120-280/晚", "desc": "青年旅舍/汉庭/如家", "areas": "春熙路/天府广场"},
        "mid": {"range": "¥300-600/晚", "desc": "全季/亚朵/锦江宾馆", "areas": "春熙路/宽窄巷子/武侯祠"},
        "luxury": {"range": "¥800-2500+/晚", "desc": "博舍/群光君悦/瑞吉/华尔道夫", "areas": "太古里/春熙路"},
        "tip": "住春熙路附近最方便，去景点都近。博舍是必住精品酒店"
    },
    "xian": {
        "name_zh": "西安",
        "name_en": "Xi'an",
        "budget": {"range": "¥100-250/晚", "desc": "青年旅舍/汉庭/如家", "areas": "钟楼/南门"},
        "mid": {"range": "¥250-600/晚", "desc": "全季/亚朵/美居", "areas": "钟楼/大雁塔/南门"},
        "luxury": {"range": "¥700-2000+/晚", "desc": "索菲特传奇/威斯汀/万豪行政公寓", "areas": "钟楼/大雁塔"},
        "tip": "住钟楼或南门城墙内，步行去回民街/城墙。大雁塔附近看喷泉方便"
    },
    "guangzhou": {
        "name_zh": "广州",
        "name_en": "Guangzhou",
        "budget": {"range": "¥120-280/晚", "desc": "汉庭/如家/7天", "areas": "天河/北京路"},
        "mid": {"range": "¥300-700/晚", "desc": "全季/亚朵/凯悦嘉轩", "areas": "天河/珠江新城"},
        "luxury": {"range": "¥900-3500+/晚", "desc": "四季/丽思卡尔顿/文华东方/瑰丽", "areas": "珠江新城/天河"},
        "tip": "珠江新城最方便，广州塔夜景一流"
    },
    "hangzhou": {
        "name_zh": "杭州",
        "name_en": "Hangzhou",
        "budget": {"range": "¥150-300/晚", "desc": "汉庭/如家/布丁", "areas": "西湖区/武林广场"},
        "mid": {"range": "¥350-800/晚", "desc": "全季/亚朵/隐居", "areas": "西湖边/灵隐"},
        "luxury": {"range": "¥1000-4000+/晚", "desc": "西子湖四季/安缦法云/悦榕庄/罗莱夏朵", "areas": "西湖景区/灵隐"},
        "tip": "西湖边的民宿比酒店更有味道；预算够安缦法云必体验"
    },
    "guilin": {
        "name_zh": "桂林",
        "name_en": "Guilin",
        "budget": {"range": "¥80-200/晚", "desc": "青年旅舍/汉庭/7天", "areas": "市中心/两江四湖"},
        "mid": {"range": "¥200-500/晚", "desc": "亚朵/维也纳/民宿", "areas": "市中心/漓江边"},
        "luxury": {"range": "¥600-1500+/晚", "desc": "桂林香格里拉/漓江泊隐/悦榕庄(阳朔)", "areas": "漓江东岸/阳朔"},
        "tip": "阳朔比桂林市区更值得住；遇龙河边的民宿体验绝佳"
    },
    "shenzhen": {
        "name_zh": "深圳",
        "name_en": "Shenzhen",
        "budget": {"range": "¥120-280/晚", "desc": "汉庭/如家/7天", "areas": "罗湖/福田"},
        "mid": {"range": "¥300-700/晚", "desc": "全季/亚朵/万怡", "areas": "福田CBD/南山"},
        "luxury": {"range": "¥900-3000+/晚", "desc": "柏悦/瑞吉/莱佛士/君悦", "areas": "福田/南山/罗湖"},
        "tip": "福田CBD最中心；南山区靠近科技园+华侨城"
    },
    "chongqing": {
        "name_zh": "重庆",
        "name_en": "Chongqing",
        "budget": {"range": "¥100-250/晚", "desc": "汉庭/如家/7天", "areas": "解放碑/观音桥"},
        "mid": {"range": "¥250-550/晚", "desc": "全季/亚朵/维也纳", "areas": "解放碑/江北嘴"},
        "luxury": {"range": "¥700-2500+/晚", "desc": "威斯汀/来福士洲际/尼依格罗/NOVOTEL", "areas": "解放碑/江北嘴/南滨路"},
        "tip": "解放碑最中心；看夜景住南滨路或江北嘴"
    },
    "kunming": {
        "name_zh": "昆明",
        "name_en": "Kunming",
        "budget": {"range": "¥100-200/晚", "desc": "汉庭/如家/青旅", "areas": "翠湖/金马碧鸡坊"},
        "mid": {"range": "¥200-500/晚", "desc": "亚朵/全季/索菲特(老)", "areas": "翠湖/东风广场"},
        "luxury": {"range": "¥600-1500+/晚", "desc": "昆明洲际/索菲特/君乐", "areas": "滇池/翠湖"},
        "tip": "住翠湖周边最有昆明味道，步行去讲武堂/云大"
    },
    "lijiang": {
        "name_zh": "丽江",
        "name_en": "Lijiang",
        "budget": {"range": "¥80-200/晚", "desc": "古城客栈/青旅", "areas": "古城内/束河古镇"},
        "mid": {"range": "¥200-600/晚", "desc": "精品民宿/古城客栈", "areas": "古城内/束河"},
        "luxury": {"range": "¥800-2500+/晚", "desc": "悦榕庄/安缦/古城精品酒店", "areas": "束河/古城边"},
        "tip": "住束河比大研古城安静；古城内石板路拖行李很痛苦，建议民宿老板来接"
    },
    "zhangjiajie": {
        "name_zh": "张家界",
        "name_en": "Zhangjiajie",
        "budget": {"range": "¥80-200/晚", "desc": "客栈/汉庭/7天", "areas": "武陵源/市区"},
        "mid": {"range": "¥200-500/晚", "desc": "亚朵/民宿/维也纳", "areas": "武陵源标志门"},
        "luxury": {"range": "¥600-1500+/晚", "desc": "张家界禾田居/纳百利/皇冠假日", "areas": "武陵源"},
        "tip": "住武陵源标志门附近，进景区最方便，步行可达"
    },
    "suzhou": {
        "name_zh": "苏州",
        "name_en": "Suzhou",
        "budget": {"range": "¥120-250/晚", "desc": "汉庭/如家/7天", "areas": "观前街/火车站"},
        "mid": {"range": "¥250-600/晚", "desc": "亚朵/全季/花间堂", "areas": "观前街/平江路"},
        "luxury": {"range": "¥700-2000+/晚", "desc": "苏州金鸡湖凯宾斯基/柏悦/中茵皇冠", "areas": "金鸡湖/平江路"},
        "tip": "平江路/山塘街的民宿最有江南韵味；金鸡湖边适合商务"
    },
    "lhasa": {
        "name_zh": "拉萨",
        "name_en": "Lhasa",
        "budget": {"range": "¥100-250/晚", "desc": "客栈/青旅/汉庭", "areas": "八廓街/北京路"},
        "mid": {"range": "¥250-600/晚", "desc": "亚朵/客栈/瑞吉(基础)", "areas": "八廓街/布达拉宫"},
        "luxury": {"range": "¥800-2000+/晚", "desc": "拉萨瑞吉/拉萨圣地天堂洲际/拉萨香格里拉", "areas": "八廓街/布达拉宫"},
        "tip": "到拉萨第一晚别洗澡！住八廓街附近转经/去布宫都近。高原反应第一两天少活动"
    },
    "nanjing": {
        "name_zh": "南京",
        "name_en": "Nanjing",
        "budget": {"range": "¥120-250/晚", "desc": "汉庭/如家/7天", "areas": "新街口/夫子庙"},
        "mid": {"range": "¥250-600/晚", "desc": "亚朵/全季/金鹰", "areas": "新街口/玄武湖"},
        "luxury": {"range": "¥800-2500+/晚", "desc": "南京丽思卡尔顿/颐和公馆/威斯汀", "areas": "新街口/颐和路"},
        "tip": "新街口最中心；颐和路公馆区有民国风情住宿"
    },
    "harbin": {
        "name_zh": "哈尔滨",
        "name_en": "Harbin",
        "budget": {"range": "¥100-250/晚", "desc": "汉庭/如家/7天", "areas": "中央大街"},
        "mid": {"range": "¥250-600/晚", "desc": "亚朵/全季/万达嘉华", "areas": "中央大街/松北"},
        "luxury": {"range": "¥600-1500+/晚", "desc": "哈尔滨万达文华/香格里拉/松北融创", "areas": "中央大街/松北"},
        "tip": "中央大街附近最方便，步行到索菲亚教堂/冰雪大世界有班车"
    },
}


def format_price_summary() -> str:
    """Compact edition for LLM system prompt"""
    lines = ["## 酒店价格参考 (Hotel Price Guide)"]
    for key, city in HOTELS.items():
        lines.append(f"- {city['name_zh']}({city['name_en']}): 经济{city['budget']['range']} | 中档{city['mid']['range']} | 豪华{city['luxury']['range']}")
    return "\n".join(lines)


def get_city_hotel(city_key: str) -> dict | None:
    return HOTELS.get(city_key.lower())
