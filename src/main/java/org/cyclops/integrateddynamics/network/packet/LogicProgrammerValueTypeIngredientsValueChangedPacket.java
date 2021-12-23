package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeIngredientsLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for a change in current ingredients value.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeIngredientsValueChangedPacket extends PacketCodec {

	@CodecField
	private INBT value;

    public LogicProgrammerValueTypeIngredientsValueChangedPacket() {

    }

    public LogicProgrammerValueTypeIngredientsValueChangedPacket(ValueObjectTypeIngredients.ValueIngredients value) {
		this.value = value.getType().serialize(value);
    }

	protected ValueObjectTypeIngredients.ValueIngredients getValue() {
		return ValueHelpers.deserializeRaw(ValueTypes.OBJECT_INGREDIENTS, value);
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
			if(element instanceof ValueTypeIngredientsLPElement) {
				((ValueTypeIngredientsLPElement) element).setServerValue(getValue());
				((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
			}
		}
	}
	
}