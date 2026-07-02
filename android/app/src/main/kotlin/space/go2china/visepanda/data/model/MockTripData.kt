package space.go2china.visepanda.data.model

/**
 * Native port of lib/mock-ai/mockButler.ts's initialTripState. Kept
 * content-identical to the web mock (same cities, POIs, coordinates,
 * addresses) on purpose — a tester comparing the web app and this APK side
 * by side should see the same starting trip, not two different demo
 * itineraries.
 */
object MockTripData {

    val initialTripState: TripState = TripState(
        summary = TripSummary(
            title = "China Trip Draft",
            durationDays = 5,
            pace = Pace.Balanced,
            travelerStyle = "First-time visitor",
            destinations = listOf("Beijing", "Shanghai"),
            confidence = TripConfidence.Draft,
        ),
        days = listOf(
            TripDay(
                day = 1,
                city = "Beijing",
                pace = Pace.Balanced,
                blocks = listOf(
                    TripBlock(
                        time = BlockTime.Morning,
                        title = "Forbidden City (故宫)",
                        description = "Start with the classic imperial axis and keep the morning focused.",
                        address = "4 Jingshan Front Street, Dongcheng District, Beijing",
                        chineseAddress = "北京市东城区景山前街4号",
                        openingHours = "Usually daytime entry; timed tickets required",
                        mapUrl = "https://uri.amap.com/search?keyword=%E6%95%85%E5%AE%AB",
                        bookingUrl = "https://intl.dpm.org.cn/",
                        bookingCandidates = listOf(
                            BookingCandidate(
                                id = "static-ticket-forbidden-city",
                                kind = BookingCandidateKind.Ticket,
                                label = "Forbidden City official ticket info",
                                provider = "Official venue",
                                status = BookingCandidateStatus.InfoOnly,
                                note = "Information link only. Confirm current availability, passport rules, and refund policy before paying.",
                            ),
                        ),
                        sourceLabel = "Static fallback",
                        coordinates = Coordinates(lat = 39.91635, lng = 116.39715),
                    ),
                    TripBlock(
                        time = BlockTime.Afternoon,
                        title = "Great Wall · Mutianyu (长城·慕田峪)",
                        description = "Use a private car or guided transfer to reduce friction.",
                        address = "Mutianyu Village, Huairou District, Beijing",
                        chineseAddress = "北京市怀柔区慕田峪村",
                        openingHours = "Daytime scenic-area hours; confirm before departure",
                        mapUrl = "https://uri.amap.com/search?keyword=%E6%85%95%E7%94%B0%E5%B3%AA%E9%95%BF%E5%9F%8E",
                        sourceLabel = "Static fallback",
                        coordinates = Coordinates(lat = 40.43191, lng = 116.57037),
                    ),
                    TripBlock(
                        time = BlockTime.Evening,
                        title = "Temple of Heaven (天坛)",
                        description = "Keep the evening iconic but simple if energy is low.",
                        address = "1 Tiantan East Road, Dongcheng District, Beijing",
                        chineseAddress = "北京市东城区天坛东路1号",
                        openingHours = "Park open into evening; halls close earlier",
                        mapUrl = "https://uri.amap.com/search?keyword=%E5%A4%A9%E5%9D%9B",
                        sourceLabel = "Static fallback",
                        coordinates = Coordinates(lat = 39.88216, lng = 116.40661),
                    ),
                ),
                food = listOf("Hutong noodles", "Roast duck dinner"),
                stay = "Beijing city-center hotel",
                transport = "Airport transfer and private car for the Great Wall.",
                note = "Keep the first day structured and book key tickets ahead.",
                status = DayStatus.New,
            ),
            TripDay(
                day = 2,
                city = "Shanghai",
                pace = Pace.Balanced,
                blocks = listOf(
                    TripBlock(
                        time = BlockTime.Morning,
                        title = "The Bund (外滩)",
                        description = "Start with an easy riverfront orientation.",
                        address = "Zhongshan East 1st Road, Huangpu District, Shanghai",
                        chineseAddress = "上海市黄浦区中山东一路",
                        openingHours = "Open public promenade",
                        mapUrl = "https://uri.amap.com/search?keyword=%E5%A4%96%E6%BB%A9",
                        sourceLabel = "Static fallback",
                        coordinates = Coordinates(lat = 31.23969, lng = 121.49976),
                    ),
                    TripBlock(
                        time = BlockTime.Afternoon,
                        title = "Yu Garden (豫园)",
                        description = "Pair old Shanghai lanes with a classic garden stop.",
                        address = "279 Yuyuan Old Street, Huangpu District, Shanghai",
                        chineseAddress = "上海市黄浦区豫园老街279号",
                        openingHours = "Ticketed garden hours vary by season",
                        mapUrl = "https://uri.amap.com/search?keyword=%E8%B1%AB%E5%9B%AD",
                        sourceLabel = "Static fallback",
                        coordinates = Coordinates(lat = 31.22723, lng = 121.49201),
                    ),
                    TripBlock(
                        time = BlockTime.Evening,
                        title = "Nanjing Road (南京路)",
                        description = "Use a central evening walk with simple food options nearby.",
                    ),
                ),
                food = listOf("Xiaolongbao", "Shanghainese noodles"),
                stay = "Shanghai city-center hotel",
                transport = "Metro / high-speed rail",
                note = "Keep hotel areas central so the evening is easy.",
                status = DayStatus.New,
            ),
            TripDay(
                day = 3,
                city = "Beijing",
                pace = Pace.Balanced,
                blocks = listOf(
                    TripBlock(
                        time = BlockTime.Morning,
                        title = "Summer Palace (颐和园)",
                        description = "Use a slower scenic morning with lake views.",
                    ),
                    TripBlock(
                        time = BlockTime.Afternoon,
                        title = "Hutong walk (胡同)",
                        description = "Add a neighborhood walk and tea break.",
                    ),
                    TripBlock(
                        time = BlockTime.Evening,
                        title = "Local dinner",
                        description = "Pick a relaxed dinner close to the hotel.",
                    ),
                ),
                food = listOf("Tea house snacks", "Neighborhood dinner"),
                stay = "Beijing city-center hotel",
                transport = "Metro / short taxi rides",
                note = "This day balances culture with recovery time.",
                status = DayStatus.New,
            ),
        ),
        alerts = listOf(
            ButlerAlert(
                type = AlertType.Payment,
                priority = AlertPriority.High,
                title = "Set up Alipay before arrival",
                body = "Payment setup prevents friction with taxis, restaurants, and small shops.",
                action = "Review payment setup",
            ),
        ),
        lastUpdatedReason = "Initial VisePanda travel draft.",
    )
}
