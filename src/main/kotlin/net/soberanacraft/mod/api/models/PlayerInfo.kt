package net.soberanacraft.mod.api.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.soberanacraft.mod.api.UUIDSerializer
import java.util.UUID

@Serializable
data class PlayerStub(@Serializable(with = UUIDSerializer::class) val uuid: UUID,
                      val nickname: String,
                      val platform: Platform
)

@Serializable
data class Player(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val nickname: String,
    val platform: Platform,
    val discordId: ULong?,
    val joinedAt: Instant,
    val linkedAt: Instant?,
    val trustFactor: Trust,
    @Serializable(with = UUIDSerializer::class) val referer: UUID?
)

@Suppress("unused")
@Serializable
enum class Platform {
    Bedrock, Java, Both
}

@Serializable
enum class Trust {
    Unlinked, Linked, Reffered, Trusted
}
