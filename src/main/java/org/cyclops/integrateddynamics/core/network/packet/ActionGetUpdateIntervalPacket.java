package org.cyclops.integrateddynamics.core.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.client.gui.container.GuiPartSettings;

/**
 * Packet for getting the current update interval for a part to display client side in the gui.
 * @author rubensworks
 *
 */
public class ActionGetUpdateIntervalPacket extends PacketCodec {

	@CodecField
	private int updateInterval;

    public ActionGetUpdateIntervalPacket() {

    }

    public ActionGetUpdateIntervalPacket(int updateInterval) {
        this.updateInterval = updateInterval;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		GuiPartSettings.LAST_UPDATE_INTERVAL = updateInterval;
	}

	@SuppressWarnings("unchecked")
    @Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}