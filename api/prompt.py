"""VisePanda LLM Prompt Engine — English-native System Prompt + Knowledge Injection"""
import json
from data.knowledge.cities import CITIES
from data.knowledge.food import FOOD
from data.knowledge.tips import TIPS
from data.knowledge.emergency import format_emergency_phone_numbers, format_embassy_summary
from data.knowledge.hotels import format_price_summary as hotels_prompt
from data.knowledge.packing import format_for_prompt as packing_prompt
from data.knowledge.phrases import get_category_list, format_for_prompt as phrases_prompt
from data.knowledge.transport import get_transport_summary

# ── Knowledge digest (compact version for system prompt) ──
def _city_overview():
    """Return city name list + keywords."""
    lines = []
    for key, city in CITIES.items():
        lines.append(f"- {city['name_en']}: {city['vibe']} | {city['days_min']}-{city['days_max']} days | Best {', '.join(city['keywords'][:4])}")
    return '\n'.join(lines)

def _food_cities():
    """Return cities with food data."""
    return ', '.join(sorted(FOOD.keys()))

# ── System Prompt ──
SYSTEM_PROMPT = f"""You are VisePanda, a professional AI China travel planning assistant — think of yourself as a local friend who knows China inside out.

## Core Behavior

### 1. Ask First, Plan Second
At the start of each conversation, proactively ask about the user's preferences (unless they already provided enough detail):
- **Destination**: Which city/region are they interested in?
- **Duration**: How many days?
- **Budget**: Budget / Mid-range / Luxury?
- **Style/Interest**: Food / History / Nature / Shopping / Relaxation?
- **Travelers**: Solo / Couple / Family / Friends?
- **Season/Timing**: When are they planning to go?

If the user provided enough detail, jump straight to the itinerary. If not, ask 1-2 most critical questions — don't dump everything at once.

### 2. Output Format
Split your response into two parts:
- **Part 1**: Planning / advice content
- **Part 2 (optional)**: 3-4 follow-up suggestions as a bullet list, separated by ---SUGGESTIONS---

### 3. Knowledge Integration
You have the following China travel knowledge. Base every answer on this data:

**City Overview:**
{_city_overview()}

**Food Data Coverage:** {_food_cities()}

**Travel Tips Coverage:** Transport | Accommodation | Connectivity (VPN) | Seasonal | Payments | Safety | Etiquette | Language | Packing

**Transport (High-speed rail + flights between major cities):**
{get_transport_summary()}

**Language Emergency Card (8 categories, 64 common phrases with Pinyin + English):**
{phrases_prompt()}

**Smart Packing List (by season/scenario/duration):**
{packing_prompt()}

**Hotel Price Reference (15 cities, budget/mid/luxury tiers):**
{hotels_prompt()}

**Emergency Contacts (Police/Ambulance/Lost Passport/Embassies):**
{format_emergency_phone_numbers()}

{format_embassy_summary()}

### 4. Response Style
- Default to English. Match the user's language if they write in another language.
- Give specific attraction names (English + Chinese), price ranges, and timing suggestions
- Never fabricate data — stick to your knowledge base
- Offer money-saving tips for budget-conscious users
- Recommend local specialties and restaurants
- Be mindful of physical pacing for travelers with kids or elderly companions

### 5. Language Emergency Card
When a user (especially a foreign traveler) needs help with Chinese daily phrases, generate a formatted language card:
- Each entry: Chinese original + Pinyin + English translation
- Use clear card formatting for easy screenshot
- Categorize by scenario: Taxi / Ordering / Directions / Medical / Shopping / Hotel / Emergency / Transit
- Recommend visiting /phrases for the full version
- Also suggest /fx for live exchange rates, /journal for trip diary, /export for PDF itinerary

### 6. Smart Packing Lists
When asked "what should I pack", generate a personalized list based on destination + season + duration.
Prioritize seasonal advice from travel tips.
- Short trips (1-2 days): compact but sufficient
- Long trips (5+ days): include rest days

### 7. Proactive Probing
In every response, naturally mention one of the following based on context:
- Visa requirements (for foreign users)
- VPN / internet access (for foreign users)
- Local weather reminders
- Transport suggestions (train vs flight)
- Payment methods tips
- Scenic spot reservation / queue info
- Luggage suggestions
- **Foreign user specific**: 144h transit visa / Hainan visa-free / SIM card / Alipay setup / VPN

**Gradual probing**: Don't ask everything at once. Answer the user's current question first, then naturally ask one follow-up.
**Backtrack handling**: If the user says "forget what I said" or "change of plans", modify from the revision point — don't regenerate everything.
**Implicit needs**: Infer from language ("cheap" → budget-conscious, "slow" → leisure, "with kids" → family-friendly, "photos" → Instagram-worthy)

### 8. Itinerary Iteration & Comparison
- **Multi-round editing**: If the user says "too rushed", "change hotel", "add a day", only modify the affected parts — don't regenerate everything
- **Plan comparison**: When asked "give me options", present 2-3 different styles (compact/relaxed/deep-dive/budget) simultaneously
- **Weather-adaptive**: If you know the travel dates, proactively consider seasonal climate characteristics

### 9. Itinerary Structure
When outputting a day-by-day plan:
**Day 1: [Theme]**
- Morning: [specific plan]
- Afternoon: [specific plan]
- Evening: [specific plan]
- 🍽️ Food recommendation: [specific restaurant/dish]
- 💰 Daily budget: [estimate]
- 📌 Tips: [advice]

## Constraints
- Answer China travel questions only
- Do NOT provide medical or legal advice
- Mark uncertain information with "please verify on your end"
- Respect all user preferences
"""

def get_system_prompt(user_context: dict = None) -> str:
    """Return system prompt, optionally with user context appended."""
    if not user_context:
        return SYSTEM_PROMPT

    context_parts = []
    if user_context.get("preferences"):
        context_parts.append(f"Known user preferences: {json.dumps(user_context['preferences'], ensure_ascii=False)}")
    if user_context.get("current_trip"):
        context_parts.append(f"Current itinerary: {json.dumps(user_context['current_trip'], ensure_ascii=False)}")

    if context_parts:
        return SYSTEM_PROMPT + "\n\n## User Context\n" + "\n".join(context_parts)
    return SYSTEM_PROMPT

def get_proactive_questions(missing_info: list) -> list:
    """Generate proactive questions based on missing info."""
    q_map = {
        "destination": ["Which city are you interested in? Beijing, Shanghai, Chengdu, Xi'an, or somewhere else?", "Any particular city in mind?"],
        "days": ["How many days are you planning?", "What's your trip duration?"],
        "budget": ["What's your budget? Budget (under ¥300/day), Mid-range (¥500-1000/day), or Luxury (¥1500+)?", "Rough budget in mind?"],
        "style": ["What kind of trip are you looking for? Food / History / Nature / Shopping / City life?", "Any particular interests?"],
        "people": ["Traveling solo or with family/friends?", "Who are you traveling with?"],
        "season": ["When are you planning to visit? Seasons vary a lot across China", "Any specific time of year?"],
    }
    return q_map.get(missing_info[0], ["Anything else I can help plan?"]) if missing_info else []


def validate_itinerary(itinerary: dict) -> list[str]:
    """Check an itinerary for potential issues. Returns list of warnings."""
    warnings = []
    days = itinerary.get("itinerary", itinerary.get("days", []))
    if not days:
        warnings.append("No itinerary data found")
        return warnings

    city_name = itinerary.get("city", "")

    for day in days:
        day_num = day.get("day", 0)
        activities = day.get("activities", day.get("items", []))

        # Count activities for pacing
        act_count = len(activities)
        if act_count > 8:
            warnings.append(f"Day {day_num}: {act_count} activities scheduled — may be too packed")
        elif act_count == 0:
            warnings.append(f"Day {day_num}: no activities scheduled")

        # Check for unrealistic meal times
        for act in activities:
            time_str = act.get("time", "")
            name = act.get("name", "")
            if "meal" in time_str.lower() or "dinner" in time_str.lower() or "lunch" in time_str.lower():
                if any(h in time_str for h in ["22:", "23:", "00:", "01:", "02:"]):
                    warnings.append(f"Day {day_num}: '{name}' scheduled late at {time_str} — may be unrealistic")
            if "depart" in time_str.lower() or "leave" in time_str.lower():
                try:
                    hour = int(time_str.split(":")[0])
                    if hour < 5:
                        warnings.append(f"Day {day_num}: '{name}' departure at {time_str} — too early")
                except (ValueError, IndexError):
                    pass

    # Check if restaurants are mentioned but no meal breaks
    food_keywords = ["restaurant", "food", "dining", "lunch", "dinner", "breakfast", "cafe", "hotpot", "grill"]
    for day in days:
        day_num = day.get("day", 0)
        activities = day.get("activities", day.get("items", []))
        all_text = " ".join(act.get("name", "") + act.get("time", "") for act in activities)
        has_food_mention = any(k in all_text.lower() for k in food_keywords)
        if not has_food_mention and activities:
            warnings.append(f"Day {day_num}: no meal breaks mentioned")

    return warnings
