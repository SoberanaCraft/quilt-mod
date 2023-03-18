package net.soberanacraft.mod.api.models

import kotlinx.serialization.Serializable
import net.soberanacraft.mod.api.UUIDSerializer
import java.util.*

@Serializable
data class Nonce(@Serializable(with = UUIDSerializer::class) val playerId: UUID, val nonce: String)
