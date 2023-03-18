@file:Suppress("unused")

package net.soberanacraft.mod.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import net.soberanacraft.mod.Config
import net.soberanacraft.mod.api.models.*
import java.util.UUID

object SoberanaApiClient {
    lateinit var client: HttpClient
    lateinit var config: Config

    lateinit var HttpAPIAddr: String
    lateinit var WsAPIAddr: String

    fun init(config: Config) {
        this.config = config
        this.HttpAPIAddr = if (config.api.tls) "https://${config.api.endpoint}" else "http://${config.api.endpoint}"
        this.WsAPIAddr = "ws://${config.api.endpoint}"
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }

            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
    }
}

object SoberanaApi {
    suspend fun players () = get<List<Player>>("/players")
    suspend fun _player (uuid: UUID) = get<Player>("/player/$uuid")
    suspend fun player(uuid: UUID) = fallible<Player> { _get("/player/$uuid") }
    suspend fun nonce () = get<String>("/nonce")
    suspend fun auth (state: String) = get<String>("/auth?state=$state")
    suspend fun isAvailable(nick: String) =  get<Boolean>("/player/isAvailable/$nick")
    suspend fun servers() = get<List<Server>>("/servers")

    // Authenticated Routes
    object Auth {
        suspend fun me() = authGet<String>("/@me")
        suspend fun createPlayer(stub: PlayerStub) = fallible<Player> { _post("/player/create", stub) }
        suspend fun addReferee(player: UUID, referee: UUID) = fallible<Boolean> { _post<Any>("/player/refer/$player/$referee") }
        suspend fun revokePlayer(uuid: UUID) = fallible<Boolean> { _post<Any>("/player/revoke?uuid=$uuid") }
        suspend fun createNonce(owner: UUID, nonce: String) = fallible<Nonce> { _post<Any>("/nonce/create?uuid=$owner&nonce=$nonce") }
        suspend fun revokeNonce(owner: UUID) = fallible<Boolean> { _delete("/nonce/revoke?uuid=$owner") }
        suspend fun joinServer(connection: Connection) = fallible<SucessMessage> { _post("/server/join", connection) }
        suspend fun disconnectServer(connection: Connection) = fallible<SucessMessage> { _post("/server/disconnect", connection) }
        suspend fun createServer(server: ServerStub) = fallible<Server> { _post("/server/create", server) }
        suspend fun revokeServer(uuid: UUID) = fallible<Boolean> {  _delete("/server/revoke?id=$uuid") }
    }
}
