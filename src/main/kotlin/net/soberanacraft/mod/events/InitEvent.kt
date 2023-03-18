package net.soberanacraft.mod.events

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.silkmc.silk.core.annotations.DelicateSilkApi
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Events
import net.silkmc.silk.core.event.Server
import net.silkmc.silk.core.logging.logger
import net.silkmc.silk.core.task.mcSyncLaunch
import net.silkmc.silk.core.task.silkCoroutineScope
import net.soberanacraft.mod.SoberanaMod
import net.soberanacraft.mod.api.Failure
import net.soberanacraft.mod.api.SoberanaApi
import net.soberanacraft.mod.api.Success
import net.soberanacraft.mod.api.intoString
import net.soberanacraft.mod.api.models.Server
import net.soberanacraft.mod.api.models.ServerStub

object InitEvent {
    @OptIn(ExperimentalSilkApi::class, DelicateSilkApi::class)
    fun init() {
        Events.Server.postStart.listen { event ->
            silkCoroutineScope.mcSyncLaunch {
                register()
            }
        }
    }

    suspend fun register() {
        val config = SoberanaMod.CFG.server
        val server = getExistingServerDataOrNull(config.name)
        if (server == null) {
            logger().info("Creating new server with name: ${config.name}")
            val stub = ServerStub(config.name,
                Json.encodeToString(config.versions), config.ip, null, config.modded, config.platform, config.modpackUrl, config.modloader)

            val newServer = createNewServer(stub) ?: return
            SoberanaMod.ServerUUID = newServer.id
        } else {
            logger().info("Got existing server.")
            SoberanaMod.ServerUUID = server.id
        }

        logger().info("[${config.name}] ${SoberanaMod.ServerUUID}")
    }

    suspend fun createNewServer(stub: ServerStub) : Server? {
        val response = SoberanaApi.Auth.createServer(stub)
        return when (response) {
            is Failure -> {
                logger().warn("An error occoured whilst creating the server [${stub.name}]:")
                logger().warn(response.message.intoString())
                null
            }
            is Success<*> -> {
                response.value as Server
            }

            else -> null
        }
    }
}


suspend fun getExistingServerDataOrNull(name: String): Server? {
    val apiServers = SoberanaApi.servers()
    if(apiServers.any { it.name == name }) {
        return apiServers.first { it.name == name }
    }
    return null
}
