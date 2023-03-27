package net.soberanacraft.mod.comands

import net.silkmc.silk.commands.command
import net.soberanacraft.mod.Components
import net.soberanacraft.mod.SoberanaMod
import net.soberanacraft.mod.api.Failure
import net.soberanacraft.mod.api.SoberanaApi
import net.soberanacraft.mod.api.Success
import net.soberanacraft.mod.api.intoString
import net.soberanacraft.mod.plus

object RegistrarCommand {
    fun register() {
        command("registrar") {
            argument<String>("senha") { res ->
                argument<String>("repetirSenha") {res2 ->
                    runsAsync {
                        val caller = source.player
                        val pwd = res()
                        val pwd2 = res2()

                        val user = SoberanaMod.PLAYERS[caller.uuid] ?: return@runsAsync


                        if (pwd != pwd2) {
                            caller.sendSystemMessage(Components.Heading.Registrar + " As senhas digitadas não são idênticas.")
                            caller.sendSystemMessage(Components.Heading.Registrar + " Verifique se \"$pwd2\" é igual à sua senha e tente novamente.")
                            return@runsAsync
                        }

                        var response = SoberanaApi.Auth.isRegistered(caller.uuid)

                        when (response) {
                            is Failure -> {
                                caller.sendSystemMessage(Components.Heading.Registrar + " Um erro ocorreu ao registrar a sua conta.")
                                caller.sendSystemMessage(Components.Heading.Registrar + " Código de erro: ${response.message.intoString()}")
                                return@runsAsync
                            }
                            is Success<*> -> {
                                val it = (response.value as Boolean)
                                if (it) {
                                    caller.sendSystemMessage(Components.Heading.Registrar + " Você já foi registrado.")
                                    caller.sendSystemMessage(Components.Heading.Registrar + " Use [/login <senha>] para entrar no servidor.")
                                    return@runsAsync
                                }
                            }
                        }

                        response = SoberanaApi.Auth.register(user.uuid, user.discordId, pwd)

                        when (response) {
                            is Failure -> {
                                caller.sendSystemMessage(Components.Heading.Registrar + " Um erro ocorreu ao registrar a sua conta.")
                                caller.sendSystemMessage(Components.Heading.Registrar + " Código de erro: ${response.message.intoString()}")
                                return@runsAsync
                            }
                            is Success<*> -> {
                                val it = (response.value as Boolean)
                                if (it) {
                                    caller.sendSystemMessage(Components.Heading.Registrar + " Conta registrada com sucesso!")
                                    caller.sendSystemMessage(Components.Heading.Registrar + " Use [/login $pwd] para entrar no servidor.")
                                    caller.sendSystemMessage(Components.Heading.Registrar + " Use [/mudarSenha $pwd <nova senha>] para mudar a senha.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
