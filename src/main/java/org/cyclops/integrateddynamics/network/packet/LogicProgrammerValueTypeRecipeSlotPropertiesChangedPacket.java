package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.ingredient.ItemMatchProperties;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeRecipeLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a to the server if recipe slot properties have changed.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket extends PacketCodec {

	@CodecField
	private int slot;
	@CodecField
	private boolean nbt;
	@CodecField
	private String tag;
	@CodecField
	private int tagQuantity;

    public LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket() {

    }

    public LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket(int slot, boolean nbt, String tag, int tagQuantity) {
    	this.slot = slot;
		this.nbt = nbt;
		this.tag = tag;
		this.tagQuantity = tagQuantity;
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
		if(player.containerMenu instanceof ContainerLogicProgrammerBase) {
			ILogicProgrammerElement element = ((ContainerLogicProgrammerBase) player.containerMenu).getActiveElement();
			if(element instanceof ValueTypeRecipeLPElement) {
				ItemMatchProperties props = ((ValueTypeRecipeLPElement) element).getInputStacks().get(slot);
				props.setNbt(nbt);
				props.setItemTag(tag.isEmpty() ? null : tag);
				props.setTagQuantity(this.tagQuantity);
                ((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
			}
		}
	}
	
}