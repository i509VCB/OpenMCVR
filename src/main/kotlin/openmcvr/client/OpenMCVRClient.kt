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
import openmcvr.client.framebuffer.FramebufferPile
import openmcvr.client.framebuffer.FramebufferType
import openmcvr.client.math.setFromOVR43
import openmcvr.client.math.setFromOVR44
import openmcvr.client.math.toMCMatrix
import openmcvr.client.player.ClientVRPlayerEntity
import openmcvr.mixinterface.EyeAlternator
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.openvr.*
import org.lwjgl.openvr.VR.*
import org.lwjgl.openvr.VRSystem.VRSystem_GetEyeToHeadTransform
import org.lwjgl.openvr.VRSystem.VRSystem_GetProjectionMatrix
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
        RenderLocation.RIGHT -> OpenMCVRClient.rightEyeBuffers!!.framebuffersByType[FramebufferType.Solid]!!
        RenderLocation.LEFT -> OpenMCVRClient.leftEyeBuffers!!.framebuffersByType[FramebufferType.Solid]!!
    }
}

@Environment(EnvType.CLIENT)
object OpenMCVRClient : ClientModInitializer {
    override fun onInitializeClient() {

    }

    var firstFramePassed = false

    var rightEyeBuffers: FramebufferPile? = null
    var leftEyeBuffers: FramebufferPile? = null
    var centerBuffers: FramebufferPile? = null

    private var headTransform: Matrix4f = Matrix4f().identity()
    private var leftEyeTransform: Matrix4f = Matrix4f().identity()
    private var rightEyeTransform: Matrix4f = Matrix4f().identity()

    var leftEyeProjection: Matrix4f? = null
    var rightEyeProjection: Matrix4f? = null

    var eye: RenderLocation = RenderLocation.CENTER

    fun getFramebufferPile(): FramebufferPile {
        return when(eye) {
            RenderLocation.RIGHT -> rightEyeBuffers!!
            RenderLocation.LEFT -> leftEyeBuffers!!
            else -> centerBuffers!!
        }
    }

    fun getFramebuffer(): Framebuffer {
        return getFramebufferFromEye(eye)
    }

    /**
     * @return A minecraft matrix for the
     */
    fun getHeadTransform(): net.minecraft.util.math.Matrix4f {
        return headTransform.toMCMatrix()
    }

    fun getEyeTransform(): net.minecraft.util.math.Matrix4f {
        return when(eye) {
            RenderLocation.CENTER -> Matrix4f().identity().toMCMatrix()
            RenderLocation.RIGHT -> rightEyeTransform.toMCMatrix()
            RenderLocation.LEFT -> leftEyeTransform.toMCMatrix()
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
        this.eye = eye
    }

    fun cleanup() {
        OpenVR.destroy()
        VR_ShutdownInternal()
    }

    var frames = 0

    fun onFrame() {
        if(!firstFramePassed) {
            rightEyeBuffers = FramebufferPile(2048, 2048, useDepth = true, getError = true)
            leftEyeBuffers = FramebufferPile(2048, 2048, useDepth = true, getError = true)
            val window = MinecraftClient.getInstance().window
            centerBuffers = FramebufferPile(window.framebufferWidth, window.framebufferHeight, useDepth = true, getError = true)
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
                val vrPlayer = player as ClientVRPlayerEntity
                vrPlayer.updateHead(headTransform)

                if(frames % 2 == 0)
                    MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate()
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
        val framebuffer = getFramebufferFromEye(eye)
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
