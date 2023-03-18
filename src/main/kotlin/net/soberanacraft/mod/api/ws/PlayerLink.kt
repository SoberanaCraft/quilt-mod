package net.soberanacraft.mod.api.ws

import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import net.soberanacraft.mod.api.Fallible
import net.soberanacraft.mod.api.SoberanaApiClient
import net.soberanacraft.mod.api.models.LinkMessage
import net.soberanacraft.mod.api.serializeFallible
import java.util.UUID

suspend fun waitUntil(uuid: UUID) : Fallible {
    lateinit var serialized : Fallible

    SoberanaApiClient.client.webSocket("${SoberanaApiClient.WsAPIAddr}/ws/link/${uuid}") {
        var received = false
        while(!received) {
            val othersMessage = incoming.receive() as? Frame.Text
            val data = othersMessage?.readText()
            if(data != null) {
                received = true
                serialized = serializeFallible<LinkMessage>(data)
            }
        }
    }

    return serialized
}
