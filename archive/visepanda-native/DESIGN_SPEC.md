# VisePanda Design Spec — 浅色东方

> 版本: v2.0 | 日期: 2026-06-17
> 方向确认: 方案C (Light Oriental)
> 灵感参考: DM Sans + DM Serif Display 字体系统, 米白底色+金色点缀

---

## 1. Design Dial

```
VISUAL_DENSITY:  4   (中等密度，白底卡片式布局，呼吸感)
DESIGN_VARIANCE: 5   (对称为主，卡片+列表变化)
MOTION_INTENSITY: 4  (轻量动效，过渡平滑，不过度)
```

## 2. Color Palette

| Token | Hex | Use |
|-------|-----|-----|
| background | #F5F0E8 | 页面主底色 (warm cream) |
| surface | #FFFFFF | 卡片/容器/列表项 |
| surfaceElevated | #FAF7F3 | 悬停态/高亮卡片 |
| primary (Gold) | #C9A96E | 品牌CTA/选中态/高亮 |
| primaryDark | #B89255 | CTA pressed态 |
| primaryLight | #DCC798 | 品牌浅色背景块 |
| secondary (JadeGrey) | #8B8B7A | 辅助文字/次级标签 |
| tertiary (JadeGreen) | #5B7B5A | 成功状态/自然元素 |
| textPrimary | #2D2D2D | 主要标题/正文 (deep charcoal) |
| textSecondary | #6B6B5E | 次要正文/描述 |
| textTertiary | #9C9A94 | 占位符/辅助信息 |
| border | #E8E0D0 | 卡片/分割线 (warm light) |
| borderLight | #F0E8D8 | 更轻的分割 |
| error | #D9534F | 错误态 |
| success | #5B7B5A | 成功态 (复用竹青) |

## 3. Typography

| Role | Font | Size | Weight | Line Height | Tracking | Use |
|------|------|------|--------|-------------|----------|-----|
| Display XL | DM Serif Display | 36sp | 400 | 40sp | -0.5sp | Hero大标题 |
| Display Large | DM Serif Display | 28sp | 400 | 34sp | -0.3sp | 区段标题 |
| Headline | DM Sans | 22sp | 600 | 28sp | -0.2sp | 卡片标题 |
| Subhead | DM Sans | 18sp | 500 | 24sp | 0 | 列表标题 |
| Body | DM Sans | 16sp | 400 | 24sp | 0 | 正文 |
| Body Small | DM Sans | 14sp | 400 | 20sp | 0 | 辅助说明 |
| Caption | DM Sans | 12sp | 500 | 16sp | 0.2sp | 标签/角标 |
| Tab Label | DM Sans | 10sp | 600 | 12sp | 0.3sp | 底部导航标签 |

**Font Family:** DM Sans (body/UI) + DM Serif Display (display/headlines only)

## 4. Spacing

| Token | dp |
|-------|----|
| xs | 4 |
| sm | 8 |
| md | 12 |
| lg | 16 |
| xl | 24 |
| xxl | 32 |
| section | 48 |

## 5. Radius

| Token | dp | Use |
|-------|----|-----|
| xs | 4 | 标签 |
| sm | 8 | 按钮/输入框 |
| md | 12 | 卡片标准 |
| lg | 16 | 大卡片 |
| xl | 24 | Hero/头部 |
| pill | 9999dp | CTA按钮/Chic |

## 6. Elevation Shadows

| Level | Value | Use |
|-------|-------|-----|
| 0 | none | 页面背景 |
| 1 | 0 1px 3px rgba(45,45,45,0.08) | 轻卡片 |
| 2 | 0 2px 8px rgba(45,45,45,0.10) | 浮起卡片 |
| 3 | 0 4px 16px rgba(45,45,45,0.12) | 弹窗/BottomSheet |

## 7. Component Specs

### Button — Primary (Gold Pill)
```
background:   #C9A96E → pressed: #B89255
text:         white
radius:       9999px
padding:      12dp 24dp
font:         15sp DM Sans 600
shadow:       level 1
```

### Button — Secondary (Ghost)
```
background:   transparent, border 1dp #E8E0D0
text:         #6B6B5E
radius:       9999px
padding:      12dp 24dp
font:         15sp DM Sans 500
```

### City Card
```
background:   white
border:       1dp #E8E0D0
radius:       12dp
shadow:       level 1
content:      image area (220dp) + bottom overlay with city name
tags:         small rounded pill (border gold/jade green)
```

### Bottom Navigation
```
background:   white
border-top:   0.5dp #E8E0D0
icon:         20dp
label:        10sp DM Sans 600, 0.3sp tracking
selected:     #C9A96E (icon + label)
unselected:   #8B8B7A
chat FAB:     40dp gold circle, -4dp top margin
```

### Chip
```
background:   white
border:       1dp #C9A96E (gold), #5B7B5A (green), #E8E0D0 (neutral)
text:         accent color
radius:       9999dp
padding:      8dp 16dp
```

### Section Header
```
left:         title (Headline/Subhead)
right:        optional "See all" link (Caption, #8B8B7A)
margin:       bottom 12dp
```

### Shimmer
```
base:         #F0E8D8 (warm light grey)
highlight:    #FAF7F3
animation:    sweep left to right, 1.2s cycle
radius:       matches parent card radius
```

## 8. Page Layout Specs

### Home Screen (自上而下)
```
Status Bar          → transparent bg, dark icons (default system)
Hero Section        → padding 24dp top, 56dp bottom
  Brand logo        → 32dp gold circle with "V"
  Headline          → 36sp DM Serif Display, "Your AI China Travel Companion"
  Subtitle          → 16sp DM Sans, #6B6B5E
  CTA Button        → gold pill, "Plan Your Trip", full-width
Featured Cities     → horizontal LazyRow, 160dp × 200dp cards
  City Card         → image area (gradient fallback) + name + tag
Inspiration         → 2 card layout (First Time / Food / Culture)
  Card              → white surface + icon + title + description
Essentials          → 3-col grid
  Icon tile         → white rounded card + emoji + label
Bottom Nav          → 4 tabs with gold FAB for Chat
```

### Explore Screen
```
Tab bar             → Cards / Map toggle
City Grid           → 2-col LazyVerticalGrid
  Card              → 155dp × 190dp, rounded 12dp, white bg
Map View            → osmdroid full width
  Marker            → gold pin
  Info Popup        → white card, rounded 12dp
```

### Chat Screen
```
Context Bar         → white, city/行程标签
Suggestion Chips    → gold outline, horizontally scrollable
Message List        → scrollable reverse
  User message      → right aligned, gold bg, white text
  AI message        → left aligned, white card, #2D2D2D text
  Itinerary Block   → gold left border, white card
Input Bar           → white bg, top border, rounded input + send button
```

### City Detail
```
Hero Image          → full width, 240dp height, bottom rounded 24dp
City Name           → 28sp DM Serif Display
Description         → one-line, 16sp #6B6B5E
Stats Bar           → white card with days + budget
Content Sections    → Must-see / Must-eat / Stay / Tips
  Each section      → white card, 12dp radius, with icon header
CTA                 → gold pill floating at bottom "Plan my trip"
```

### Trips Screen
```
Section Header      → "Recent" + "Saved" tabs
Trip Card           → white surface with gold left border
  Content           → title (Headline) + city·days (Caption) + preview (2 lines)
  Delete            → swipe to delete or long press
Empty State         → illustration + "Start planning" CTA
```

## 9. Transition & Animation Specs

| Action | Animation | Timing |
|--------|-----------|--------|
| App Launch | Splash fade-out + Hero fade-in | 0.8s |
| Tab Switch | Cross-fade + content stagger | 0.3s |
| Card Tap | Scale 0.97 on press + release | 0.15s |
| Chat Typing | Token characters cascade | 30ms/char |
| Modal (City Detail) | Slide up from bottom + bg dim | 0.35s |
| Trip Save | Bottom toast slide-up | 0.25s |
| Button Press | Scale 0.95 + shadow reduce | 0.1s |

## 10. Dark Mode (Future)

When dark mode is needed later:
```
background:   #1A1A1A
surface:      #232323  
textPrimary:  #F5F0E8
textSecondary:#D4CEC4
border:       #3A3A3A
gold:         #C9A96E (unchanged)
```

---

> 本设计规范对应 Compose Design System 中的 Color.kt / Type.kt / Theme.kt 实现
> 所有 token 值在设计系统中定义为常量，Figam 设计参照本规范
