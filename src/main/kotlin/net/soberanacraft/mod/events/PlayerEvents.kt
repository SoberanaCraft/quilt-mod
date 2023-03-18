package net.soberanacraft.mod.events

import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import net.silkmc.silk.core.annotations.DelicateSilkApi
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Player
import net.silkmc.silk.core.logging.logger
import net.silkmc.silk.core.task.mcSyncLaunch
import net.silkmc.silk.core.task.silkCoroutineScope
import net.soberanacraft.mod.Components
import net.soberanacraft.mod.SoberanaMod
import net.soberanacraft.mod.api.Failure
import net.soberanacraft.mod.api.SoberanaApi
import net.soberanacraft.mod.api.Success
import net.soberanacraft.mod.api.models.*
import net.soberanacraft.mod.plus
import net.soberanacraft.mod.toComponent

object PlayerEvents {
    @OptIn(ExperimentalSilkApi::class, DelicateSilkApi::class)
    fun init() {
        Events.Player.postLogin.listen { event ->
            silkCoroutineScope.mcSyncLaunch {
                if(SoberanaMod.PLAYERS.containsKey(event.player.uuid)) return@mcSyncLaunch
                event.player.login()
            }
        }

        Events.Player.preQuit.listen { event ->
            silkCoroutineScope.mcSyncLaunch {
                event.player.disconnect()
            }
        }
    }


}

suspend fun ServerPlayerEntity.login() {
    if(SoberanaApi.isAvailable(gameProfile.name)) {
        createNew()
    }
    val player = get()
    player.connect()
    player.applyEffects(this)
}

fun Player.applyEffects(entity: ServerPlayerEntity) {
    when (trustFactor) {
        Trust.Unlinked ->  {
            entity.changeGameMode(GameMode.ADVENTURE)
            entity.addStatusEffect(StatusEffectInstance(StatusEffects.SATURATION, Int.MAX_VALUE))
            entity.sendSystemMessage(Components.Heading.ReadOnly + " Você está no modo somente leitura.")
            entity.sendSystemMessage(Components.Heading.ReadOnly + " O seu nick: ${this.nickname} poderá ser registrado por outros jogadores.")
            entity.sendSystemMessage(Components.Heading.ReadOnly + " Para sair desse modo:")
            entity.sendSystemMessage(Components.Heading.ReadOnly + " A) Seja convidado por um amigo")
            entity.sendSystemMessage(Components.Heading.ReadOnly + " B) Linque sua conta do discord com " + "/link".toComponent().setStyle(
                Components.Styles.fromRgb(Components.Colors.COMMAND_GREEN)))
        }
        Trust.Linked, Trust.Reffered, Trust.Trusted   -> {
            if (entity.interactionManager.gameMode == GameMode.ADVENTURE) {
                entity.changeGameMode(GameMode.SURVIVAL)
                if (entity.statusEffects.any { it -> it.effectType == StatusEffects.SATURATION }) {
                    entity.removeStatusEffect(StatusEffects.SATURATION)
                }
            }
        }
    }
}

suspend fun ServerPlayerEntity.createNew() {
    logger().info("Creating new player profile for [${this.uuid}]")
    val response = SoberanaApi.Auth.createPlayer(PlayerStub(this.uuid, this.gameProfile.name, Platform.Java))
    when (response) {
        is Failure -> {
            this.sendSystemMessage("Um erro aconteceu ao registrar sua conta: ${response.message}".toComponent())
            return
        }
        is Success<*> -> {
            (response.value as Player).connect()
        }
    }
}

suspend fun ServerPlayerEntity.get() = SoberanaApi._player(uuid)

suspend fun Player.connect() {
    val connection = Connection(this.uuid, "1.19.3", SoberanaMod.ServerUUID)
    SoberanaApi.Auth.joinServer(connection)
    SoberanaMod.REFMAP[uuid] = connection
    SoberanaMod.PLAYERS[uuid] = this
}

suspend fun ServerPlayerEntity.disconnect() {
    if(SoberanaMod.REFMAP.containsKey(uuid)) {
        SoberanaApi.Auth.disconnectServer(SoberanaMod.REFMAP[uuid]!!)
        SoberanaMod.REFMAP.remove(uuid)
        SoberanaMod.PLAYERS.remove(uuid)
    }
}
