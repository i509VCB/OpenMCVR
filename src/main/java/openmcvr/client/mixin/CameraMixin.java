package openmcvr.client.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    /**
     * @reason This method is called with Euler angles, which we can't use anymore.
     */
    @Inject(at = @At("HEAD"), method = "setRotation(FF)V", cancellable = true)
    void setRotation(float yaw, float pitch, CallbackInfo ci) {
        ci.cancel();
    }
}
