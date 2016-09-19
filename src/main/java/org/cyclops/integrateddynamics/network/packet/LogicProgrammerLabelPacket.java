package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet to trigger variable labeling.
 * @author rubensworks
 *
 */
public class LogicProgrammerLabelPacket extends PacketCodec {

    @CodecField
    private String label;

    public LogicProgrammerLabelPacket() {

    }

    public LogicProgrammerLabelPacket(String label) {
		this.label = label;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {
		if(player.openContainer instanceof ContainerLogicProgrammerBase) {
			((ContainerLogicProgrammerBase) player.openContainer).onLabelPacket(label);
		}
	}
	
}