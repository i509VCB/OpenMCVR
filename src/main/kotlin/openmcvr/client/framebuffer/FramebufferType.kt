package openmcvr.client.framebuffer

enum class FramebufferType {
    Solid,
    Translucent,
    Entity,
    Particles,
    Weather,
    Clouds,
    EntityOutlines;

    companion object {
        fun fromString(str: String): FramebufferType? {
            return when(str) {
                "solid" -> Solid
                "final" -> EntityOutlines
                "translucent" -> Translucent
                "itemEntity" -> Entity
                "particles" -> Particles
                "clouds" -> Clouds
                else -> {
                    println("Problem: bad framebuffer name: " + str)
                    null
                }
            }
        }
    }
}