package openmcvr.client.player

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.recipebook.ClientRecipeBook
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.stat.StatHandler
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Implements various features required for VR controls and collisions on the server-side.
 */
class ClientVRPlayerEntity(client: MinecraftClient?, world: ClientWorld?, networkHandler: ClientPlayNetworkHandler?, stats: StatHandler?, recipeBook: ClientRecipeBook?, lastSneaking: Boolean, lastSprinting: Boolean) :
        ClientPlayerEntity(client, world, networkHandler, stats, recipeBook, lastSneaking, lastSprinting) {
    private val origin: Vector3f = Vector3f()
    var rotation: Quaternionf = Quaternionf()

    var headTransform: Matrix4f = Matrix4f().identity()
}