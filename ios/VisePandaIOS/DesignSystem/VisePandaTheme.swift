import SwiftUI

enum VPColor {
    static let paper = Color(hex: 0xFAF8F4)
    static let paperSoft = Color(hex: 0xFFFFFF)
    static let paperWarm = Color(hex: 0xFFF8E6)
    static let ink = Color(hex: 0x1C1410)
    static let inkMuted = Color(hex: 0x3A3028)
    static let inkSoft = Color(hex: 0x7A6558)
    static let cinnabar = Color(hex: 0xC1292E)
    static let cinnabarDeep = Color(hex: 0xA02226)
    static let gold = Color(hex: 0xC9A84C)
    static let sage = Color(hex: 0x667B5C)
    static let outline = Color(hex: 0xD8CCC0)
}

enum VPSpacing {
    static let xs: CGFloat = 6
    static let sm: CGFloat = 10
    static let md: CGFloat = 14
    static let lg: CGFloat = 18
    static let xl: CGFloat = 24
    static let bottomBarClearance: CGFloat = 88
}

enum VPRadius {
    static let sm: CGFloat = 10
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let pill: CGFloat = 999
}

enum VPFont {
    static func display(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom("PlayfairDisplayRoman-Regular", size: size).weight(weight)
    }

    static func body(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom("DMSans-Regular", size: size).weight(weight)
    }
}

extension Color {
    init(hex: UInt32, alpha: Double = 1) {
        let red = Double((hex >> 16) & 0xff) / 255
        let green = Double((hex >> 8) & 0xff) / 255
        let blue = Double(hex & 0xff) / 255
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: alpha)
    }
}

struct VPCard<Content: View>: View {
    let padding: CGFloat
    @ViewBuilder let content: Content

    init(padding: CGFloat = VPSpacing.lg, @ViewBuilder content: () -> Content) {
        self.padding = padding
        self.content = content()
    }

    var body: some View {
        content
            .padding(padding)
            .background(VPColor.paperSoft)
            .clipShape(RoundedRectangle(cornerRadius: VPRadius.md, style: .continuous))
            .overlay {
                RoundedRectangle(cornerRadius: VPRadius.md, style: .continuous)
                    .stroke(VPColor.outline.opacity(0.7), lineWidth: 1)
            }
            .shadow(color: VPColor.ink.opacity(0.08), radius: 8, x: 0, y: 4)
    }
}

struct VPChip: View {
    let title: String
    var selected = false

    var body: some View {
        Text(title)
            .font(VPFont.body(13, weight: .semibold))
            .foregroundStyle(selected ? VPColor.paperSoft : VPColor.inkSoft)
            .padding(.horizontal, 14)
            .padding(.vertical, 9)
            .background(selected ? VPColor.ink : VPColor.paperWarm)
            .clipShape(Capsule())
    }
}

struct VPStatusPill: View {
    let title: String
    var tone: Tone = .neutral

    enum Tone {
        case neutral
        case ready
        case warning
        case red
    }

    var body: some View {
        Text(title)
            .font(VPFont.body(11, weight: .bold))
            .foregroundStyle(foreground)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(background)
            .clipShape(Capsule())
    }

    private var foreground: Color {
        switch tone {
        case .neutral:
            VPColor.inkSoft
        case .ready:
            VPColor.sage
        case .warning:
            VPColor.gold
        case .red:
            VPColor.cinnabar
        }
    }

    private var background: Color {
        switch tone {
        case .neutral:
            VPColor.paperWarm
        case .ready:
            VPColor.sage.opacity(0.13)
        case .warning:
            VPColor.gold.opacity(0.15)
        case .red:
            VPColor.cinnabar.opacity(0.12)
        }
    }
}
