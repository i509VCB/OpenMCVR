package openmcvr.client.mixin;

import net.minecraft.util.math.Matrix4f;
import openmcvr.mixinterface.OVRCompatMatrix4f;
import org.lwjgl.openvr.HmdMatrix34;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix4f.class)
public class Matrix4fMixin implements OVRCompatMatrix4f {
    @Shadow protected float a00;
    @Shadow protected float a01;
    @Shadow protected float a02;
    @Shadow protected float a03;
    @Shadow protected float a10;
    @Shadow protected float a11;
    @Shadow protected float a12;
    @Shadow protected float a13;
    @Shadow protected float a20;
    @Shadow protected float a21;
    @Shadow protected float a22;
    @Shadow protected float a23;
    @Shadow protected float a30;
    @Shadow protected float a31;
    @Shadow protected float a32;
    @Shadow protected float a33;

    @Override
    public Matrix4f setFromOVR43(HmdMatrix34 val) {
        this.a00 = val.m(0);
        this.a01 = val.m(1);
        this.a02 = val.m(2);
        this.a03 = 0f;
        this.a10 = val.m(3);
        this.a11 = val.m(4);
        this.a12 = val.m(5);
        this.a13 = 0f;
        this.a20 = val.m(6);
        this.a21 = val.m(7);
        this.a22 = val.m(8);
        this.a23 = 0f;
        this.a30 = val.m(9);
        this.a31 = val.m(10);
        this.a32 = val.m(11);
        this.a33 = 1f;

        return (Matrix4f)((Object)this);
    }
}
