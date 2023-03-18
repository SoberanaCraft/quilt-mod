package net.soberanacraft.mod.comands

import net.minecraft.command.argument.GameProfileArgumentType.GameProfileArgument
import net.silkmc.silk.commands.command
import net.soberanacraft.mod.Components
import net.soberanacraft.mod.api.Failure
import net.soberanacraft.mod.api.SoberanaApi
import net.soberanacraft.mod.api.Success
import net.soberanacraft.mod.api.intoString
import net.soberanacraft.mod.api.models.Trust
import net.soberanacraft.mod.events.get
import net.soberanacraft.mod.plus

object InviteCommand {
    fun register() {
        command("invite") {
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
                                           player.sendSystemMessage(Components.Heading.InviteCommand + " Um erro aconteceu durante a execução do comando. Tente novamente.")
                                           player.sendSystemMessage(Components.Heading.InviteCommand + " Código do erro: ${refereeResponse.message.intoString()}")
                                       }
                                       else -> {
                                           player.sendSystemMessage(Components.Heading.InviteCommand + " Jogador ${target.name} convidado com sucesso.")
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
