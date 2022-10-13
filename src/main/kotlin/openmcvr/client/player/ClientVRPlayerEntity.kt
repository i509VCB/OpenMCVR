package openmcvr.client.player

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.recipebook.ClientRecipeBook
import net.minecraft.client.world.ClientWorld
import net.minecraft.stat.StatHandler
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import org.joml.*
import java.lang.Math

/**
 * Implements various features required for VR controls and collisions on the server-side.
 */
@Environment(EnvType.CLIENT)
class ClientVRPlayerEntity(client: MinecraftClient?, world: ClientWorld?, networkHandler: ClientPlayNetworkHandler?, stats: StatHandler?, recipeBook: ClientRecipeBook?, lastSneaking: Boolean, lastSprinting: Boolean) :
        ClientPlayerEntity(client, world, networkHandler, stats, recipeBook, lastSneaking, lastSprinting) {
    val rotation: Quaternionf = Quaternionf()
    val headTransform: Matrix4f = Matrix4f().identity()

    /**
     * DO NOT USE THESE FOR CAMERA TRANSFORMS, THINGS GET WEIRD
     */
    private val eulerAngles: Vector3f = Vector3f()

    private var lastRoomPos: Vector3f? = null
    private var currentRoomPos: Vector3f? = null

    private val radianConv = 180f / Math.PI

    fun updateHead(matrix: Matrix4f) {
        headTransform.set(matrix)
        val inverseMatrix = Matrix4f(matrix).invert()

        // rotation
        rotation.set(inverseMatrix.getUnnormalizedRotation(Quaternionf()))

        val nextRoomPos = inverseMatrix.getTranslation(Vector3f())

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
        setPos(mcPos.x - offset.x, mcPos.y + offset.y, mcPos.z - offset.z)
    }

    override fun getPitch(tickDelta: Float): Float {
        return eulerAngles.x
    }

    override fun getYaw(tickDelta: Float): Float {
        return eulerAngles.y
    }

    override fun raycast(maxDistance: Double, tickDelta: Float, includeFluids: Boolean): HitResult {
        val vec3d = getCameraPosVecVR(tickDelta)
        val vec3d2 = getRotationVecVR(tickDelta)
        val vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance)
        System.out.printf("POS: %.2f, %.2f, %.2f \n", vec3d.x, vec3d.y, vec3d.z)
        //System.out.printf("ROT: %.2f, %.2f, %.2f \n", vec3d2.x, vec3d2.y, vec3d2.z)
        return world.raycast(RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, if (includeFluids) RaycastContext.FluidHandling.ANY else RaycastContext.FluidHandling.NONE, this))
    }

    private fun getRotationVecVR(tickDelta: Float): Vec3d {
        val vec = Vector3d(0.0, 0.0, 1.0)
        vec.rotate(Quaterniond(rotation))
        return Vec3d(vec.x, -vec.y, vec.z)
    }

    private fun getCameraPosVecVR(tickDelta: Float): Vec3d {
        return Vec3d(this.pos.x, this.pos.y, this.pos.z)
    }


}