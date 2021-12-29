package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
	@OnlyIn(Dist.CLIENT)
	public void actionClient(Level world, Player player) {
		
	}

	@Override
	public void actionServer(Level world, ServerPlayer player) {
		if(player.containerMenu instanceof ContainerLogicProgrammerBase) {
			ILogicProgrammerElement element = ((ContainerLogicProgrammerBase) player.containerMenu).getActiveElement();
			if(element instanceof ValueTypeLPElementBase) {
				int slotId = player.containerMenu.slots.size() - 1;
				player.containerMenu.setItem(slotId, 0, itemStack.copy());
			}
		}
	}
	
}