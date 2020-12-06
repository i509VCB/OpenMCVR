package openmcvr.common.player

import com.mojang.authlib.GameProfile
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.network.ServerPlayerInteractionManager
import net.minecraft.server.world.ServerWorld

/**
 * Implements various features required for VR controls and collisions on the server-side.
 */
class ServerVRPlayerEntity(server: MinecraftServer?, world: ServerWorld?, profile: GameProfile?, interactionManager: ServerPlayerInteractionManager?) :
        ServerPlayerEntity(server, world, profile, interactionManager) {

}