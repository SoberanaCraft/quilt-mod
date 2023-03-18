package net.soberanacraft.mod.api.models

import kotlinx.datetime.Instant

@Suppress("unused")
data class DiscordUser(
    val id: ULong,
    val username: String,
    val discriminator: Int,
    val nickname: String?,
    val email: String,
    val joinedAt: Instant,
    var accessToken: String,
    var refreshToken: String,
    var expiresAt: Instant
) {
    override fun toString(): String = "$username#$discriminator ($id)"
}
