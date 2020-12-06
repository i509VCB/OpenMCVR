package openmcvr.client.player

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.recipebook.ClientRecipeBook
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.stat.StatHandler
import openmcvr.mixinterface.HasVRPlayer
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class VRPlayer(client: MinecraftClient?, world: ClientWorld?, networkHandler: ClientPlayNetworkHandler?, stats: StatHandler?, recipeBook: ClientRecipeBook?, lastSneaking: Boolean, lastSprinting: Boolean) :
        ClientPlayerEntity(client, world, networkHandler, stats, recipeBook, lastSneaking, lastSprinting) {
    private val origin: Vector3f = Vector3f()
    var rotation: Quaternionf = Quaternionf()

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