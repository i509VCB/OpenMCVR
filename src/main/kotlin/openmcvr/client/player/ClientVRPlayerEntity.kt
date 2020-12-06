package openmcvr.client.player

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.recipebook.ClientRecipeBook
import net.minecraft.client.world.ClientWorld
import net.minecraft.stat.StatHandler
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Implements various features required for VR controls and collisions on the server-side.
 */
@Environment(EnvType.CLIENT)
class ClientVRPlayerEntity(client: MinecraftClient?, world: ClientWorld?, networkHandler: ClientPlayNetworkHandler?, stats: StatHandler?, recipeBook: ClientRecipeBook?, lastSneaking: Boolean, lastSprinting: Boolean) :
        ClientPlayerEntity(client, world, networkHandler, stats, recipeBook, lastSneaking, lastSprinting) {
    val rotation: Quaternionf = Quaternionf()
    val headTransform: Matrix4f = Matrix4f().identity()

    private var lastRoomPos: Vector3f? = null
    private var currentRoomPos: Vector3f? = null

    fun updateHead(matrix: Matrix4f) {
        headTransform.set(matrix)
        rotation.set(Matrix4f(matrix).invert().getNormalizedRotation(Quaternionf()))
        val nextRoomPos = matrix.getTranslation(Vector3f())

        // first frame initialization stuff
        if(lastRoomPos == null) {
            lastRoomPos = Vector3f(nextRoomPos)
        } else {
            lastRoomPos = Vector3f(currentRoomPos)
        }
        currentRoomPos = nextRoomPos

        // compute distance moved since last frame
        val offset = Vector3f(currentRoomPos).sub(lastRoomPos)

        // offset player pos
        val mcPos = pos
        setPos(mcPos.x + offset.x, mcPos.y + offset.y, mcPos.z + offset.z)
    }
}