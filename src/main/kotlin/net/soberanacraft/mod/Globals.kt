@file:Suppress("RemoveRedundantQualifierName")

package net.soberanacraft.mod

import kotlinx.serialization.json.Json
import net.minecraft.text.*
import net.minecraft.util.Formatting

val Jsoberana = Json {prettyPrint = true }

fun String.toComponent() : MutableText = Text.literal(this)
fun String.toLinkComponent(uri: String) = Components.Styles.Link(uri, this)
fun String.toInfoComponent() = Components.Styles.Info(this.toComponent().setStyle(Components.Styles.GRAY))

operator fun MutableText.plus(other: MutableText) : MutableText = this.copy().append(other)
operator fun MutableText.plus(other: String) : MutableText = this.copy().append(other.toComponent())

object Components {
    object Colors {
        val READ_ONLY_RED = 0xF2706F
        val COMMAND_GREEN = 0x4BF27B
        val LINK = 0x5865F2
        val INFO = 0xA72DDB
    }
    object Styles {
        val GRAY = Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.GRAY))
        fun fromRgb(rgb: Int) = Style.EMPTY.withColor(rgb)
        fun Bracketed (component: MutableText) = "[".toComponent().setStyle(Styles.GRAY) + component + "]".toComponent().setStyle(Styles.GRAY)
        fun Link(uri: String, description: String) = description.toComponent().setStyle(Styles.fromRgb(Colors.LINK).withUnderline(true).withClickEvent(
            ClickEvent(ClickEvent.Action.OPEN_URL,uri)))
        fun Info (component: MutableText) = "Info: ".toComponent().setStyle(Styles.fromRgb(Colors.INFO).withItalic(true)) + component
    }
    object Heading {
        val ReadOnly =  Styles.Bracketed("Somente Leitura".toComponent().setStyle(Styles.fromRgb(Colors.READ_ONLY_RED)))
        val LinkCommand = Styles.Bracketed("Link".toComponent().setStyle(Styles.fromRgb(Colors.COMMAND_GREEN)))
        val InviteCommand = Styles.Bracketed("Invite".toComponent().setStyle(Styles.fromRgb(Colors.COMMAND_GREEN)))
    }
}
