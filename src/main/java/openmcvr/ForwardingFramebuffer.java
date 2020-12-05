package openmcvr;

import net.minecraft.client.gl.Framebuffer;
import openmcvr.client.OpenMCVRClient;
import openmcvr.client.framebuffer.FramebufferPile;
import openmcvr.client.framebuffer.FramebufferType;

import java.util.Objects;

public class ForwardingFramebuffer extends Framebuffer {
    private final FramebufferType type;

    public ForwardingFramebuffer(FramebufferType type, int width, int height, boolean useDepth, boolean getError) {
        super(width, height, useDepth, getError);
        this.type = type;
    }

    private Framebuffer getForwardingTarget() {
        FramebufferPile framebuffers = OpenMCVRClient.INSTANCE.getFramebufferPile();
        return framebuffers.getFramebuffersByType().get(type);
    }

    @Override
    public void resize(int width, int height, boolean getError) {
        if(type == null) {
            Objects.requireNonNull(OpenMCVRClient.INSTANCE.getRightEyeBuffers()).getFramebuffersByType().values().forEach(val -> {
                val.resize(width, height, getError);
            });
            Objects.requireNonNull(OpenMCVRClient.INSTANCE.getLeftEyeBuffers()).getFramebuffersByType().values().forEach(val -> {
                val.resize(width, height, getError);
            });
            Objects.requireNonNull(OpenMCVRClient.INSTANCE.getCenterBuffers()).getFramebuffersByType().values().forEach(val -> {
                val.resize(width, height, getError);
            });
        } else {
            getForwardingTarget().resize(width, height, getError);
        }
    }

    @Override
    public void delete() {
        getForwardingTarget().delete();
    }

    @Override
    public void copyDepthFrom(Framebuffer framebuffer) {
        getForwardingTarget().copyDepthFrom(framebuffer);
    }

    @Override
    public void initFbo(int width, int height, boolean getError) {
        getForwardingTarget().initFbo(width, height, getError);
    }

    @Override
    public void setTexFilter(int i) {
        getForwardingTarget().setTexFilter(i);
    }

    @Override
    public void checkFramebufferStatus() {
        getForwardingTarget().checkFramebufferStatus();
    }

    @Override
    public void beginRead() {
        getForwardingTarget().beginRead();
    }

    @Override
    public void endRead() {
        getForwardingTarget().endRead();
    }

    @Override
    public void beginWrite(boolean setViewport) {
        getForwardingTarget().beginWrite(setViewport);
    }

    @Override
    public void endWrite() {
        getForwardingTarget().endWrite();
    }

    @Override
    public void setClearColor(float r, float g, float b, float a) {
        getForwardingTarget().setClearColor(r, g, b, a);
    }

    @Override
    public void draw(int width, int height) {
        getForwardingTarget().draw(width, height);
    }

    @Override
    public void draw(int width, int height, boolean bl) {
        getForwardingTarget().draw(width, height, bl);
    }

    @Override
    public void clear(boolean getError) {
        getForwardingTarget().clear(getError);
    }

    @Override
    public int getColorAttachment() {
        return getForwardingTarget().getColorAttachment();
    }

    @Override
    public int getDepthAttachment() {
        return getForwardingTarget().getDepthAttachment();
    }
}
