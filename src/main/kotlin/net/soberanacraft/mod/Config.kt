package net.soberanacraft.mod

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.soberanacraft.mod.api.models.Platform
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
data class ApiConfig(val endpoint: String, val token: String, val tls: Boolean) {
    companion object {
        val Default = ApiConfig("localhost:8080", "", false)
    }
}

@Serializable
data class ServerConfig(val name : String,
                        val versions: List<String>,
                        val ip: String,
                        val modded: Boolean,
                        val modpackUrl: String,
                        val platform: Platform,
                        val modloader: String) {
    companion object {
        val Default = ServerConfig("dev", listOf(), "localhost:25565", false, "", Platform.Java, "quilt")
    }
}

@Serializable
data class Config(val api: ApiConfig, val server: ServerConfig) {
    companion object {
        val Default = Config(ApiConfig.Default, ServerConfig.Default)
    }
}


fun fromFile(path: File): Config {
    if (!path.exists()) {
        val s = Jsoberana.encodeToString(Config.Default)
        path.writeText(s)
    }
    return Json.decodeFromString(path.readText())
}
