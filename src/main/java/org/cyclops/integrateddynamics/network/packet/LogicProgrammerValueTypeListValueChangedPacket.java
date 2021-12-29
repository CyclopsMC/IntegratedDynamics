package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeListLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeListValueChangedPacket extends PacketCodec {

	@CodecField
	private Tag value;

    public LogicProgrammerValueTypeListValueChangedPacket() {

    }

    public LogicProgrammerValueTypeListValueChangedPacket(ValueTypeList.ValueList value) {
		this.value = ValueHelpers.serializeRaw(value);
    }

	protected ValueTypeList.ValueList getListValue() {
		return ValueHelpers.deserializeRaw(ValueTypes.LIST, value);
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
			if(element instanceof ValueTypeListLPElement) {
				((ValueTypeListLPElement) element).setServerValue(getListValue());
				((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
			}
		}
	}
	
}