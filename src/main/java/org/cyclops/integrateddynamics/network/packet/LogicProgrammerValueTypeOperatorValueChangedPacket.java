package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeOperatorLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeOperatorValueChangedPacket extends PacketCodec {

	@CodecField
	private String operatorValue;

    public LogicProgrammerValueTypeOperatorValueChangedPacket() {

    }

    public LogicProgrammerValueTypeOperatorValueChangedPacket(ValueTypeOperator.ValueOperator value) {
		try {
			this.operatorValue = ValueHelpers.serializeRaw(value);
		} catch (Exception e) {
			this.operatorValue = "";
		}
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
			if(element instanceof ValueTypeOperatorLPElement) {
				IOperator operator;
				try {
					operator = ValueHelpers.deserializeRaw(ValueTypes.OPERATOR, operatorValue).getRawValue();
				} catch (IllegalArgumentException e) {
					operator = null;
				}
				((ValueTypeOperatorLPElement) element).setSelectedOperator(operator);
                ((ContainerLogicProgrammerBase) player.openContainer).onDirty();
			}
		}
	}
	
}