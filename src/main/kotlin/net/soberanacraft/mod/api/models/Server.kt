package net.soberanacraft.mod.api.models

import kotlinx.serialization.Serializable
import net.soberanacraft.mod.api.UUIDSerializer
import java.util.*

@Serializable
data class ServerStub(
    val name: String,
    val supportedVersions: String,
    val externalIp: String,
    val internalIp: String?,
    val modded: Boolean,
    val platform: Platform,
    val modpackUrl: String?,
    val modloader: String?
)

@Serializable
data class Server(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val playerCount: Int,
    val supportedVersions: String,
    val externalIp: String,
    val internalIp: String?,
    val modded: Boolean,
    val platform: Platform,
    val modpackUrl: String?,
    val modloader: String?
)
