package openmcvr.mixinterface;

import net.minecraft.util.math.Matrix4f;
import org.lwjgl.openvr.HmdMatrix34;

public interface OVRCompatMatrix4f {
    Matrix4f setFromOVR43(HmdMatrix34 val);
}
