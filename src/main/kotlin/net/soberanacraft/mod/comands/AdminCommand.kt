package net.soberanacraft.mod.comands

import net.minecraft.command.argument.GameProfileArgumentType.GameProfileArgument
import net.minecraft.server.command.ServerCommandSource
import net.silkmc.silk.commands.LiteralCommandBuilder
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.soberanacraft.mod.*
import net.soberanacraft.mod.api.Failure
import net.soberanacraft.mod.api.SoberanaApi
import net.soberanacraft.mod.api.Success
import net.soberanacraft.mod.api.permission

object AdminCommand {
    fun register() {
        command("adm") {
            requires { it.player.isAuthenticated() }
            requires { it.permission("soberana.admin") }
            requiresPermissionLevel(PermissionLevel.COMMAND_RIGHTS)
            conta()
        }
    }
}


fun LiteralCommandBuilder<ServerCommandSource>.conta() {
    literal("conta") {
        requires { it.permission("soberana.admin.conta") }
        reset()
    }
}

fun LiteralCommandBuilder<ServerCommandSource>.reset() {
    literal("reset") {
        requires { it.permission("soberana.admin.conta.reset") }

        argument<GameProfileArgument>("player") {player ->
            runsAsync {
                val caller = source.player
                val target = player().getNames(source).first()

                if (target.id == caller.uuid) {
                    caller.sendSystemMessage(Components.Heading.Admin + " Você não pode resetar sua própria conta.")
                    return@runsAsync
                }

                var result = SoberanaApi.Auth.isRegistered(caller.uuid)

                when (result) {
                    is Failure -> {
                        result.Failed(Components.Heading.Admin, "obtenção de dados do jogador", caller) { player, message ->
                            player.sendSystemMessage(message)
                        }
                        return@runsAsync
                    }
                    is Success<*> -> {
                        val response = result.value as Boolean
                        if(!response) {
                            caller.sendSystemMessage(Components.Heading.Admin + " O Jogador "
                                + (target.name.rgb(Components.Colors.COMMAND_GREEN)
                                + " (${target.id})".rgb(Components.Colors.INFO))
                                + " não está registrado."
                            )
                            return@runsAsync
                        }
                    }
                }

                result = SoberanaApi.Auth.unregister(caller.uuid)

                when (result) {
                    is Failure -> {
                        result.Failed(Components.Heading.Admin, "remover o registro do jogador", caller) { player, message ->
                            player.sendSystemMessage(message)
                        }
                        return@runsAsync
                    }
                    is Success<*> -> {
                        val response = result.value as Boolean
                        if (!response) {
                            caller.sendSystemMessage(Components.Heading.Admin + " Não foi possivel remover o registro do jogador "
                                + (target.name.rgb(Components.Colors.COMMAND_GREEN)
                                + " (${target.id})".rgb(Components.Colors.INFO))
                                + "."
                            )
                        }
                    }
                }

                caller.sendSystemMessage(Components.Heading.Admin + " O Jogador "
                    + (target.name.rgb(Components.Colors.COMMAND_GREEN)
                    + " (${target.id})".rgb(Components.Colors.INFO))
                    + " teve o seu registro removido com sucesso."
                )
            }
        }
    }
}
