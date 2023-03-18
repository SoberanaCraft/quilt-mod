@file:Suppress("unused")

package net.soberanacraft.mod.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.soberanacraft.mod.api.models.*
import java.util.*


open class Fallible
class Failure(val message: Message) : Fallible()
class Success<T>(val value : T) : Fallible()

suspend inline fun <reified T> fallible(request: () -> HttpResponse) : Fallible {
    val req = request()
    val code = req.status
    val body = req.bodyAsText()

    if (!code.isSuccess()) {
        return Failure(HttpError(code.value, code.description))
    }

    return serializeFallible<T>(body)
}

inline fun <reified T> serializeFallible(input: String) : Fallible {
    val json = Json.parseToJsonElement(input).jsonObject

    if (json.containsKey("error")) {
        val errorMessage = Json.decodeFromString<ErrorMessage>(input)
        return Failure(errorMessage)
    }

    if (json.containsKey("param")) {
        val invalidUUIDMessage = Json.decodeFromString<InvalidUUIDMessage>(input)
        return Failure(invalidUUIDMessage)
    }

    return Success<T>(Json.decodeFromString(input))
}

suspend inline fun _delete(path: String) : HttpResponse {
    val response = SoberanaApiClient.client.get (SoberanaApiClient.HttpAPIAddr + path) {
        this.bearerAuth(SoberanaApiClient.config.api.token)
        this.userAgent("SoberanaCraft-API Authorized Delete")
    }
    return response
}


suspend inline fun _get(path: String) : HttpResponse {
    val response = SoberanaApiClient.client.get (SoberanaApiClient.HttpAPIAddr + path) {
        this.userAgent("SoberanaCraft-API Anonymous Get")
    }
    return response
}

suspend inline fun _authGet(path: String) : HttpResponse {
    val response = SoberanaApiClient.client.get (SoberanaApiClient.HttpAPIAddr + path) {
        this.bearerAuth(SoberanaApiClient.config.api.token)
        this.userAgent("SoberanaCraft-API Authorized Get")
    }

    return response
}

suspend inline fun <reified A> _post(path: String, value : A? = null) : HttpResponse {
    val response = SoberanaApiClient.client.post (SoberanaApiClient.HttpAPIAddr + path) {
        if(value != null) {
            this.contentType(ContentType.Application.Json)
            this.setBody(value)
        }
        this.bearerAuth(SoberanaApiClient.config.api.token)
        this.userAgent("SoberanaCraft-API Authorized POST")
    }
    return response
}

suspend inline fun <reified T> get(path: String): T {
    return _get(path).body()
}

suspend inline fun <reified T> delete(path: String) : T {
    return _delete(path).body()
}

suspend inline fun <reified T> authGet(path: String): T {
    return _authGet(path).body()
}

suspend inline fun <reified T, reified A> post(path: String, value: A?) : T {
    return _post(path, value).body()
}

fun String.intoUUID() : UUID = UUID.fromString(this)

fun Message.intoString() : String {
    return when (this) {
        is HttpError -> "${this.error}: ${this.code}  ${this.description}"
        is InvalidUUIDMessage -> "${this.error}: Parameter ${this.param} is invalid."
        is ErrorMessage -> this.error
        is SucessMessage -> this.message
        is LinkMessage -> handleLinkMessage(this)
        else -> "Unreachable."
    }
}

fun handleLinkMessage(message: LinkMessage): String {
    return when (message.linkStatus) {
        LinkStatus.InvalidDiscord -> "INVALID_DISCORD"
        LinkStatus.NotJoinedToGuild -> "NOT_JOINED_TO_GUILD"
        LinkStatus.JoinedGuild -> "SUCCESS"
        LinkStatus.AlreadyLinked -> "ALREADY_LINKED"
    }
}
