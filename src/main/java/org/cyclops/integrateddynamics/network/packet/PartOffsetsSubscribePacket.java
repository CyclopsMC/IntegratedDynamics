package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.network.PartOffsetsClientNotifier;

/**
 * Packet for subscribing a player to network diagnostics updates.
 * @author rubensworks
 *
 */
public class PartOffsetsSubscribePacket extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "part_offsets_subscribe");

    @CodecField
    private boolean subscribe;

    public PartOffsetsSubscribePacket() {
        super(ID);
    }

    public PartOffsetsSubscribePacket(boolean subscribe) {
        super(ID);
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
