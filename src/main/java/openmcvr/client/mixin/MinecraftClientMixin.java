package openmcvr.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.client.options.CloudRenderMode;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.MetricsData;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.snooper.Snooper;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import openmcvr.client.OpenMCVRClient;
import openmcvr.client.RenderLocation;
import openmcvr.mixinterface.EyeAlternator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin extends ReentrantThreadExecutor<Runnable> implements EyeAlternator {
    @Shadow @Final private Framebuffer framebuffer;

    @Shadow @Final private Window window;

    public MinecraftClientMixin(String string) {
        super(string);
    }

    @Shadow public abstract void scheduleStop();

    @Shadow private @Nullable CompletableFuture<Void> resourceReloadFuture;

    @Shadow @Nullable public Overlay overlay;

    @Shadow public abstract CompletableFuture<Void> reloadResources();

    @Shadow @Final private Queue<Runnable> renderTaskQueue;

    @Shadow @Final private RenderTickCounter renderTickCounter;

    @Shadow private Profiler profiler;

    @Shadow public abstract void tick();

    @Shadow @Final private SoundManager soundManager;

    @Shadow @Final public Mouse mouse;

    @Shadow @Final public GameRenderer gameRenderer;

    @Shadow @Final public static boolean IS_SYSTEM_MAC;

    @Shadow public boolean skipGameRender;

    @Shadow private boolean paused;

    @Shadow private float pausedTickDelta;

    @Shadow @Final private ToastManager toastManager;

    @Shadow private @Nullable ProfileResult tickProfilerResult;

    @Shadow protected abstract void drawProfilerResults(MatrixStack matrices, ProfileResult profileResult);

    @Shadow protected abstract int getFramerateLimit();

    @Shadow private int fpsCounter;

    @Shadow public abstract boolean isIntegratedServerRunning();

    @Shadow @Nullable public Screen currentScreen;

    @Shadow private long lastMetricsSampleTime;

    @Shadow @Final public MetricsData metricsData;

    @Shadow private long nextDebugInfoUpdateTime;

    @Shadow private static int currentFps;

    @Shadow public String fpsDebugString;

    @Shadow @Final public GameOptions options;

    @Shadow @Final private Snooper snooper;

    @Shadow private @Nullable IntegratedServer server;

    @Inject(at = @At("HEAD"), method = "close()V")
    public void close(CallbackInfo ci) {
        OpenMCVRClient.INSTANCE.cleanup();
    }

    /**
     * @author SoapyXM
     * @reason To alternate framebuffers.
     */
    @Overwrite
    public Framebuffer getFramebuffer() {
        return OpenMCVRClient.INSTANCE.getFramebuffer();
    }

    /**
     * @author SoapyXM
     * @reason Method needs heavy modification.
     */
    @Overwrite
    private void render(boolean tick) {
        OpenMCVRClient.INSTANCE.onFrame();
        this.window.setPhase("Pre render");
        long l = Util.getMeasuringTimeNano();
        if (this.window.shouldClose()) {
            this.scheduleStop();
        }

        if (this.resourceReloadFuture != null && !(this.overlay instanceof SplashScreen)) {
            CompletableFuture<Void> completableFuture = this.resourceReloadFuture;
            this.resourceReloadFuture = null;
            this.reloadResources().thenRun(() -> {
                completableFuture.complete(null);
            });
        }

        Runnable runnable;
        while((runnable = (Runnable)this.renderTaskQueue.poll()) != null) {
            runnable.run();
        }

        int k;
        if (tick) {
            k = this.renderTickCounter.beginRenderTick(Util.getMeasuringTimeMs());
            this.profiler.push("scheduledExecutables");
            this.runTasks();
            this.profiler.pop();
            this.profiler.push("tick");

            for(int j = 0; j < Math.min(10, k); ++j) {
                this.profiler.visit("clientTick");
                this.tick();
            }

            this.profiler.pop();
        }

        //this.mouse.updateMouse();
        this.window.setPhase("Render");
        this.profiler.push("sound");
        this.soundManager.updateListenerPosition(this.gameRenderer.getCamera());
        this.profiler.pop();
        this.profiler.push("render");

        OpenMCVRClient.INSTANCE.setRenderLocation(RenderLocation.RIGHT);
        renderWithTransforms(l, tick);

        OpenMCVRClient.INSTANCE.setRenderLocation(RenderLocation.LEFT);
        renderWithTransforms(l, tick);

        OpenMCVRClient.INSTANCE.setRenderLocation(RenderLocation.CENTER);
        renderWithTransforms(l, tick);

        this.profiler.push("blit");
        RenderSystem.pushMatrix();
        this.getFramebuffer().draw(this.getFramebuffer().textureWidth, this.getFramebuffer().textureHeight);
        RenderSystem.popMatrix();
        this.profiler.swap("updateDisplay");
        this.window.swapBuffers();
        k = this.getFramerateLimit();
        if ((double)k < Option.FRAMERATE_LIMIT.getMax()) {
            RenderSystem.limitDisplayFPS(k);
        }

        this.profiler.swap("yield");
        Thread.yield();
        this.profiler.pop();
        this.window.setPhase("Post render");
        ++this.fpsCounter;
        boolean bl = this.isIntegratedServerRunning() && (this.currentScreen != null && this.currentScreen.isPauseScreen() || this.overlay != null && this.overlay.pausesGame()) && !this.server.isRemote();
        if (this.paused != bl) {
            if (this.paused) {
                this.pausedTickDelta = this.renderTickCounter.tickDelta;
            } else {
                this.renderTickCounter.tickDelta = this.pausedTickDelta;
            }

            this.paused = bl;
        }

        long m = Util.getMeasuringTimeNano();
        this.metricsData.pushSample(m - this.lastMetricsSampleTime);
        this.lastMetricsSampleTime = m;
        this.profiler.push("fpsUpdate");

        while(Util.getMeasuringTimeMs() >= this.nextDebugInfoUpdateTime + 1000L) {
            this.currentFps = this.fpsCounter;
            this.fpsDebugString = String.format("%d fps T: %s%s%s%s B: %d", currentFps, (double)this.options.maxFps == Option.FRAMERATE_LIMIT.getMax() ? "inf" : this.options.maxFps, this.options.enableVsync ? " vsync" : "", this.options.graphicsMode.toString(), this.options.cloudRenderMode == CloudRenderMode.OFF ? "" : (this.options.cloudRenderMode == CloudRenderMode.FAST ? " fast-clouds" : " fancy-clouds"), this.options.biomeBlendRadius);
            this.nextDebugInfoUpdateTime += 1000L;
            this.fpsCounter = 0;
            this.snooper.update();
            if (!this.snooper.isActive()) {
                this.snooper.method_5482();
            }
        }

        this.profiler.pop();
    }

    private void renderWithTransforms(long l, boolean tick) {
        RenderSystem.pushMatrix();
        RenderSystem.clear(16640, IS_SYSTEM_MAC);
        this.getFramebuffer().beginWrite(true);
        BackgroundRenderer.method_23792();
        this.profiler.push("display");
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        this.profiler.pop();
        if (!this.skipGameRender) {
            this.profiler.swap("gameRenderer");
            this.gameRenderer.render(this.paused ? this.pausedTickDelta : this.renderTickCounter.tickDelta, l, tick);
            this.profiler.swap("toasts");
            this.toastManager.draw(new MatrixStack());
            this.profiler.pop();
        }

        if (this.tickProfilerResult != null) {
            this.profiler.push("fpsPie");
            this.drawProfilerResults(new MatrixStack(), this.tickProfilerResult);
            this.profiler.pop();
        }

        RenderSystem.popMatrix();

        this.getFramebuffer().endWrite();
    }

    @Override
    public Framebuffer mcvr_getWindowFramebuffer() {
        return this.framebuffer;
    }
}
