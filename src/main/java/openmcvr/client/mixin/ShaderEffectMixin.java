package openmcvr.client.mixin;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import openmcvr.ForwardingFramebuffer;
import openmcvr.client.framebuffer.FramebufferType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ShaderEffect.class)
public class ShaderEffectMixin {

    /**
     * @author SoapyXM
     * @reason To redirect these to forwarding framebuffers.
     */
    @Overwrite
    public Framebuffer getSecondaryTarget(String name) {
        return new ForwardingFramebuffer(FramebufferType.Companion.fromString(name), 2048, 2048, true, true);
    }
}
