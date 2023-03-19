package net.soberanacraft.mod

import net.soberanacraft.mod.api.SoberanaApiClient
import net.soberanacraft.mod.api.models.Connection
import net.soberanacraft.mod.api.models.Player
import net.soberanacraft.mod.comands.CommandRegistry
import net.soberanacraft.mod.events.EventRegistry
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

object SoberanaMod : ModInitializer {
    val LOGGER: Logger = LoggerFactory.getLogger("Soberana")

    val REFMAP: MutableMap<UUID, Connection> = mutableMapOf()
    val PLAYERS: MutableMap<UUID, Player> = mutableMapOf()

    lateinit var CFG : Config

    lateinit var ServerUUID: UUID

    fun resetCacheOf(uuid: UUID, player: Player) {
        PLAYERS[uuid] = player
    }

    override fun onInitialize(mod: ModContainer) {

        LOGGER.warn("Mod path: ${mod.rootPath()}")
        //TODO: Use `quilt-config`
        CFG = fromFile(File(QuiltLoader.getConfigDir().toFile(), "soberana-config.json"))
        if (CFG.api.token.isEmpty()) {
            LOGGER.error("PLEASE FIX: Token is missing on config.json. Authentication to the backend won't work and most features depend on that.")
        } else {
            LOGGER.info("Authenticating with Bearer Token.")
        }

        SoberanaApiClient.init(CFG)
        LOGGER.info("Http API: ${SoberanaApiClient.HttpAPIAddr}")
        LOGGER.info("WS API: ${SoberanaApiClient.WsAPIAddr}")
        CommandRegistry.init()
        EventRegistry.init()
        LOGGER.info("Hello Quilt world from {}!", mod.metadata()?.name())
    }
}
