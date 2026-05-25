"""价格估算引擎 — 基于知识库的真实价格估算"""
import random

PRICE_DATA = {
    # 门票
    "故宫": 60, "长城": 45, "天坛": 15, "颐和园": 30, "兵马俑": 120,
    "大熊猫基地": 55, "都江堰": 80, "西湖": 0, "灵隐寺": 75, "鼓浪屿": 0,
    "黄鹤楼": 70, "嵩山少林": 100, "张家界": 225, "黄山": 190, "九寨沟": 169,
    "莫高窟": 238, "青海湖": 0, "布达拉宫": 200, "玉龙雪山": 100,
    # 交通
    "高铁_每100km": 45, "飞机_国内": 500, "地铁_次": 4,
    "打车_起步": 12, "打车_perkm": 2.5,
    # 住宿
    "青旅_晚": 80, "快捷_晚": 200, "精品_晚": 400, "五星_晚": 1000,
    # 餐饮
    "早餐": 15, "午餐": 40, "晚餐_普通": 60, "晚餐_豪华": 200,
    "小吃_次": 20, "饮料_杯": 15,
}

def estimate_trip_cost(city: str, days: int = 3, budget_tier: str = "mid") -> dict:
    """估算行程费用"""
    tiers = {"budget": 0.6, "mid": 1.0, "luxury": 1.5}
    multiplier = tiers.get(budget_tier, 1.0)
    
    hotel_tier = {"budget": "青旅_晚", "mid": "精品_晚", "luxury": "五星_晚"}
    meal_tier = {"budget": "小吃_次", "mid": "晚餐_普通", "luxury": "晚餐_豪华"}
    
    hotel = PRICE_DATA.get(hotel_tier.get(budget_tier, "精品_晚"), 400) * multiplier
    meals = (PRICE_DATA.get("早餐", 15) + PRICE_DATA.get(meal_tier.get(budget_tier, "晚餐_普通"), 60)) * days * multiplier
    transport = PRICE_DATA.get("地铁_次", 4) * 4 * days + PRICE_DATA.get("打车_起步", 12) * days * 0.5
    attractions = PRICE_DATA.get("故宫", 60) * 2 * multiplier  # 2 attractions per day
    misc = PRICE_DATA.get("饮料_杯", 15) * 3 * days
    
    total = hotel * days + meals + transport + attractions + misc
    
    return {
        "住宿": round(hotel * days),
        "餐饮": round(meals),
        "交通": round(transport),
        "门票": round(attractions),
        "其他": round(misc),
        "总计": round(total),
        "日均": round(total / days),
        "budget_tier": budget_tier,
    }

def get_weather_advice(city: str, season: str = "spring") -> str:
    """生成天气建议"""
    tips = {
        "beijing": {"spring": "3-4月有沙尘，带口罩", "summer": "炎热40°C+，注意防暑", "autumn": "秋高气爽，最佳季节", "winter": "干冷-10°C，穿羽绒服"},
        "shanghai": {"spring": "梅雨季潮湿，带伞", "summer": "闷热35°C+，室内活动优先", "autumn": "舒适凉爽", "winter": "湿冷，魔法攻击"},
        "chengdu": {"spring": "舒适，偶尔阴雨", "summer": "闷热潮湿，火锅配冰粉", "autumn": "凉爽舒适", "winter": "阴冷多雾"},
        "guangzhou": {"spring": "回南天极潮", "summer": "炎热+台风，注意天气预报", "autumn": "最舒适的季节", "winter": "温暖，短袖即可"},
        "default": "出行前查当地天气预报，备好对应衣物"}
    return tips.get(city, {}).get(season, tips["default"])
