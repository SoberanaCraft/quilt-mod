package net.soberanacraft.mod.events

import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.TypedActionResult
import net.minecraft.world.GameMode
import net.silkmc.silk.core.annotations.DelicateSilkApi
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Player
import net.silkmc.silk.core.logging.logger
import net.silkmc.silk.core.task.mcSyncLaunch
import net.silkmc.silk.core.task.silkCoroutineScope
import net.soberanacraft.mod.*
import net.soberanacraft.mod.Role.Async.addRole
import net.soberanacraft.mod.Role.Async.hasRole
import net.soberanacraft.mod.Role.Async.removeRole
import net.soberanacraft.mod.api.*
import net.soberanacraft.mod.api.models.*
import java.util.*

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
                if (SoberanaMod.AUTHENTICATED_PLAYERS.contains(event.player.uuid)){
                    SoberanaMod.AUTHENTICATED_PLAYERS -= event.player.uuid
                }

                event.player.disconnect()
            }
        }

        PlayerBlockBreakEvents.BEFORE.register(PlayerBlockBreakEvents.Before { _, player, _, _, _ ->
            isAuthenticated(player!!.uuid) == ActionResult.PASS
        })

        UseBlockCallback.EVENT.register(UseBlockCallback { player, _,_,_ ->
            isAuthenticated(player!!.uuid)
        })

        UseItemCallback.EVENT.register(UseItemCallback { player,_, _ ->
            isAuthenticatedItemStack(player!!.uuid)
        })

        AttackEntityCallback.EVENT.register(AttackEntityCallback { player, _,_, _, _ ->
            isAuthenticated(player!!.uuid)
        })

    }

    private fun isAuthenticated(uuid: UUID): ActionResult {
        if (SoberanaMod.AUTHENTICATED_PLAYERS.contains(uuid)) {
            return ActionResult.PASS
        }

        return ActionResult.FAIL
    }

    private fun isAuthenticatedItemStack(uuid: UUID) : TypedActionResult<ItemStack> {
        if (SoberanaMod.AUTHENTICATED_PLAYERS.contains(uuid)) {
            return TypedActionResult.pass(ItemStack.EMPTY)
        }

        return TypedActionResult.fail(ItemStack.EMPTY)
    }


}

suspend fun ServerPlayerEntity.login() {
    if(SoberanaApi.isAvailable(gameProfile.name)) {
        createNew()
    }
    val player = get()
    player.connect()
    player.applyEffects(this)
    player.applyRoles()
    player.sendJoinMessage(this)
}

suspend fun Player.applyRoles() {
    when (trustFactor) {
        Trust.Unlinked ->  {
            if (!uuid.hasRole(Role.Name.READ_ONLY))
                uuid.addRole(Role.READ_ONLY)
        }
        Trust.Linked ->  {
            if (uuid.hasRole(Role.Name.READ_ONLY))
                uuid.removeRole(Role.READ_ONLY)

            if (!uuid.hasRole(Role.Name.MEMBER))
                uuid.addRole(Role.MEMBER)
        }
        Trust.Reffered -> {
            if (uuid.hasRole(Role.Name.READ_ONLY))
                uuid.removeRole(Role.READ_ONLY)

            if (!uuid.hasRole(Role.Name.REFERRED))
                uuid.addRole(Role.REFERRED)
        }
        Trust.Trusted -> {
            if (uuid.hasRole(Role.Name.READ_ONLY))
                uuid.removeRole(Role.READ_ONLY)

            if (!uuid.hasRole(Role.Name.TRUSTED))
                uuid.addRole(Role.TRUSTED)
        }
    }

}

fun Player.applyEffects(entity: ServerPlayerEntity) {
    when (trustFactor) {
        Trust.Unlinked ->  {
            entity.changeGameMode(GameMode.ADVENTURE)
            entity.addStatusEffect(StatusEffectInstance(StatusEffects.SATURATION, Int.MAX_VALUE))
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

suspend fun Player.sendJoinMessage(entity: ServerPlayerEntity) {
    when(trustFactor) {
        Trust.Unlinked -> {
            entity.sendSystemMessage(Components.Heading.ReadOnly + " Você está no modo somente leitura.")
            entity.sendSystemMessage(Components.Heading.ReadOnly + " O seu nick: ${this.nickname} poderá ser registrado por outros jogadores.")
            entity.sendSystemMessage(Components.Heading.ReadOnly + " Para sair desse modo:")
            entity.sendSystemMessage(Components.Heading.ReadOnly + " A) Seja convidado por um amigo")
            entity.sendSystemMessage(Components.Heading.ReadOnly + " B) Linque sua conta do discord com " + "/link".rgb(Components.Colors.COMMAND_GREEN))
        }
        Trust.Linked, Trust.Reffered, Trust.Trusted -> {
            TODO()
        }
    }
}

suspend fun ServerPlayerEntity.createNew() {
    logger().info("Creating new player profile for [${this.uuid}]")
    val response = SoberanaApi.Auth.createPlayer(PlayerStub(this.uuid, this.gameProfile.name, Platform.Java))
    either<Player>({ it.fail(Components.Heading.Login, "criar a sua conta", this) },
    { player ->
        player.connect()
    }, response)
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
