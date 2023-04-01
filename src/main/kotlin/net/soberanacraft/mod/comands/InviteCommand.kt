package net.soberanacraft.mod.comands

import net.minecraft.command.argument.GameProfileArgumentType.GameProfileArgument
import net.silkmc.silk.commands.command
import net.soberanacraft.mod.*
import net.soberanacraft.mod.api.*
import net.soberanacraft.mod.api.models.Trust
import net.soberanacraft.mod.events.get

object InviteCommand {
    fun register() {
        command("invite") {
            requires { it.player.isAuthenticated()}
            requires { it.permission("soberana.trusted.invite") }

            argument<GameProfileArgument>("player") { arg ->
               runsAsync {
                   val player = source.player
                   val target = arg().getNames(this.source).first()

                   if (player.uuid == target.id) {
                       player.sendSystemMessage(Components.Heading.InviteCommand + " Você não pode convidar à sí mesmo.")
                       return@runsAsync
                   }

                   val trust = player.get()



                   when (trust.trustFactor) {
                       Trust.Unlinked, Trust.Linked, Trust.Reffered ->  {
                           player.sendSystemMessage(Components.Heading.InviteCommand + " Você não pode executar esse comando ainda.")
                       }
                       Trust.Trusted ->  {
                           val response = SoberanaApi.player(target.id)
                           when (response) {
                               is Failure -> {
                                   player.sendSystemMessage(Components.Heading.InviteCommand + " O jogador ${target.name} nunca entrou no servidor.")
                                   player.sendSystemMessage(Components.Heading.InviteCommand + " Tente novamente assim que esse jogador estiver online.")
                               }
                               is Success<*> -> {
                                   val refereeResponse = SoberanaApi.Auth.addReferee(target.id, player.uuid)
                                   when (refereeResponse) {
                                       is Failure -> {
                                           refereeResponse.Failed(Components.Heading.InviteCommand, "criar o convite", player) { p, msg ->
                                               p.sendSystemMessage(msg)
                                           }
                                       }
                                       else -> {
                                           val targetPlayerEntity = source.server.playerManager.getPlayer(target.id)
                                           player.sendSystemMessage(Components.Heading.InviteCommand + " Jogador ${target.name} convidado com sucesso.")
                                           targetPlayerEntity?.sendSystemMessage(Components.Heading.InviteCommand + " Você foi convidade por " +
                                               "${player.gameProfile.name.rgb(Components.Colors.COMMAND_GREEN)}!")
                                           targetPlayerEntity?.sendSystemMessage(Components.Heading.InviteCommand + " Registre sua conta com [/registrar <senha> <repetirSenha>]")
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
            }
        }
    }
}
