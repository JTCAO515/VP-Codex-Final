"""中国各地美食知识库"""

CUISINE_TYPES = {
    "street": "街头小吃 — 便宜、地道、边走边吃",
    "famous": "名菜/招牌菜 — 餐厅必点",
    "restaurant": "餐厅美食 — 坐下来吃",
    "snack": "零食/饮品 — 随手买的",
}

FOOD = {
    "beijing": [
        {"name_zh": "北京烤鸭", "name_en": "Peking Duck", "type": "famous", "price_range": "¥150-300/只", "description": "皮脆肉嫩，全聚德/大董/四季民福", "must_try": True},
        {"name_zh": "炸酱面", "name_en": "Zhajiang Noodles", "type": "street", "price_range": "¥15-30", "description": "老北京家常面，方砖厂69号有名", "must_try": True},
        {"name_zh": "涮羊肉", "name_en": "Mutton Hot Pot", "type": "famous", "price_range": "¥80-150/人", "description": "东来顺/聚宝源铜锅涮肉", "must_try": True},
        {"name_zh": "豆汁儿", "name_en": "Douzhi (Fermented Bean Drink)", "type": "street", "price_range": "¥5-10", "description": "老北京挑战级饮品，酸甜馊味", "must_try": False},
        {"name_zh": "冰糖葫芦", "name_en": "Bingtang Hulu", "type": "snack", "price_range": "¥5-15", "description": "山楂裹糖，儿时记忆", "must_try": False},
    ],
    "shanghai": [
        {"name_zh": "小笼包", "name_en": "Xiaolongbao (Soup Dumplings)", "type": "famous", "price_range": "¥8-15/笼", "description": "汤汁丰富，佳家汤包/南翔馒头店", "must_try": True},
        {"name_zh": "生煎包", "name_en": "Shengjianbao", "type": "street", "price_range": "¥6-12/两", "description": "底部焦脆，小杨生煎/大壶春", "must_try": True},
        {"name_zh": "红烧肉", "name_en": "Braised Pork Belly", "type": "famous", "price_range": "¥60-100", "description": "浓油赤酱，外婆家/兰心", "must_try": True},
        {"name_zh": "大闸蟹", "name_en": "Hairy Crab", "type": "famous", "price_range": "¥100-300/只", "description": "秋季限定，阳澄湖大闸蟹", "must_try": False},
        {"name_zh": "葱油拌面", "name_en": "Scallion Oil Noodles", "type": "street", "price_range": "¥10-20", "description": "简单但香浓", "must_try": False},
    ],
    "chengdu": [
        {"name_zh": "火锅", "name_en": "Sichuan Hot Pot", "type": "famous", "price_range": "¥60-120/人", "description": "麻辣鲜香，小龙坎/大龙燚/蜀大侠", "must_try": True},
        {"name_zh": "串串香", "name_en": "Chuan Chuan", "type": "street", "price_range": "¥30-60/人", "description": "自选串串煮着吃，冒椒火辣/钢管厂五区", "must_try": True},
        {"name_zh": "担担面", "name_en": "Dandan Noodles", "type": "famous", "price_range": "¥10-20", "description": "花生碎+肉末，甜水面类似", "must_try": True},
        {"name_zh": "麻婆豆腐", "name_en": "Mapo Tofu", "type": "famous", "price_range": "¥20-40", "description": "陈麻婆豆腐是始祖", "must_try": True},
        {"name_zh": "夫妻肺片", "name_en": "Fuqi Feipian", "type": "famous", "price_range": "¥30-50", "description": "红油凉拌牛肉牛杂", "must_try": False},
        {"name_zh": "冰粉", "name_en": "Ice Jelly", "type": "snack", "price_range": "¥8-15", "description": "火锅必备解辣甜品", "must_try": False},
    ],
    "xian": [
        {"name_zh": "肉夹馍", "name_en": "Roujiamo (Chinese Burger)", "type": "street", "price_range": "¥8-15", "description": "腊汁肉夹白吉馍，潼关肉夹馍更酥脆", "must_try": True},
        {"name_zh": "羊肉泡馍", "name_en": "Yangrou Paomo", "type": "famous", "price_range": "¥25-40", "description": "自己掰馍才有灵魂，老孙家/同盛祥", "must_try": True},
        {"name_zh": "biangbiang面", "name_en": "Biangbiang Noodles", "type": "famous", "price_range": "¥15-25", "description": "宽如裤带，油泼辣子最香", "must_try": True},
        {"name_zh": "凉皮", "name_en": "Liangpi (Cold Noodles)", "type": "street", "price_range": "¥6-12", "description": "陕西凉皮/米皮，酸辣开胃", "must_try": True},
        {"name_zh": "灌汤包子", "name_en": "Soup Dumplings", "type": "street", "price_range": "¥10-18/笼", "description": "贾三灌汤包子最出名", "must_try": False},
    ],
    "guilin": [
        {"name_zh": "桂林米粉", "name_en": "Guilin Rice Noodles", "type": "street", "price_range": "¥8-15", "description": "卤水是灵魂，配锅烧肉", "must_try": True},
        {"name_zh": "啤酒鱼", "name_en": "Beer Fish", "type": "famous", "price_range": "¥60-100", "description": "阳朔招牌菜，用漓江啤酒做", "must_try": True},
        {"name_zh": "田螺酿", "name_en": "Stuffed Snails", "type": "street", "price_range": "¥20-35", "description": "螺肉+肉末塞回螺壳", "must_try": False},
        {"name_zh": "荔浦芋扣肉", "name_en": "Lipu Taro Pork Belly", "type": "famous", "price_range": "¥40-60", "description": "荔浦芋头+五花肉蒸", "must_try": False},
        {"name_zh": "油茶", "name_en": "Oil Tea", "type": "street", "price_range": "¥5-10", "description": "恭城油茶，配米花/花生", "must_try": False},
    ],
    "guangzhou": [
        {"name_zh": "早茶", "name_en": "Dim Sum (Yum Cha)", "type": "restaurant", "price_range": "¥40-100/人", "description": "虾饺/烧卖/凤爪/肠粉，陶陶居/点都德", "must_try": True},
        {"name_zh": "白切鸡", "name_en": "White Cut Chicken", "type": "famous", "price_range": "¥50-80/半只", "description": "皮滑肉嫩，配姜葱酱", "must_try": True},
        {"name_zh": "煲仔饭", "name_en": "Claypot Rice", "type": "street", "price_range": "¥20-40", "description": "腊味/排骨煲仔饭，底有锅巴", "must_try": True},
        {"name_zh": "双皮奶", "name_en": "Double-skin Milk", "type": "snack", "price_range": "¥10-20", "description": "顺德甜品，南信/仁信", "must_try": True},
        {"name_zh": "云吞面", "name_en": "Wonton Noodles", "type": "street", "price_range": "¥15-25", "description": "竹升面+鲜虾云吞，坚记面店", "must_try": True},
        {"name_zh": "烧鹅", "name_en": "Roasted Goose", "type": "famous", "price_range": "¥50-80/例", "description": "皮脆肉嫩，炳胜/太兴", "must_try": False},
    ],
    "chongqing": [
        {"name_zh": "重庆火锅", "name_en": "Chongqing Hot Pot", "type": "famous", "price_range": "¥50-100/人", "description": "九宫格+牛油锅底，佩姐/周师兄", "must_try": True},
        {"name_zh": "重庆小面", "name_en": "Chongqing Xiao Mian", "type": "street", "price_range": "¥8-15", "description": "红油汤底，秦云老太婆摊摊面", "must_try": True},
        {"name_zh": "毛血旺", "name_en": "Mao Xue Wang", "type": "famous", "price_range": "¥40-70", "description": "鸭血+毛肚+豆芽，麻辣鲜香", "must_try": True},
        {"name_zh": "酸辣粉", "name_en": "Suan La Fen", "type": "street", "price_range": "¥8-15", "description": "红薯粉条酸辣Q弹", "must_try": False},
        {"name_zh": "辣子鸡", "name_en": "Chongqing Spicy Chicken", "type": "famous", "price_range": "¥40-70", "description": "干辣椒里找鸡丁", "must_try": False},
    ],
    "yunnan": [
        {"name_zh": "过桥米线", "name_en": "Crossing Bridge Noodles", "type": "famous", "price_range": "¥25-80", "description": "滚烫鸡汤+生料现烫", "must_try": True},
        {"name_zh": "野生菌火锅", "name_en": "Wild Mushroom Hot Pot", "type": "famous", "price_range": "¥80-200/人", "description": "雨季限定，菌子种类丰富", "must_try": True},
        {"name_zh": "汽锅鸡", "name_en": "Steam Pot Chicken", "type": "famous", "price_range": "¥50-90", "description": "不加一滴水，蒸汽凝结为汤", "must_try": True},
        {"name_zh": "烤乳扇", "name_en": "Roasted Milk Fan", "type": "street", "price_range": "¥5-10", "description": "大理街头小吃，奶香酥脆", "must_try": False},
        {"name_zh": "水性杨花", "name_en": "Water Lily Stem", "type": "famous", "price_range": "¥20-35", "description": "泸沽湖水草，口感滑嫩", "must_try": False},
    ],
    "changsha": [
        {"name_zh": "臭豆腐", "name_en": "Stinky Tofu", "type": "street", "price_range": "¥5-10", "description": "黑色经典，外脆里嫩", "must_try": True},
        {"name_zh": "茶颜悦色", "name_en": "Sexy Tea Brand", "type": "snack", "price_range": "¥12-18/杯", "description": "长沙之光，幽兰拿铁必喝", "must_try": True},
        {"name_zh": "口味虾", "name_en": "Flavor Crayfish", "type": "famous", "price_range": "¥60-100/份", "description": "麻辣小龙虾，文和友最出名", "must_try": True},
        {"name_zh": "糖油粑粑", "name_en": "Tangyou BABA", "type": "street", "price_range": "¥5-10", "description": "糯米团子炸后裹糖，甜糯", "must_try": False},
        {"name_zh": "剁椒鱼头", "name_en": "Steamed Fish Head with Chopped Chili", "type": "famous", "price_range": "¥60-100", "description": "湘菜经典招牌", "must_try": False},
    ],
    "hangzhou": [
        {"name_zh": "西湖醋鱼", "name_en": "West Lake Vinegar Fish", "type": "famous", "price_range": "¥80-150", "description": "草鱼+糖醋汁，楼外楼最经典", "must_try": True},
        {"name_zh": "东坡肉", "name_en": "Dongpo Pork", "type": "famous", "price_range": "¥60-100", "description": "苏东坡发明的红烧肉做法", "must_try": True},
        {"name_zh": "龙井虾仁", "name_en": "Longjing Shrimp", "type": "famous", "price_range": "¥80-120", "description": "龙井茶叶炒虾仁，清香", "must_try": True},
        {"name_zh": "定胜糕", "name_en": "Dingsheng Cake", "type": "snack", "price_range": "¥10-25", "description": "杭州传统糕点", "must_try": False},
        {"name_zh": "片儿川", "name_en": "Pianerchuan Noodles", "type": "street", "price_range": "¥15-25", "description": "杭州特色面条", "must_try": False},
    ],
    "nanjing": [
        {"name_zh": "鸭血粉丝汤", "name_en": "Duck Blood Vermicelli Soup", "type": "street", "price_range": "¥15-25", "description": "鸭血+粉丝+豆腐泡，回味鸭血粉丝", "must_try": True},
        {"name_zh": "盐水鸭", "name_en": "Nanjing Salted Duck", "type": "famous", "price_range": "¥30-60", "description": "金陵一绝，皮白肉嫩", "must_try": True},
        {"name_zh": "牛肉锅贴", "name_en": "Beef Potstickers", "type": "street", "price_range": "¥10-20", "description": "底部金黄，汁水丰富", "must_try": False},
        {"name_zh": "梅花糕", "name_en": "Plum Cake", "type": "snack", "price_range": "¥5-10", "description": "传统小吃，豆沙馅", "must_try": False},
    ],
    "suzhou": [
        {"name_zh": "松鼠桂鱼", "name_en": "Squirrel-shaped Mandarin Fish", "type": "famous", "price_range": "¥150-250", "description": "刀工+酸甜汁，形如松鼠", "must_try": True},
        {"name_zh": "碧螺虾仁", "name_en": "Biluochun Shrimp", "type": "famous", "price_range": "¥80-120", "description": "碧螺春茶炒虾仁", "must_try": True},
        {"name_zh": "苏式汤面", "name_en": "Suzhou Soup Noodles", "type": "street", "price_range": "¥15-35", "description": "面细汤鲜，秃黄油面是至尊", "must_try": True},
        {"name_zh": "桂花糖藕", "name_en": "Osmanthus Lotus Root", "type": "snack", "price_range": "¥15-25", "description": "糯米塞莲藕，桂花蜜汁", "must_try": False},
    ],
    "harbin": [
        {"name_zh": "锅包肉", "name_en": "Guobaorou", "type": "famous", "price_range": "¥35-55", "description": "哈尔滨起源，酸甜酥脆", "must_try": True},
        {"name_zh": "马迭尔冰棍", "name_en": "Modern Ice Cream", "type": "snack", "price_range": "¥5-10", "description": "中央大街百年冰棍，冬天也吃", "must_try": True},
        {"name_zh": "酱骨棒", "name_en": "Braised Pork Bone", "type": "famous", "price_range": "¥40-70", "description": "大口吃肉", "must_try": False},
        {"name_zh": "红肠", "name_en": "Red Sausage", "type": "snack", "price_range": "¥10-20/根", "description": "俄式红肠，秋林里道斯", "must_try": False},
    ],
    "shenzhen": [
        {"name_zh": "椰子鸡火锅", "name_en": "Coconut Chicken Hot Pot", "type": "famous", "price_range": "¥80-150/人", "description": "深圳本地产物，椰汁煮鸡，润园四季", "must_try": True},
        {"name_zh": "砂锅粥", "name_en": "Claypot Congee", "type": "street", "price_range": "¥20-50", "description": "潮汕砂锅粥加海鲜，金稻园", "must_try": True},
        {"name_zh": "烧鹅", "name_en": "Roasted Goose", "type": "famous", "price_range": "¥50-80", "description": "光明乳鸽是深圳特色", "must_try": False},
        {"name_zh": "肠粉", "name_en": "Rice Noodle Roll", "type": "street", "price_range": "¥6-15", "description": "广式肠粉当早餐，红荔村", "must_try": True},
        {"name_zh": "早茶", "name_en": "Dim Sum (Yum Cha)", "type": "restaurant", "price_range": "¥40-100/人", "description": "点都德/广州酒家/凤凰楼", "must_try": True},
    ],
    "kunming": [
        {"name_zh": "过桥米线", "name_en": "Crossing-the-Bridge Noodles", "type": "famous", "price_range": "¥20-60", "description": "正宗发源地，建新园/桥香园", "must_try": True},
        {"name_zh": "汽锅鸡", "name_en": "Steam Pot Chicken", "type": "famous", "price_range": "¥50-100", "description": "福照楼是最老牌汽锅鸡", "must_try": True},
        {"name_zh": "野生菌火锅", "name_en": "Wild Mushroom Hot Pot", "type": "famous", "price_range": "¥100-200/人", "description": "雨季去昆明必须吃菌子", "must_try": True},
        {"name_zh": "烧饵块", "name_en": "Roasted Rice Cake", "type": "street", "price_range": "¥5-8", "description": "昆明街边早餐，夹酱/烤肠", "must_try": False},
        {"name_zh": "官渡粑粑", "name_en": "Guandu Flatbread", "type": "snack", "price_range": "¥3-5", "description": "官渡古镇特色，红糖/豆沙馅", "must_try": False},
    ],
    "lijiang": [
        {"name_zh": "腊排骨火锅", "name_en": "Smoked Pork Rib Hot Pot", "type": "famous", "price_range": "¥60-100/人", "description": "丽江必吃，象山市场腊排骨", "must_try": True},
        {"name_zh": "鸡豆凉粉", "name_en": "Chicken Bean Jelly", "type": "street", "price_range": "¥8-15", "description": "丽江特有，煎/凉拌两种吃法", "must_try": True},
        {"name_zh": "纳西烤鱼", "name_en": "Naxi Grilled Fish", "type": "restaurant", "price_range": "¥40-70", "description": "纳西族特色烤鱼配香料", "must_try": False},
        {"name_zh": "黑山羊火锅", "name_en": "Black Goat Hot Pot", "type": "famous", "price_range": "¥50-80/人", "description": "丽江黑山羊汤锅，暖身首选", "must_try": False},
        {"name_zh": "酥油茶", "name_en": "Butter Tea", "type": "snack", "price_range": "¥10-20", "description": "藏式酥油茶，高原必备", "must_try": False},
        {"name_zh": "粑粑", "name_en": "Baba (Flatbread)", "type": "street", "price_range": "¥5-10", "description": "丽江粑粑，酥油煎制", "must_try": False},
    ],
    "lhasa": [
        {"name_zh": "藏面", "name_en": "Tibetan Noodles", "type": "street", "price_range": "¥10-20", "description": "牦牛骨汤底+粗面，光明港琼甜茶馆", "must_try": True},
        {"name_zh": "甜茶", "name_en": "Sweet Tea", "type": "snack", "price_range": "¥1-3/杯", "description": "拉萨人民的精神饮料", "must_try": True},
        {"name_zh": "糌粑", "name_en": "Tsampa (Roasted Barley Dough)", "type": "famous", "price_range": "¥5-10", "description": "青稞炒面+酥油茶捏成团", "must_try": False},
        {"name_zh": "牦牛肉火锅", "name_en": "Yak Meat Hot Pot", "type": "famous", "price_range": "¥60-120/人", "description": "高原牦牛肉，滋补暖身", "must_try": True},
        {"name_zh": "藏族酸奶", "name_en": "Tibetan Yogurt", "type": "snack", "price_range": "¥8-15", "description": "牦牛酸奶配蜂蜜，酸到皱眉", "must_try": True},
        {"name_zh": "青稞酒", "name_en": "Highland Barley Wine", "type": "snack", "price_range": "¥5-15/杯", "description": "度数不高但后劲足", "must_try": False},
    ],
    "zhangjiajie": [
        {"name_zh": "三下锅", "name_en": "Sanxiaguo (Three-Pot Mix)", "type": "famous", "price_range": "¥40-80", "description": "张家界必吃，腊肉+蔬菜+豆腐一锅炖", "must_try": True},
        {"name_zh": "土家腊肉", "name_en": "Tujia Smoked Pork", "type": "famous", "price_range": "¥30-60", "description": "烟熏腊肉配蒜苗", "must_try": True},
        {"name_zh": "岩耳炖土鸡", "name_en": "Rock Ear Mushroom Stewed Chicken", "type": "famous", "price_range": "¥60-120", "description": "悬崖岩耳，鲜香无比", "must_try": False},
        {"name_zh": "葛根粉", "name_en": "Kudzu Root Jelly", "type": "snack", "price_range": "¥5-10", "description": "山上特产，清火解暑", "must_try": False},
        {"name_zh": "酸肉", "name_en": "Sour Pork", "type": "street", "price_range": "¥15-30", "description": "土家族传统发酵肉", "must_try": False},
    ],
}

# 特色美食类型概览
CUISINE_CATEGORIES = {
    "火锅": {"type": "hotpot", "description": "四川/重庆麻辣火锅 vs 北京铜锅涮肉 vs 潮汕牛肉火锅", "cities": ["chengdu", "chongqing", "beijing"]},
    "面食": {"type": "noodles", "description": "biangbiang面/担担面/小面/热干面/炸酱面", "cities": ["xian", "chengdu", "chongqing", "beijing"]},
    "烧烤": {"type": "bbq", "description": "各地烧烤各有特色，淄博烧烤最火", "cities": ["changsha", "nanjing"]},
    "甜品": {"type": "dessert", "description": "双皮奶/冰粉/糖水/芝麻糊", "cities": ["guangzhou", "chengdu"]},
}
