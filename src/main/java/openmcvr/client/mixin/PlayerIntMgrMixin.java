package openmcvr.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import openmcvr.client.player.ClientVRPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayerInteractionManager.class)
public class PlayerIntMgrMixin {
    @Shadow @Final private ClientPlayNetworkHandler networkHandler;

    @Shadow @Final private MinecraftClient client;

    /**
     * @author SoapyXM
     * @reason To redirect new client player creation to a custom VR one.
     */
    @Overwrite
    public ClientPlayerEntity createPlayer(ClientWorld world, StatHandler statHandler, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
        return new ClientVRPlayerEntity(this.client, world, this.networkHandler, statHandler, recipeBook, lastSneaking, lastSprinting);
    }
}
