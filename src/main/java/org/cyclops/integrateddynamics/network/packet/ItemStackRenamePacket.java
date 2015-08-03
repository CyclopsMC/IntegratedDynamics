package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;

/**
 * Packet for renaming a regular itemstack.
 * @author rubensworks
 *
 */
public class ItemStackRenamePacket extends PacketCodec {

    @CodecField
    private String name;

    public ItemStackRenamePacket() {

    }

    public ItemStackRenamePacket(String name) {
        this.name = name;
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
		if(player.openContainer instanceof ContainerLabeller) {
			((ContainerLabeller) player.openContainer).setItemStackName(this.name);
		}
	}
	
}