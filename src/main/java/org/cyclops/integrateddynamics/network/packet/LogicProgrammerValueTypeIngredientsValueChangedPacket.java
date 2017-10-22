package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
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
	private String value;

    public LogicProgrammerValueTypeIngredientsValueChangedPacket() {

    }

    public LogicProgrammerValueTypeIngredientsValueChangedPacket(ValueObjectTypeIngredients.ValueIngredients value) {
		this.value = value.getType().serialize(value);
    }

	protected ValueObjectTypeIngredients.ValueIngredients getValue() {
		return ValueTypes.OBJECT_INGREDIENTS.deserialize(value);
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
			if(element instanceof ValueTypeIngredientsLPElement) {
				((ValueTypeIngredientsLPElement) element).setServerValue(getValue());
				((ContainerLogicProgrammerBase) player.openContainer).onDirty();
			}
		}
	}
	
}