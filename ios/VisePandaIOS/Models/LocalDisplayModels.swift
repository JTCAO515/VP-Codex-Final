import Foundation

enum LocalDisplayCardKind: String {
    case address
    case allergy
    case symptom
}

struct LocalDisplayCard: Identifiable {
    var id: String { "\(kind.rawValue)-\(headline)-\(detail ?? "")" }
    var kind: LocalDisplayCardKind
    var title: String
    var headline: String
    var detail: String?
    var disclaimer: String?
    var showEmergencyAction: Bool = false

    var copyText: String {
        [headline, detail, disclaimer]
            .compactMap { $0?.isEmpty == false ? $0 : nil }
            .joined(separator: "\n")
    }
}

enum DietaryRestriction: String, CaseIterable, Identifiable {
    case peanut
    case treeNut
    case seafood
    case dairy
    case gluten
    case spicyIntolerant
    case vegetarian
    case halal
    case kosher

    var id: String { rawValue }

    var label: String {
        switch self {
        case .peanut: "Peanut"
        case .treeNut: "Tree nuts"
        case .seafood: "Seafood"
        case .dairy: "Dairy"
        case .gluten: "Gluten"
        case .spicyIntolerant: "No spicy food"
        case .vegetarian: "Vegetarian"
        case .halal: "Halal"
        case .kosher: "Kosher"
        }
    }

    var chinese: String {
        switch self {
        case .peanut: "我对花生过敏，请不要放花生，谢谢。"
        case .treeNut: "我对坚果过敏，请不要放坚果类食材。"
        case .seafood: "我对海鲜过敏，请不要放海鲜类食材。"
        case .dairy: "我对乳制品过敏，请不要放牛奶、奶油或奶酪。"
        case .gluten: "我不能吃含麸质的食物，请不要放小麦、面粉或面筋。"
        case .spicyIntolerant: "我不能吃辣，请做无辣或微辣。"
        case .vegetarian: "我吃素，请不要放肉类和肉汤。"
        case .halal: "我只吃清真食品，请确认食材和调料符合清真要求。"
        case .kosher: "我只吃符合犹太洁食要求的食物，请帮我确认食材。"
        }
    }
}

enum CommonSymptom: String, CaseIterable, Identifiable {
    case headache
    case stomachache
    case diarrhea
    case fever
    case nausea
    case allergicReaction
    case difficultyBreathing

    var id: String { rawValue }

    var label: String {
        switch self {
        case .headache: "Headache"
        case .stomachache: "Stomachache"
        case .diarrhea: "Diarrhea"
        case .fever: "Fever"
        case .nausea: "Nausea"
        case .allergicReaction: "Allergic reaction"
        case .difficultyBreathing: "Difficulty breathing"
        }
    }

    var chinese: String {
        switch self {
        case .headache: "我头疼。"
        case .stomachache: "我肚子疼。"
        case .diarrhea: "我拉肚子。"
        case .fever: "我发烧了。"
        case .nausea: "我恶心想吐。"
        case .allergicReaction: "我可能过敏了，身上起了疹子/肿了。"
        case .difficultyBreathing: "我呼吸困难，请帮我叫救护车。"
        }
    }

    var isEmergency: Bool {
        self == .difficultyBreathing
    }
}
