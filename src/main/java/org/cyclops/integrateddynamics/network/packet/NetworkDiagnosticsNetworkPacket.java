package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.network.diagnostics.GuiNetworkDiagnostics;
import org.cyclops.integrateddynamics.core.network.diagnostics.RawNetworkData;

/**
 * Packet for subscribing a network update to a player.
 * @author rubensworks
 *
 */
public class NetworkDiagnosticsNetworkPacket extends PacketCodec {

    @CodecField
    private NBTTagCompound networkData;

    public NetworkDiagnosticsNetworkPacket() {

    }

    public NetworkDiagnosticsNetworkPacket(NBTTagCompound networkData) {
		this.networkData = networkData;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		RawNetworkData networkData = RawNetworkData.fromNbt(this.networkData);
		if (networkData.getParts().isEmpty()) {
			// Force observers to be cleared when no parts are present.
			networkData.getObservers().clear();
		}
		GuiNetworkDiagnostics.setNetworkData(networkData.getId(), networkData.isKilled() ? null : networkData);
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}