package net.soberanacraft.mod.comands

import net.silkmc.silk.commands.command
import net.soberanacraft.mod.*
import net.soberanacraft.mod.api.*

object LoginCommand {
    fun register() {
        command("login") {
            requires { it.player.isAuthenticated().not() }
            requires { it.permission("soberana.account.login") }

            argument<String>("senha") { arg ->
                runsAsync {
                    val caller = source.player

                    var response = SoberanaApi.Auth.isRegistered(caller.uuid)

                    when (response) {
                        is Failure -> {
                            response.Failed(Components.Heading.Login, "verificar seu login", caller) { player, msg ->
                                player.sendSystemMessage(msg)
                            }
                            return@runsAsync
                        }
                        is Success<*> -> {
                            val registered = (response.value as Boolean)
                            if (!registered) {
                                caller.sendSystemMessage(Components.Heading.Login + " Você não está registrado. Use [/registrar <senha> <repetirSenha>] para registrar sua conta.")
                                return@runsAsync
                            }
                        }
                    }

                    response = SoberanaApi.Auth.auth(caller.uuid, arg())

                    when (response) {
                        is Failure -> {
                            response.Failed(Components.Heading.Login, "logar na sua conta", caller) { player, msg ->
                                player.sendSystemMessage(msg)
                            }
                            return@runsAsync
                        }
                        is Success<*> -> {
                            val valid = (response.value as Boolean)
                            if (!valid) {
                                caller.sendSystemMessage(Components.Heading.Login + " Senha incorreta, tente novamente.")
                                return@runsAsync
                            }
                        }
                    }

                    SoberanaMod.AUTHENTICATED_PLAYERS += caller.uuid
                    source.updateCommandTree()
                    caller.sendSystemMessage(Components.Heading.Login + " Logado com sucesso.")

                }
            }
        }
    }
}
