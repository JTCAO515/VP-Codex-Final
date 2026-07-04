package space.go2china.visepanda.ui.translate

data class Phrase(
    val english: String,
    val chinese: String,
    val pinyin: String
)

data class PhraseCategory(
    val name: String,
    val icon: String,
    val phrases: List<Phrase>
)

object PhraseDictionary {
    val categories = listOf(
        PhraseCategory(
            name = "💡 Essential",
            icon = "💡",
            phrases = listOf(
                Phrase("Thank you.", "谢谢。", "Xièxie."),
                Phrase("You're welcome.", "不客气。", "Bú kèqi."),
                Phrase("Hello.", "你好。", "Nǐ hǎo."),
                Phrase("Where is the toilet?", "洗手间在哪里？", "Xǐshǒujiān zài nǎlǐ?"),
                Phrase("Excuse me / Sorry.", "对不起 / 打扰一下。", "Duìbùqǐ / Dǎrǎo yíxià."),
                Phrase("Help!", "救命！", "Jiùmìng!")
            )
        ),
        PhraseCategory(
            name = "🛒 Shopping & Pay",
            icon = "🛒",
            phrases = listOf(
                Phrase("How much is this?", "这个多少钱？", "Zhège duōshǎo qián?"),
                Phrase("Do you accept WeChat Pay/Alipay?", "支持微信/支付宝付款吗？", "Zhīchí Wēixìn/Zhīfùbǎo fùkuǎn ma?"),
                Phrase("Can I pay with credit card?", "可以刷信用卡吗？", "Kěyǐ shuā xìnyòngkǎ ma?"),
                Phrase("Can you make it cheaper?", "便宜一点可以吗？", "Piányi yìdiǎn kěyǐ ma?"),
                Phrase("I want this.", "我要这个。", "Wǒ yào zhège.")
            )
        ),
        PhraseCategory(
            name = "🚕 Transport",
            icon = "🚕",
            phrases = listOf(
                Phrase("Please take me to this address.", "请带我到这个地址。", "Qǐng dài wǒ dào zhège dìzhǐ."),
                Phrase("Please turn on the meter.", "请打表。", "Qǐng dǎbiǎo."),
                Phrase("Stop here, please.", "请在这里停车。", "Qǐng zài zhèlǐ tíngchē."),
                Phrase("Where is the subway station?", "地铁站在哪里？", "Dìtiězhàn zài nǎlǐ?"),
                Phrase("I want to buy a ticket.", "我想买张票。", "Wǒ xiǎng mǎi zhāng piào.")
            )
        ),
        PhraseCategory(
            name = "🍲 Food",
            icon = "🍲",
            phrases = listOf(
                Phrase("Please give me the menu.", "请给我菜单。", "Qǐng gěi wǒ càidān."),
                Phrase("Is this spicy?", "这个辣吗？", "Zhège là ma?"),
                Phrase("Please make it not spicy.", "请做不辣的。", "Qǐng zuò bú là de."),
                Phrase("Water, please.", "请给我水。", "Qǐng gěi wǒ shuǐ."),
                Phrase("Check, please.", "买单。", "Mǎidān.")
            )
        ),
        PhraseCategory(
            name = "🏨 Hotel",
            icon = "🏨",
            phrases = listOf(
                Phrase("I have a reservation.", "我预订了房间。", "Wǒ yùdìng le fángjiān."),
                Phrase("Where is the elevator?", "电梯在哪里？", "Diàntī zài nǎlǐ?"),
                Phrase("The Wi-Fi is not working.", "无线网络用不了。", "Wúxiàn wǎngluò yòng bù liǎo."),
                Phrase("Can I leave my bags here?", "可以寄放行李吗？", "Kěyǐ jìfàng xíngli ma?"),
                Phrase("I want to check out.", "我要退房。", "Wǒ yào tuìfáng.")
            )
        )
    )
}
