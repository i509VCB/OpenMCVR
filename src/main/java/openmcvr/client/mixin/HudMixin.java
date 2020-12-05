package openmcvr.client.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import openmcvr.client.OpenMCVRClient;
import openmcvr.client.RenderLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudMixin {
    @Shadow private int scaledHeight;

    @Shadow private int scaledWidth;

    @Inject(at = @At(value = "INVOKE_ASSIGN", ordinal = 4), method = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/util/math/MatrixStack;F)V", cancellable = true)
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        this.scaledWidth = OpenMCVRClient.INSTANCE.getFramebuffer().viewportWidth;
        this.scaledHeight = OpenMCVRClient.INSTANCE.getFramebuffer().viewportHeight;

        if(OpenMCVRClient.INSTANCE.getEye() != RenderLocation.CENTER)
            ci.cancel();
    }
}
