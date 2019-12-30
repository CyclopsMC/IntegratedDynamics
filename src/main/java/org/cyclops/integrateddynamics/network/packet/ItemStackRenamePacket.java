package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
	@OnlyIn(Dist.CLIENT)
	public void actionClient(World world, PlayerEntity player) {
		
	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {
		if(player.openContainer instanceof ContainerLabeller) {
			((ContainerLabeller) player.openContainer).setItemStackName(this.name);
		}
	}
	
}