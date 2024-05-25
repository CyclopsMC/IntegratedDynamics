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
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnostics;

/**
 * Packet for subscribing a player to network diagnostics updates.
 * @author rubensworks
 *
 */
public class NetworkDiagnosticsSubscribePacket extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "network_diagnostics_subscribe");

    @CodecField
    private boolean subscribe;

    public NetworkDiagnosticsSubscribePacket() {
        super(ID);
    }

    public NetworkDiagnosticsSubscribePacket(boolean subscribe) {
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
            NetworkDiagnostics.getInstance().registerPlayer(player);
        } else {
            NetworkDiagnostics.getInstance().unRegisterPlayer(player);
        }
    }

    public static NetworkDiagnosticsSubscribePacket subscribe() {
        return new NetworkDiagnosticsSubscribePacket(true);
    }

    public static NetworkDiagnosticsSubscribePacket unsubscribe() {
        return new NetworkDiagnosticsSubscribePacket(false);
    }

}
