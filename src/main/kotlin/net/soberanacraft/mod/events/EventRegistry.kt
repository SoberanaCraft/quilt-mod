package net.soberanacraft.mod.events

object EventRegistry {
    fun init() {
        InitEvent.init()
        PlayerEvents.init()
    }
}
