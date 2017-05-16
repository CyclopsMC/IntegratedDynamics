package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for letting the server know of a logic programmer element itemstack value.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeSlottedValueChangedPacket extends PacketCodec {

	@CodecField
	private ItemStack itemStack;

    public LogicProgrammerValueTypeSlottedValueChangedPacket() {

    }

    public LogicProgrammerValueTypeSlottedValueChangedPacket(ItemStack itemStack) {
		this.itemStack = itemStack;
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
			ILogicProgrammerElement element = ((ContainerLogicProgrammerBase) player.openContainer).getActiveElement();
			if(element instanceof ValueTypeLPElementBase) {
				int slotId = player.openContainer.inventorySlots.size() - 1;
				player.openContainer.putStackInSlot(slotId, itemStack.copy());
			}
		}
	}
	
}