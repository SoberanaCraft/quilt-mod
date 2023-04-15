@file:Suppress("RemoveRedundantQualifierName")

package net.soberanacraft.mod

import eu.pb4.placeholders.api.TextParserUtils
import kotlinx.coroutines.future.asDeferred
import kotlinx.serialization.json.Json
import net.luckperms.api.node.Node
import net.luckperms.api.node.ScopedNode
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.*
import net.minecraft.util.Formatting
import net.soberanacraft.mod.api.Failure
import net.soberanacraft.mod.api.intoString
import net.soberanacraft.mod.api.models.Message
import java.util.UUID

val Jsoberana = Json {prettyPrint = true }

fun String.toComponent() : MutableText = this.stf()
fun String.toLinkComponent(uri: String) = Components.Styles.Link(uri, this)
fun String.toInfoComponent() = Components.Styles.Info(this.toComponent())
fun String.toErrComponent() = Components.Styles.Error(this.toComponent())
fun String.suggestCommand(command: String) = Components.Styles.SuggestCommand("/$command", this)

/// Supremo Tribunal Federal
private fun String.stf() = TextParserUtils.formatText(this).copy()
fun String.rgb(color: String) = "<c:#$color>$this</c>"
fun String.color(color: Formatting) = "<c:#${color.colorValue!!.toString(16)}>$this</c>"
fun String.underline() = "<underline>$this</underline>"
fun String.bold() = "<b>$this</b>"
fun String.italic() = "<i>$this</i>"
fun String.url(uri: String) = "<url:'$uri'>$this</url>"


operator fun MutableText.plus(other: MutableText) : MutableText = this.copy().append(other)
operator fun MutableText.plus(other: String) : MutableText = this.copy().append(other.stf())

fun Failure.Failed(heading: MutableText, where: String, to: ServerPlayerEntity, action: (ServerPlayerEntity, MutableText) -> Unit)  {
    action(to, heading + " Um erro aconteceu durante: \"${where.rgb(Components.Colors.INFO)}\"".stf())
    action(to, heading + " Código de erro: ${this.message.intoString().rgb(Components.Colors.INFO)}".stf())
}

fun Message.fail(heading: MutableText, where: String, to: ServerPlayerEntity) {
    to.sendSystemMessage(heading + " Um erro aconteceu durante: \"${where.rgb(Components.Colors.INFO)}\"".stf())
    to.sendSystemMessage(heading + " Código de erro: ${this.intoString().rgb(Components.Colors.INFO)}".stf())
    to.playSound(SoundEvents.BLOCK_BELL_RESONATE)
}

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
        fun SuggestCommand(command: String, component: String) = "<cmd:'$command'>$component</cmd>".stf()
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

object Role {
    object Name {
        val READ_ONLY = "ro"
        val MEMBER = "member"
        val TRUSTED = "trusted"
        val REFERRED = "convidado"
    }

    val READ_ONLY = Name.READ_ONLY.toRole()
    val MEMBER = Name.MEMBER.toRole()
    val TRUSTED = Name.TRUSTED.toRole()
    val REFERRED = Name.REFERRED.toRole()

    private fun String.toRole() : String = "group.$this"
    fun String.toNode(): ScopedNode<*, *> = Node.builder(this).build()
    object Async {
        suspend fun UUID.hasRole(roleName: String) : Boolean {
            val user = SoberanaMod.LuckPerms.userManager.loadUser(this).asDeferred().await()
            return user.getInheritedGroups(user.queryOptions).any { it.name == roleName }
        }
        suspend fun UUID.addRole(role: String) {
            val user = SoberanaMod.LuckPerms.userManager.loadUser(this).asDeferred().await()
            user.data().add(role.toNode())
            SoberanaMod.LuckPerms.userManager.saveUser(user).asDeferred().await()
        }

        suspend fun UUID.removeRole(role: String) {
            val user = SoberanaMod.LuckPerms.userManager.loadUser(this).asDeferred().await()
            user.data().remove(role.toNode())
            SoberanaMod.LuckPerms.userManager.saveUser(user).asDeferred().await()
        }
    }

}

fun ServerPlayerEntity.isAuthenticated()  = SoberanaMod.AUTHENTICATED_PLAYERS.contains(this.uuid)
fun ServerCommandSource.updateCommandTree() =  this.server.playerManager.sendCommandTree(this.player)
