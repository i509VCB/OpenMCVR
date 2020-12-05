package openmcvr.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Matrix4f;
import openmcvr.client.OpenMCVRClient;
import openmcvr.client.RenderLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow protected abstract void renderHand(MatrixStack matrices, Camera camera, float tickDelta);

    @Shadow @Final private Camera camera;

    @Shadow @Final private LightmapTextureManager lightmapTextureManager;

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract void updateTargetedEntity(float tickDelta);

    @Shadow protected abstract boolean shouldRenderBlockOutline();

    @Shadow private float viewDistance;

    @Shadow protected abstract void bobViewWhenHurt(MatrixStack matrixStack, float f);

    @Shadow protected abstract void bobView(MatrixStack matrixStack, float f);

    @Shadow private int ticks;

    @Shadow public abstract void loadProjectionMatrix(Matrix4f matrix4f);

    @Shadow private boolean renderHand;

    @Shadow public abstract void onResized(int i, int j);

    @Shadow protected abstract void renderFloatingItem(int scaledWidth, int scaledHeight, float tickDelta);

    @Shadow protected abstract void method_31136(float f);

    @Shadow private @Nullable ShaderEffect shader;

    @Shadow protected abstract void updateWorldIcon();

    @Shadow private long lastWorldIconUpdate;

    @Shadow private long lastWindowFocusedTime;

    @Shadow private boolean shadersEnabled;

    @Shadow public abstract void renderWorld(float tickDelta, long limitTime, MatrixStack matrix);

    @Shadow private float zoom;

    @Shadow protected abstract double getFov(Camera camera, float tickDelta, boolean changingFov);

    @Shadow private float zoomX;

    @Shadow private float zoomY;

    /**
     * @author SoapyXM
     * @reason To provide a proper projection matrix to the game.
     */
    @Overwrite
    public Matrix4f getBasicProjectionMatrix(Camera camera, float f, boolean bl) {
        MatrixStack matrixStack = new MatrixStack();

        matrixStack.peek().getModel().loadIdentity();
        if (this.zoom != 1.0F) {
            matrixStack.translate(this.zoomX, -this.zoomY, 0.0D);
            matrixStack.scale(this.zoom, this.zoom, 1.0F);
        }

        if(OpenMCVRClient.INSTANCE.getEye() == RenderLocation.CENTER) {
            matrixStack.peek().getModel().multiply(
                    Matrix4f.viewboxMatrix(
                            this.getFov(camera, f, bl),
                            (float) this.client.getWindow().getFramebufferWidth() / (float) this.client.getWindow().getFramebufferHeight(),
                            0.05F,
                            this.viewDistance * 4.0F)
            );
        } else {
            matrixStack.peek().getModel().multiply(OpenMCVRClient.INSTANCE.getEyeProjection());
        }
        return matrixStack.peek().getModel();
    }

    /**
     * @author SoapyXM
     * @reason this method needs heavy modification
     */
    @Overwrite
    public void render(float tickDelta, long startTime, boolean tick) {
        if (this.client.isWindowFocused() || !this.client.options.pauseOnLostFocus || this.client.options.touchscreen && this.client.mouse.wasRightButtonClicked()) {
            this.lastWindowFocusedTime = Util.getMeasuringTimeMs();
        } else if (Util.getMeasuringTimeMs() - this.lastWindowFocusedTime > 500L) {
            this.client.openPauseMenu(false);
        }

        if (!this.client.skipGameRender) {
            int i = (int) (this.client.mouse.getX() * (double) this.client.getWindow().getScaledWidth() / (double) this.client.getWindow().getWidth());
            int j = (int) (this.client.mouse.getY() * (double) this.client.getWindow().getScaledHeight() / (double) this.client.getWindow().getHeight());

            Framebuffer framebuffer = OpenMCVRClient.INSTANCE.getFramebuffer();
            RenderSystem.viewport(0, 0, framebuffer.viewportWidth, framebuffer.viewportHeight);

            if (tick && this.client.world != null) {
                this.client.getProfiler().push("level");
                // eye transforms
                MatrixStack matrix = new MatrixStack();
                if(OpenMCVRClient.INSTANCE.getEye() != RenderLocation.CENTER) {
                    Matrix4f transform = (OpenMCVRClient.INSTANCE.getHeadTransform());
                    matrix.peek().getModel().multiply(transform);
                    Matrix4f eyeTransform = (OpenMCVRClient.INSTANCE.getEyeTransform());
                    matrix.peek().getModel().multiply(eyeTransform);
                }

                this.renderWorld(tickDelta, startTime, matrix);
                if (this.client.isIntegratedServerRunning() && this.lastWorldIconUpdate < Util.getMeasuringTimeMs() - 1000L) {
                    this.lastWorldIconUpdate = Util.getMeasuringTimeMs();
                    if (!this.client.getServer().hasIconFile()) {
                        this.updateWorldIcon();
                    }
                }

                this.client.worldRenderer.drawEntityOutlinesFramebuffer();

                framebuffer.beginWrite(true);
            }

            Window window = this.client.getWindow();
            RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
            RenderSystem.matrixMode(5889);
            RenderSystem.loadIdentity();
            if(OpenMCVRClient.INSTANCE.getEye() == RenderLocation.CENTER) {
                RenderSystem.ortho(0.0D, (double)window.getFramebufferWidth() / window.getScaleFactor(), (double)window.getFramebufferHeight() / window.getScaleFactor(), 0.0D, 1000.0D, 3000.0D);
            } else {
                RenderSystem.ortho(0.0D, framebuffer.textureWidth, framebuffer.textureHeight, 0.0D, 1000.0D, 3000.0D);
            }

            RenderSystem.matrixMode(5888);
            RenderSystem.loadIdentity();
            RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
            DiffuseLighting.enableGuiDepthLighting();
            MatrixStack matrixStack = new MatrixStack();
            if (tick && this.client.world != null) {
                this.client.getProfiler().swap("gui");

                // nausea has been removed because this would probably physically harm people in VR

                if (!this.client.options.hudHidden || this.client.currentScreen != null) {
                    RenderSystem.defaultAlphaFunc();
                    this.renderFloatingItem(this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), tickDelta);
                    this.client.inGameHud.render(matrixStack, tickDelta);
                    RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
                }

                this.client.getProfiler().pop();
            }

            CrashReport crashReport2;
            CrashReportSection crashReportSection2;
            if (this.client.overlay != null) {
                try {
                    this.client.overlay.render(matrixStack, i, j, this.client.getLastFrameDuration());
                } catch (Throwable var13) {
                    crashReport2 = CrashReport.create(var13, "Rendering overlay");
                    crashReportSection2 = crashReport2.addElement("Overlay render details");
                    crashReportSection2.add("Overlay name", () -> {
                        return this.client.overlay.getClass().getCanonicalName();
                    });
                    throw new CrashException(crashReport2);
                }
            } else if (this.client.currentScreen != null) {
                try {
                    this.client.currentScreen.render(matrixStack, i, j, this.client.getLastFrameDuration());
                } catch (Throwable var12) {
                    crashReport2 = CrashReport.create(var12, "Rendering screen");
                    //TODO: re-add detailed crash report
                    throw new CrashException(crashReport2);
                }
            }
        }
    }
}
