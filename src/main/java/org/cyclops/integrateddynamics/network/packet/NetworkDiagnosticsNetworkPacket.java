package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDataClient;
import org.cyclops.integrateddynamics.core.network.diagnostics.RawNetworkData;

/**
 * Packet for subscribing a network update to a player.
 * @author rubensworks
 *
 */
public class NetworkDiagnosticsNetworkPacket extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "network_diagnostics_network");

    @CodecField
    private CompoundTag networkData;

    public NetworkDiagnosticsNetworkPacket() {
        super(ID);
    }

    public NetworkDiagnosticsNetworkPacket(CompoundTag networkData) {
        super(ID);
        this.networkData = networkData;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        RawNetworkData networkData = RawNetworkData.fromNbt(this.networkData);
        if (networkData.getParts().isEmpty()) {
            // Force observers to be cleared when no parts are present.
            networkData.getObservers().clear();
        }
        NetworkDataClient.setNetworkData(networkData.getId(), networkData.isKilled() ? null : networkData);
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
