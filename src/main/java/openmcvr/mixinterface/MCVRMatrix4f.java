package openmcvr.mixinterface;


import net.minecraft.util.math.Matrix4f;

public interface MCVRMatrix4f {
    Matrix4f setFromJomlMatrix(org.joml.Matrix4f val);
    org.joml.Matrix4f toJomlMatrix(Matrix4f val);
}
