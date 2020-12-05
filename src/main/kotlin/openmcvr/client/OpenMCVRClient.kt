/*
 * Copyright (c) 2020 OpenMCVR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openmcvr.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.util.math.EulerAngle
import net.minecraft.util.math.Quaternion
import openmcvr.client.math.*
import openmcvr.client.player.VRPlayer
import openmcvr.mixinterface.EyeAlternator
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.openvr.*
import org.lwjgl.openvr.VR.*
import org.lwjgl.openvr.VRSystem.*
import org.lwjgl.system.MemoryStack.stackPush

fun init() {
    var token: Int? = null
    stackPush().use {
        val buf = it.mallocInt(1)
        token = VR_InitInternal(buf, EVRApplicationType_VRApplication_Scene)
    }
    OpenVR.create(token!!)
}

fun getFramebufferFromEye(eye: RenderLocation): Framebuffer {
    return when(eye) {
        RenderLocation.CENTER -> (MinecraftClient.getInstance() as EyeAlternator).mcvr_getWindowFramebuffer()
        RenderLocation.RIGHT -> OpenMCVRClient.rightEyeBuf!!
        RenderLocation.LEFT -> OpenMCVRClient.leftEyeBuf!!
    }
}

fun toQuaternion(euler: EulerAngle): Quaternion{
    val c1 = Math.cos(euler.yaw / 2.0).toFloat()
    val s1 = Math.sin(euler.yaw / 2.0).toFloat()
    val c2 = Math.cos(euler.pitch / 2.0).toFloat()
    val s2 = Math.sin(euler.pitch / 2.0).toFloat()
    val c3 = Math.cos(euler.roll / 2.0).toFloat()
    val s3 = Math.sin(euler.roll / 2.0).toFloat()
    val c1c2 = c1 * c2
    val s1s2 = s1 * s2

    return Quaternion(c1c2*c3 - s1s2*s3, c1c2*s3 + s1s2*c3, s1*c2*c3 + c1*s2*s3, c1*s2*c3 - s1*c2*s3)
}

fun safeAngle(angle: Float): Float {
    return Math.round(angle * 10f) / 10f
}

@Environment(EnvType.CLIENT)
object OpenMCVRClient : ClientModInitializer {
    override fun onInitializeClient() {

    }

    val fPI = Math.PI.toFloat()

    var firstFramePassed = false

    var rightEyeBuf: Framebuffer? = null
    var leftEyeBuf: Framebuffer? = null

    private var headTransform: Matrix4f = Matrix4f().identity()
    private var leftEyeTransform: Matrix4f = Matrix4f().identity()
    private var rightEyeTransform: Matrix4f = Matrix4f().identity()

    var leftEyeProjection: Matrix4f? = null
    var rightEyeProjection: Matrix4f? = null

    var eye: RenderLocation = RenderLocation.CENTER

    fun getFramebuffer(): Framebuffer {
        return getFramebufferFromEye(eye)
    }

    /**
     * @return A minecraft matrix for the
     */
    fun getHeadTransform(): net.minecraft.util.math.Matrix4f {
        return headTransform!!.toMCMatrix()
    }

    fun getEyeTransform(): net.minecraft.util.math.Matrix4f {
        return when(eye) {
            RenderLocation.CENTER -> Matrix4f().identity().toMCMatrix()
            RenderLocation.RIGHT -> rightEyeTransform!!.toMCMatrix()
            RenderLocation.LEFT -> leftEyeTransform!!.toMCMatrix()
        }
    }

    fun getEyeProjection(): net.minecraft.util.math.Matrix4f {
        return when(eye) {
            RenderLocation.CENTER -> Matrix4f().identity().toMCMatrix()
            RenderLocation.RIGHT -> rightEyeProjection!!.toMCMatrix()
            RenderLocation.LEFT -> leftEyeProjection!!.toMCMatrix()
        }
    }

    fun setRenderLocation(eye: RenderLocation) {
        this.eye = eye;
    }

    fun cleanup() {
        OpenVR.destroy()
        VR_ShutdownInternal()
    }

    var frames = 0

    fun onFrame() {
        if(!firstFramePassed) {
            rightEyeBuf = Framebuffer(2048, 2048, true, true)
            leftEyeBuf = Framebuffer(2048, 2048, true, true)
        }
        firstFramePassed = true

        val player = MinecraftClient.getInstance().player

        stackPush().use { stack ->

            val trackedDevicePose: TrackedDevicePose.Buffer = TrackedDevicePose.Buffer(stack.malloc(k_unMaxTrackedDeviceCount * TrackedDevicePose.SIZEOF))
            val renderDevicePose: TrackedDevicePose.Buffer = TrackedDevicePose.Buffer(stack.malloc(k_unMaxTrackedDeviceCount * TrackedDevicePose.SIZEOF))
            VRCompositor.VRCompositor_WaitGetPoses(trackedDevicePose, renderDevicePose)

            renderDevicePose.mDeviceToAbsoluteTracking()

            val tracking = renderDevicePose.mDeviceToAbsoluteTracking()

            headTransform = Matrix4f().setFromOVR43(tracking)
            headTransform.invertAffine()
            if(player != null) {
                VRPlayer.getFromPlayer(player).headTransform = headTransform
            }
        }

        renderToEye(RenderLocation.LEFT)
        renderToEye(RenderLocation.RIGHT)

        stackPush().use {
            leftEyeTransform = Matrix4f().setFromOVR43(
                    VRSystem_GetEyeToHeadTransform(EVREye_Eye_Left, HmdMatrix34.mallocStack())
            )
            rightEyeTransform = Matrix4f().setFromOVR43(
                    VRSystem_GetEyeToHeadTransform(EVREye_Eye_Right, HmdMatrix34.mallocStack())
            )

            val farPlane = MinecraftClient.getInstance().gameRenderer.viewDistance * 4.0f
            leftEyeProjection = Matrix4f().setFromOVR44(
                    VRSystem_GetProjectionMatrix(EVREye_Eye_Left, 0.05f, farPlane, HmdMatrix44.mallocStack())
            )
            rightEyeProjection = Matrix4f().setFromOVR44(
                    VRSystem_GetProjectionMatrix(EVREye_Eye_Right, 0.05f, farPlane, HmdMatrix44.mallocStack())
            )
        }

        frames++
    }

    fun renderToEye(eye: RenderLocation) {
        val framebuffer = getFramebufferFromEye(eye);
        val buffer = BufferUtils.createByteBuffer(Texture.SIZEOF)

        val textureObj = Texture(buffer)

        framebuffer.beginRead()
        textureObj.set(framebuffer.colorAttachment.toLong(), ETextureType_TextureType_OpenGL, EColorSpace_ColorSpace_Gamma)
        textureObj.eType(ETextureType_TextureType_OpenGL)
        textureObj.eColorSpace(EColorSpace_ColorSpace_Gamma)

        val ovrEye = when(eye) {
            RenderLocation.RIGHT -> EVREye_Eye_Right
            RenderLocation.LEFT -> EVREye_Eye_Left
            RenderLocation.CENTER -> -1
        }
        VRCompositor.VRCompositor_Submit(ovrEye, textureObj, null, EVRSubmitFlags_Submit_Default)

        VRCompositor.VRCompositor_PostPresentHandoff()
        framebuffer.endRead()

    }
}
