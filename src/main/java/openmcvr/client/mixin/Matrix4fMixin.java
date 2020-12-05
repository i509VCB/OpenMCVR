package openmcvr.client.mixin;

import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import openmcvr.mixinterface.MCVRMatrix4f;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix4f.class)
public class Matrix4fMixin implements MCVRMatrix4f {
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
    public Matrix4f setFromJomlMatrix(org.joml.Matrix4f val) {
        this.a00 = val.m00();
        this.a10 = val.m01();
        this.a20 = val.m02();
        this.a30 = val.m03();
        this.a01 = val.m10();
        this.a11 = val.m11();
        this.a21 = val.m12();
        this.a31 = val.m13();
        this.a02 = val.m20();
        this.a12 = val.m21();
        this.a22 = val.m22();
        this.a32 = val.m23();
        this.a03 = val.m20();
        this.a13 = val.m21();
        this.a23 = val.m22();
        this.a33 = val.m23();
        this.a33 = val.m33();
        return (Matrix4f)((Object)this);
    }

    @Override
    public org.joml.Matrix4f toJomlMatrix(Matrix4f val) {
        return new org.joml.Matrix4f(a00, a10, a20, a30, a01, a11, a21, a31, a02, a12, a22, a32, a03, a13, a23, a33);
    }
}
