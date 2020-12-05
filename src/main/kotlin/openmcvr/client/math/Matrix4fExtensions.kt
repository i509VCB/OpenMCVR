package openmcvr.client.math

import openmcvr.mixinterface.MCVRMatrix4f
import org.joml.Matrix4f
import org.lwjgl.openvr.HmdMatrix34
import org.lwjgl.openvr.HmdMatrix44

/**
 * Sets the values of this matrix from the OpenVR 3x4 matrix.
 * @return This.
 */
fun Matrix4f.setFromOVR43(mat: HmdMatrix34): Matrix4f {
    this.m00(mat.m(0))
    this.m10(mat.m(1))
    this.m20(mat.m(2))
    this.m30(mat.m(3))
    this.m01(mat.m(4))
    this.m11(mat.m(5))
    this.m21(mat.m(6))
    this.m31(mat.m(7))
    this.m02(mat.m(8))
    this.m12(mat.m(9))
    this.m22(mat.m(10))
    this.m32(mat.m(11))
    this.m03(0f)
    this.m13(0f)
    this.m23(0f)
    this.m33(1f)
    return this
}

/**
 * Sets the values of this matrix from the OpenVR 4x4 matrix.
 * @return This.
 */
fun Matrix4f.setFromOVR44(mat: HmdMatrix44): Matrix4f {
    this.m00(mat.m(0))
    this.m10(mat.m(1))
    this.m20(mat.m(2))
    this.m30(mat.m(3))
    this.m01(mat.m(4))
    this.m11(mat.m(5))
    this.m21(mat.m(6))
    this.m31(mat.m(7))
    this.m02(mat.m(8))
    this.m12(mat.m(9))
    this.m22(mat.m(10))
    this.m32(mat.m(11))
    this.m03(mat.m(12))
    this.m13(mat.m(13))
    this.m23(mat.m(14))
    this.m33(mat.m(15))
    return this
}

/**
 * Convenience method, turns a JOML matrix into a native MC one.
 * @return An equivalent Minecraft matrix.
 */
fun Matrix4f.toMCMatrix(): net.minecraft.util.math.Matrix4f {
    return (net.minecraft.util.math.Matrix4f() as MCVRMatrix4f).setFromJomlMatrix(this)
}