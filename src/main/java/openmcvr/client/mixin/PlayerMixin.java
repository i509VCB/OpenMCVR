package openmcvr.client.mixin;

import net.minecraft.entity.player.PlayerEntity;
import openmcvr.client.player.VRPlayer;
import openmcvr.mixinterface.HasVRPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public class PlayerMixin implements HasVRPlayer {
    private final VRPlayer openmcvr_vrplayer = new VRPlayer();

    @Override
    public VRPlayer getVRPlayer() {
        return openmcvr_vrplayer;
    }
}
