package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.network.PartOffsetsClientNotifier;

/**
 * Packet for subscribing a player to network diagnostics updates.
 * @author rubensworks
 *
 */
public class PartOffsetsSubscribePacket extends PacketCodec {

    @CodecField
    private boolean subscribe;

    public PartOffsetsSubscribePacket() {

    }

    public PartOffsetsSubscribePacket(boolean subscribe) {
        this.subscribe = subscribe;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        if (subscribe) {
            PartOffsetsClientNotifier.getInstance().registerPlayer(player);
        } else {
            PartOffsetsClientNotifier.getInstance().unRegisterPlayer(player);
        }
    }

}
