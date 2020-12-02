package openmcvr.client.mixin;

import net.minecraft.client.MinecraftClient;
import openmcvr.client.OpenMCVRClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "close()V")
    public void close(CallbackInfo ci) {
        OpenMCVRClient.INSTANCE.cleanup();
    }
}
