@file:Suppress("RemoveRedundantQualifierName")

package net.soberanacraft.mod

import eu.pb4.placeholders.api.TextParserUtils
import kotlinx.serialization.json.Json
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.*

val Jsoberana = Json {prettyPrint = true }

fun String.toComponent() : MutableText = Text.literal(this)
fun String.toLinkComponent(uri: String) = Components.Styles.Link(uri, this)
fun String.toInfoComponent() = Components.Styles.Info(this.toComponent())
fun String.toErrComponent() = Components.Styles.Error(this.toComponent())

/// Supremo Tribunal Federal
fun String.stf() = TextParserUtils.formatText(this).copy()
fun String.rgb(color: String) = "<c:#$color>$this</c>"
fun String.underline() = "<underline>$this</underline>"
fun String.bold() = "<b>$this</b>"
fun String.italic() = "<i>$this</i>"
fun String.url(uri: String) = "<url:$uri>$this</url>"


operator fun MutableText.plus(other: MutableText) : MutableText = this.copy().append(other)
operator fun MutableText.plus(other: String) : MutableText = this.copy().append(other.toComponent())

object Components {
    object Colors {
        val READ_ONLY_RED = "F2706F"
        val COMMAND_GREEN = "4BF27B"
        val LINK = "5865F2"
        val INFO = "A72DDB"
        val AUTH = "D9902E"
        var ERR = "FF0000"
        val ADMIN = "7D21Bf"
    }

    object Styles {
        fun Bracketed(component: MutableText) = "<gray>[</gray>".stf() + component + "<gray>]</gray>".stf()
        fun Link(uri: String, description: String) = description.url(uri).underline().rgb(Colors.LINK).stf()
        fun Info(component: MutableText) = "Info: ".rgb(Colors.INFO).italic().stf() + component
        fun Error(component: MutableText) = "Erro: ".rgb(Colors.ERR).bold().underline() + component

    }
    object Heading {
        val ReadOnly = Styles.Bracketed("Somente Leitura".rgb(Colors.READ_ONLY_RED).stf())
        val LinkCommand = Styles.Bracketed("Link".rgb(Colors.LINK).stf())
        val InviteCommand = Styles.Bracketed("Invite".rgb(Colors.COMMAND_GREEN).stf())
        val Registrar = Styles.Bracketed("Registrar".rgb(Colors.AUTH).stf())
        val Login = Styles.Bracketed("Login".rgb(Colors.AUTH).stf())
        val MudarSenha = Styles.Bracketed("Mudar Senha".rgb(Colors.AUTH).stf())
        val Admin = ("\\<".rgb(Colors.ERR) + "Admin".rgb(Colors.ADMIN).bold() + "\\>".rgb(Colors.ERR)).stf()

    }
}

fun ServerPlayerEntity.isAuthenticated()  = SoberanaMod.AUTHENTICATED_PLAYERS.contains(this.uuid)
