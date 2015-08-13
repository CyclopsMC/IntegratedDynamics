package org.cyclops.integrateddynamics.core.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartSettings;

/**
 * Packet for setting the current update interval for a part.
 * @author rubensworks
 *
 */
public class ActionSetUpdateIntervalPacket extends PacketCodec {

	@CodecField
	private int updateInterval;

    public ActionSetUpdateIntervalPacket() {

    }

    public ActionSetUpdateIntervalPacket(int updateInterval) {
        this.updateInterval = updateInterval;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {

	}

	@SuppressWarnings("unchecked")
    @Override
	public void actionServer(World world, EntityPlayerMP player) {
        if(player.openContainer instanceof ContainerPartSettings) {
            ContainerPartSettings container = ((ContainerPartSettings) player.openContainer);
			container.getPartType().setUpdateInterval(container.getPartState(), updateInterval);
        }
	}
	
}