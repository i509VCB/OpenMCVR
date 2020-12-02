package openmcvr.client.mixin;

import net.minecraft.client.main.Main;
import openmcvr.client.OpenMCVRClientKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {
    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/main/Main;main([Ljava/lang/String;)V")
    private static void main(String[] args, CallbackInfo ci) {
        OpenMCVRClientKt.init();
    }
}
