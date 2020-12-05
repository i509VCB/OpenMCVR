package openmcvr.client.player

import net.minecraft.entity.player.PlayerEntity
import openmcvr.mixinterface.HasVRPlayer
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class VRPlayer {
    private val origin: Vector3f = Vector3f()
    private val rotation: Quaternionf = Quaternionf()

    var headTransform: Matrix4f = Matrix4f().identity()

    companion object {
        /**
         * Convenience function.
         * @see HasVRPlayer
         * @return The VR player attached to a given player object.
         */
        fun getFromPlayer(player: PlayerEntity): VRPlayer {
            return (player as HasVRPlayer).vrPlayer
        }
    }
}