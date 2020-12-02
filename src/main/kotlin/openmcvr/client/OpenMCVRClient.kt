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
import net.minecraft.client.render.Camera
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f
import openmcvr.mixinterface.OVRCompatMatrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.openvr.*
import org.lwjgl.openvr.VR.*
import org.lwjgl.system.MemoryStack.stackPush
import java.nio.IntBuffer

fun init() {
    var token: Int? = null
    stackPush().use {
        val buf = it.mallocInt(1)
        token = VR_InitInternal(buf, EVRApplicationType_VRApplication_Scene)
    }
    OpenVR.create(token!!)
}

@Environment(EnvType.CLIENT)
object OpenMCVRClient : ClientModInitializer {
    override fun onInitializeClient() {

    }

    val singleIntBuffer: IntBuffer = IntBuffer.allocate(1)
    var firstFramePassed = false

    var rightEyeBuf: Framebuffer? = null
    var leftEyeBuf: Framebuffer? = null

    var rightEyeCamera: Camera? = null
    var leftEyeCamera: Camera? = null

    fun cleanup() {
        OpenVR.destroy()
        VR_ShutdownInternal()
    }

    fun onFrame() {
        /*if(!firstFramePassed) {
            rightEyeBuf = Framebuffer(2048, 2048, true, true)
            leftEyeBuf = Framebuffer(2048, 2048, true, true)

            rightEyeCamera = Camera()
            leftEyeCamera = Camera()
        }
        firstFramePassed = true*/
        stackPush().use { stack ->

            val trackedDevicePose: TrackedDevicePose.Buffer = TrackedDevicePose.Buffer(stack.malloc(k_unMaxTrackedDeviceCount * TrackedDevicePose.SIZEOF))
            val renderDevicePose: TrackedDevicePose.Buffer = TrackedDevicePose.Buffer(stack.malloc(k_unMaxTrackedDeviceCount * TrackedDevicePose.SIZEOF))
            VRCompositor.VRCompositor_WaitGetPoses(trackedDevicePose, renderDevicePose)

            renderDevicePose.mDeviceToAbsoluteTracking()

            /*val unfriendlyTracking = renderDevicePose.mDeviceToAbsoluteTracking()

            val headView = (Matrix4f() as OVRCompatMatrix4f).setFromOVR43(unfriendlyTracking)
            headView.invert()*/

            val framebuffer = MinecraftClient.getInstance().framebuffer

            val buffer = BufferUtils.createByteBuffer(Texture.SIZEOF)

            val textureObj = Texture(buffer)

            framebuffer.beginRead()
            textureObj.set(framebuffer.colorAttachment.toLong(), ETextureType_TextureType_OpenGL, EColorSpace_ColorSpace_Gamma)
            textureObj.eType(ETextureType_TextureType_OpenGL)
            textureObj.eColorSpace(EColorSpace_ColorSpace_Gamma)

            VRCompositor.VRCompositor_Submit(EVREye_Eye_Left, textureObj, null, EVRSubmitFlags_Submit_Default)
            VRCompositor.VRCompositor_Submit(EVREye_Eye_Right, textureObj, null, EVRSubmitFlags_Submit_Default)

            VRCompositor.VRCompositor_PostPresentHandoff()
            framebuffer.endRead()
        }
    }
}
