package openmcvr.client.framebuffer

import net.minecraft.client.gl.Framebuffer
import java.util.*

class FramebufferPile(width: Int, height: Int, useDepth: Boolean, getError: Boolean) {
    val framebuffersByType: MutableMap<FramebufferType, Framebuffer> = EnumMap(FramebufferType::class.java)

    init {
        framebuffersByType[FramebufferType.Solid] = Framebuffer(width, height, useDepth, getError)
        framebuffersByType[FramebufferType.Translucent] = Framebuffer(width, height, useDepth, getError)
        framebuffersByType[FramebufferType.Entity] = Framebuffer(width, height, useDepth, getError)
        framebuffersByType[FramebufferType.Particles] = Framebuffer(width, height, useDepth, getError)
        framebuffersByType[FramebufferType.Weather] = Framebuffer(width, height, useDepth, getError)
        framebuffersByType[FramebufferType.Clouds] = Framebuffer(width, height, useDepth, getError)
        framebuffersByType[FramebufferType.EntityOutlines] = Framebuffer(width, height, useDepth, getError)
    }
}