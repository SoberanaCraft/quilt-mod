package net.soberanacraft.mod.api.models

import kotlinx.serialization.Serializable
import net.soberanacraft.mod.api.UUIDSerializer
import java.util.*

@Serializable
data class AuthenticatedUser(@Serializable(with= UUIDSerializer::class) val owner: UUID, val discordId: ULong?, val password: String)

