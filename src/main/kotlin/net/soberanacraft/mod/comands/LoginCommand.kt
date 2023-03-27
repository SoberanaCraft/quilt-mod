package net.soberanacraft.mod.comands

import net.silkmc.silk.commands.command
import net.soberanacraft.mod.Components
import net.soberanacraft.mod.SoberanaMod
import net.soberanacraft.mod.api.Failure
import net.soberanacraft.mod.api.SoberanaApi
import net.soberanacraft.mod.api.Success
import net.soberanacraft.mod.api.intoString
import net.soberanacraft.mod.plus

object LoginCommand {
    fun register() {
        command("login") {
            argument<String>("senha") { arg ->
                runsAsync {
                    val caller = source.player

                    var response = SoberanaApi.Auth.isRegistered(caller.uuid)

                    when (response) {
                        is Failure -> {
                            caller.sendSystemMessage(Components.Heading.Login + " Um erro ocorreu ao logar na sua conta.")
                            caller.sendSystemMessage(Components.Heading.Login + " Código de erro: ${response.message.intoString()}")
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
                            caller.sendSystemMessage(Components.Heading.Login + " Um erro ocorreu ao logar na sua conta.")
                            caller.sendSystemMessage(Components.Heading.Login + " Código de erro: ${response.message.intoString()}")
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
                    caller.sendSystemMessage(Components.Heading.Login + " Logado com sucesso.")

                }
            }
        }
    }
}
