package net.soberanacraft.mod.comands

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.silkmc.silk.commands.command
import net.soberanacraft.mod.*
import net.soberanacraft.mod.SoberanaMod.resetCacheOf
import net.soberanacraft.mod.api.Failure
import net.soberanacraft.mod.api.SoberanaApi
import net.soberanacraft.mod.api.Success
import net.soberanacraft.mod.api.models.LinkMessage
import net.soberanacraft.mod.api.models.LinkStatus
import net.soberanacraft.mod.api.models.Trust
import net.soberanacraft.mod.api.ws.waitUntil
import net.soberanacraft.mod.events.applyEffects
import net.soberanacraft.mod.events.get

object LinkCommand {
    fun register() {
        command("link") {
            runsAsync {
                val caller = source.player
                if (caller == null) {
                    source.sendError("Esse comando só pode ser executado por um jogador.".toComponent())
                }
                var playerInfo = caller.get()

                when (playerInfo.trustFactor) {
                    Trust.Unlinked, Trust.Reffered ->  {
                        caller.sendSystemMessage(Components.Heading.LinkCommand + " Seu status atual é ${playerInfo.trustFactor.name}.")
                        caller.sendSystemMessage(Components.Heading.LinkCommand + " Gerando link único para o jogador: ${playerInfo.nickname}.")
                        val nonce = SoberanaApi.nonce()
                        //NOTE: É safe rodar isso mesmo que não haja nenhuma nonce, porque cada chamada do comando deve gerar e invalidar as nonces anteriores.
                        SoberanaApi.Auth.revokeNonce(caller.uuid)

                        val response = SoberanaApi.Auth.createNonce(caller.uuid, nonce)
                        when (response) {
                            is Failure -> {
                                caller.sendSystemMessage(Components.Heading.LinkCommand + " Um erro aconteceu enquanto o seu código foi gerado. Tente novamente.")
                                caller.sendSystemMessage(Components.Heading.LinkCommand + " Código de erro: ${response.message}")
                            }
                            is Success<*> -> {
                                val auth = SoberanaApi.auth(nonce)
                                caller.sendSystemMessage(Components.Heading.LinkCommand + " " + "Clique aqui ".toLinkComponent(auth)+ "para proseguir.")
                                caller.sendSystemMessage(Components.Heading.LinkCommand + " " + "Esse comando invalida os links que foram enviados anteriromente.".toInfoComponent())

                                val linkStatus = coroutineScope {
                                    async {
                                        waitUntil(playerInfo.uuid)
                                    }
                                }.await()

                                when (linkStatus) {
                                    is Failure  -> {
                                        caller.sendSystemMessage(Components.Heading.LinkCommand + " Um erro aconteceu durante o link com o discord. Tente novamente.")
                                        caller.sendSystemMessage(Components.Heading.LinkCommand + " Código de erro: ${linkStatus.message}")
                                    }
                                    is Success<*> -> {
                                        val status = linkStatus.value as LinkMessage
                                        when (status.linkStatus) {
                                            LinkStatus.InvalidDiscord -> {
                                                caller.sendSystemMessage(Components.Heading.LinkCommand + " Erro ao comunicar com o discord. Tente novamente.")
                                            }
                                            LinkStatus.NotJoinedToGuild ->  {
                                                caller.sendSystemMessage(Components.Heading.LinkCommand + " Você não entrou no servidor do discord da soberana.")
                                                caller.sendSystemMessage(Components.Heading.LinkCommand +
                                                    " " + Components.Styles.Link("https://discord.gg/soberana", "Clique aqui")
                                                    + " e tente novamente após entrar no servidor."
                                                )
                                            }
                                            LinkStatus.JoinedGuild -> {
                                                caller.sendSystemMessage(Components.Heading.LinkCommand + " Conta linkada com sucesso.")
                                                playerInfo = caller.get()
                                                resetCacheOf(caller.uuid, playerInfo)
                                                playerInfo.applyEffects(caller)
                                                caller.sendSystemMessage(Components.Heading.LinkCommand + " O seu status atual é ${playerInfo.trustFactor.name}.")
                                            }
                                            LinkStatus.AlreadyLinked -> {
                                                caller.sendSystemMessage(Components.Heading.LinkCommand + " Sua conta já está linkada com o discord.")
                                                caller.sendSystemMessage(Components.Heading.LinkCommand + " Caso considere isso um erro, contate a staff.")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Trust.Linked, Trust.Trusted -> {
                        caller.sendSystemMessage(Components.Heading.LinkCommand + " Sua conta já está linkada com o discord.")
                        caller.sendSystemMessage(Components.Heading.LinkCommand + " Caso considere isso um erro, contate a staff.")
                    }
                }
            }
        }
    }
}
