package space.go2china.visepanda.data.explore

object MockExploreData {
    val cities = listOf(
        ExploreCity(
            id = "beijing",
            name = "Beijing",
            region = "North China",
            tagline = "Imperial history, the Great Wall, and wide boulevards.",
            bestFor = listOf("First-time visitors", "History lovers", "Architecture")
        ),
        ExploreCity(
            id = "shanghai",
            name = "Shanghai",
            region = "East China",
            tagline = "Riverfront skyline, French Concession streets, and global dining.",
            bestFor = listOf("Business travelers", "Nightlife", "Shopping")
        ),
        ExploreCity(
            id = "chengdu",
            name = "Chengdu",
            region = "Southwest China",
            tagline = "Sichuan food, pandas, and a slower pace of life.",
            bestFor = listOf("Food travelers", "Solo travelers", "Relaxed pace")
        ),
        ExploreCity(
            id = "xian",
            name = "Xi'an",
            region = "Northwest China",
            tagline = "Ancient city walls and the Terracotta Army.",
            bestFor = listOf("History lovers", "Day-trip planners")
        ),
        ExploreCity(
            id = "guangzhou",
            name = "Guangzhou",
            region = "South China",
            tagline = "Cantonese food, river views, old trading history, and easy Greater Bay links.",
            bestFor = listOf("Food travelers", "Business travelers", "Warm weather")
        ),
        ExploreCity(
            id = "hangzhou",
            name = "Hangzhou",
            region = "East China",
            tagline = "West Lake scenery, tea fields, temples, and relaxed day trips from Shanghai.",
            bestFor = listOf("Scenic walks", "Tea culture", "Couples")
        ),
        ExploreCity(
            id = "suzhou",
            name = "Suzhou",
            region = "East China",
            tagline = "Classical gardens, canals, silk history, and soft Jiangnan pacing.",
            bestFor = listOf("Gardens", "Photography", "Slow travel")
        ),
        ExploreCity(
            id = "chongqing",
            name = "Chongqing",
            region = "Southwest China",
            tagline = "Mountain-city views, hotpot, river nightscapes, and dramatic urban layers.",
            bestFor = listOf("Food travelers", "Night views", "Urban explorers")
        )
    )

    val attractions = listOf(
        ExploreAttraction(
            id = "beijing-forbidden-city", cityId = "beijing", name = "Forbidden City", category = "Heritage",
            description = "The former imperial palace at the heart of Beijing.", rating = "4.9", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "World-class heritage landmark. Essential for first-time visitors to Beijing."
        ),
        ExploreAttraction(
            id = "beijing-great-wall", cityId = "beijing", name = "Great Wall (Mutianyu)", category = "Heritage",
            description = "A well-restored, visitor-friendly section of the Great Wall.", rating = "4.8", priceLevel = "¥¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Foreigner-friendly segment. Chairlifts and toboggans make access seamless."
        ),
        ExploreAttraction(
            id = "shanghai-the-bund", cityId = "shanghai", name = "The Bund", category = "Landmark",
            description = "Riverside promenade facing Shanghai's modern skyline.", rating = "4.7", priceLevel = "¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Iconic photo point. Walkable and highly vibrant during sunset."
        ),
        ExploreAttraction(
            id = "shanghai-yu-garden", cityId = "shanghai", name = "Yu Garden", category = "Heritage",
            description = "Classical Ming-dynasty garden in the old city.", rating = "4.5", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Classic Jiangnan architecture inside a bustling shopping area."
        ),
        ExploreAttraction(
            id = "chengdu-panda-base", cityId = "chengdu", name = "Chengdu Research Base of Giant Panda Breeding", category = "Wildlife",
            description = "Home to giant pandas in a forested research setting.", rating = "4.9", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Must-see flagship species center. Early morning visits recommended."
        ),
        ExploreAttraction(
            id = "chengdu-kuanzhai", cityId = "chengdu", name = "Kuanzhai Alley", category = "Heritage",
            description = "Restored Qing-dynasty lanes with teahouses and shops.", rating = "4.4", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Great leisure walk. Showcases traditional Sichuan teahouse culture."
        ),
        ExploreAttraction(
            id = "xian-terracotta-army", cityId = "xian", name = "Terracotta Army", category = "Heritage",
            description = "Thousands of life-sized terracotta soldiers guarding an ancient emperor's tomb.", rating = "4.8", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Unequaled archaeological site. Hire an English guide on arrival."
        ),
        ExploreAttraction(
            id = "xian-city-wall", cityId = "xian", name = "Xi'an City Wall", category = "Heritage",
            description = "One of China's best-preserved ancient city walls, open for walking and cycling.", rating = "4.6", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Superb cycle route. Offers elevated views of ancient fortifications."
        ),
        ExploreAttraction(
            id = "guangzhou-chen-clan", cityId = "guangzhou", name = "Chen Clan Ancestral Hall", category = "Heritage",
            description = "Intricate Lingnan architecture with carved wood, brick, and ceramic details.", rating = "4.7", priceLevel = "¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Magnificent local handicraft museum, rich in Southern China history."
        ),
        ExploreAttraction(
            id = "guangzhou-canton-tower", cityId = "guangzhou", name = "Canton Tower", category = "Landmark",
            description = "A skyline icon with Pearl River views, best around sunset or after dark.", rating = "4.5", priceLevel = "¥¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Tallest structure in Guangzhou. High-altitude views of the GBA delta."
        ),
        ExploreAttraction(
            id = "hangzhou-west-lake", cityId = "hangzhou", name = "West Lake", category = "Scenic",
            description = "Hangzhou's classic lakefront for walking, cycling, boats, and temple-side views.", rating = "4.9", priceLevel = "¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "UNESCO heritage site. The absolute crown jewel of Jiangnan scenery."
        ),
        ExploreAttraction(
            id = "suzhou-humble-administrator", cityId = "suzhou", name = "Humble Administrator's Garden", category = "Garden",
            description = "A landmark classical garden with ponds, pavilions, and layered views.", rating = "4.8", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Finest example of Chinese garden design. Arrive early to avoid crowds."
        ),
        ExploreAttraction(
            id = "chongqing-hongya", cityId = "chongqing", name = "Hongya Cave", category = "Night view",
            description = "Layered riverside architecture that glows at night near Jiefangbei.", rating = "4.6", priceLevel = "¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Stunning cyber-punk aesthetic nightscape that drops to the river edge."
        )
    )

    val foodSpots = listOf(
        ExploreFoodSpot(
            id = "beijing-peking-duck", cityId = "beijing", name = "Da Dong Roast Duck", dish = "Peking duck",
            description = "A well-known spot for Beijing's signature roast duck.", rating = "4.8", priceLevel = "¥¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Leaner, crispy skin roast duck style. English-speaking service ready."
        ),
        ExploreFoodSpot(
            id = "shanghai-xiaolongbao", cityId = "shanghai", name = "Din Tai Fung", dish = "Xiaolongbao",
            description = "Soup dumplings done with precision.", rating = "4.6", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Highly standardized. Safe for first-timers to sample authentic soup buns."
        ),
        ExploreFoodSpot(
            id = "chengdu-hotpot", cityId = "chengdu", name = "Shu Jiu Xiang Hotpot", dish = "Sichuan hotpot",
            description = "Spicy, numbing hotpot in a classic Chengdu setting.", rating = "4.7", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Authentic hotpot chain offering customized low-spice layout parameters."
        ),
        ExploreFoodSpot(
            id = "xian-noodles", cityId = "xian", name = "De Fa Chang", dish = "Biang biang noodles",
            description = "Hand-pulled wide noodles, a Xi'an street food staple.", rating = "4.5", priceLevel = "¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Excellent central restaurant. Dumpling banquets and wide wheat noodles."
        ),
        ExploreFoodSpot(
            id = "guangzhou-dim-sum", cityId = "guangzhou", name = "Tao Tao Ju", dish = "Dim sum",
            description = "Classic Cantonese tea-house dining with a broad dim sum menu.", rating = "4.7", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Century-old tea house. Perfect for trying 'Yum Cha' morning tea."
        ),
        ExploreFoodSpot(
            id = "hangzhou-louwailou", cityId = "hangzhou", name = "Lou Wai Lou", dish = "West Lake fish",
            description = "A historic lakeside restaurant for Hangzhou classics.", rating = "4.3", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Lakeside dining pedigree. Taste traditional sweet & sour fish."
        ),
        ExploreFoodSpot(
            id = "suzhou-noodles", cityId = "suzhou", name = "Tong De Xing", dish = "Suzhou noodles",
            description = "Seasonal noodle bowls with a delicate Jiangnan style.", rating = "4.6", priceLevel = "¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Featured in food documentaries. Delicate broth, clean Jiangnan setup."
        ),
        ExploreFoodSpot(
            id = "chongqing-hotpot", cityId = "chongqing", name = "Pei Jie Hotpot", dish = "Chongqing hotpot",
            description = "Bold, spicy hotpot close to the city's signature flavor.", rating = "4.8", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Very popular locally. Strong, fiery tallow hotpot base."
        )
    )

    val stays = listOf(
        ExploreStay(
            id = "beijing-wangfujing", cityId = "beijing", name = "Wangfujing Area", area = "Dongcheng District",
            description = "Central, walkable, close to the Forbidden City.", rating = "4.8", priceLevel = "¥¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Premium downtown hub. Walk to Forbidden City and subway line 1."
        ),
        ExploreStay(
            id = "shanghai-jingan", cityId = "shanghai", name = "Jing'an Area", area = "Jing'an District",
            description = "Convenient metro access and a mix of local and international dining.", rating = "4.7", priceLevel = "¥¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Expats favorite. High English coverage and central hub intersection."
        ),
        ExploreStay(
            id = "chengdu-kuanzhai-stay", cityId = "chengdu", name = "Kuanzhai Alley Area", area = "Qingyang District",
            description = "Walkable old-town base close to teahouses and food streets.", rating = "4.6", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Charming heritage district. Plentiful boutique courtyard hostings."
        ),
        ExploreStay(
            id = "xian-bell-tower", cityId = "xian", name = "Bell Tower Area", area = "Beilin District",
            description = "Central base inside the city wall, close to the Muslim Quarter.", rating = "4.7", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Fortification heart. Steps from night markets and central drum towers."
        ),
        ExploreStay(
            id = "guangzhou-tianhe", cityId = "guangzhou", name = "Tianhe Area", area = "Tianhe District",
            description = "Business-friendly base with malls, metro access, and easy dining.", rating = "4.8", priceLevel = "¥¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Modern GBA center. Connected to top-tier malls and direct HK trains."
        ),
        ExploreStay(
            id = "hangzhou-west-lake-stay", cityId = "hangzhou", name = "West Lake East Shore", area = "Shangcheng District",
            description = "Convenient for lake walks, shopping streets, and first-time stays.", rating = "4.7", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Scenic stroll accessibility. Direct access to lake paths and line 1."
        ),
        ExploreStay(
            id = "suzhou-gusu", cityId = "suzhou", name = "Gusu Old Town", area = "Gusu District",
            description = "Best for gardens, canals, and short taxi or walking distances.", rating = "4.6", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Historic water town charm. Sleep next to canals and bridges."
        ),
        ExploreStay(
            id = "chongqing-jiefangbei", cityId = "chongqing", name = "Jiefangbei Area", area = "Yuzhong District",
            description = "Central for night views, food streets, metro access, and riverfront walks.", rating = "4.8", priceLevel = "¥¥",
            sourceLabel = "static", confidence = "Curated", fitRationale = "Mountain capital focal point. Subway lines 1/2 convergence."
        )
    )
}
