package net.soberanacraft.mod.comands

import net.silkmc.silk.commands.command
import net.soberanacraft.mod.*
import net.soberanacraft.mod.api.*

object MudarSenhaCommand {
    fun register() {
        command("mudarSenha") {
            requires { it.permission("soberana.account.changepassword") }

            argument<String>("antiga") { old ->
                argument<String>("nova") { new ->
                    runsAsync {
                        val caller = source.player

                        if (!SoberanaMod.AUTHENTICATED_PLAYERS.contains(caller.uuid)) {
                            caller.sendSystemMessage(Components.Heading.MudarSenha + " Você não foi registrado.")
                            caller.sendSystemMessage(Components.Heading.MudarSenha + " Registre sua conta com [/registrar <senha> <repetirSenha>].")
                            return@runsAsync
                        }

                        val old1 = old()
                        val new1  = new()

                        var response = SoberanaApi.Auth.auth(owner = caller.uuid, password = old1)
                        when (response) {
                            is Failure -> {
                                caller.sendSystemMessage(Components.Heading.Registrar + " Um erro ocorreu ao mudar a sua senha.")
                                caller.sendSystemMessage(Components.Heading.Registrar + " Código de erro: ${response.message.intoString()}")
                                return@runsAsync
                            }
                            is Success<*> -> {
                                val result = (response.value as Boolean)
                                if (!result) {
                                    caller.sendSystemMessage(Components.Heading.MudarSenha + " Senha incorreta.")
                                    caller.sendSystemMessage(Components.Heading.MudarSenha + " Verifique se: \"$old1\" está correto e tente novamente.")
                                    return@runsAsync
                                }
                            }
                        }

                        response = SoberanaApi.Auth.updatePassword(owner = caller.uuid, old = old1, new = new1)

                        when (response) {
                            is Failure -> {
                                caller.sendSystemMessage(Components.Heading.Registrar + " Um erro ocorreu ao mudar a sua senha.")
                                caller.sendSystemMessage(Components.Heading.Registrar + " Código de erro: ${response.message.intoString()}")
                                return@runsAsync
                            }
                            is Success<*> -> {
                                val result = (response.value as Boolean)
                                if (!result) {
                                    caller.sendSystemMessage(Components.Heading.Registrar + " " + "!!! Favor reportar aos desenvolvedores. !!!".toErrComponent())
                                    caller.sendSystemMessage(Components.Heading.Registrar + " " + "Código de erro: `UNREACHABLE_PATH [M_S_C_.kt:55]`".toErrComponent())
                                    caller.sendSystemMessage(Components.Heading.Registrar + " " + "uid:${caller.uuid} | sid:${SoberanaMod.ServerUUID}".toErrComponent())
                                    return@runsAsync
                                }
                            }
                        }

                        caller.sendSystemMessage(Components.Heading.Registrar + " Senha alterada com sucesso!")
                    }
                }
            }

        }
    }
}
