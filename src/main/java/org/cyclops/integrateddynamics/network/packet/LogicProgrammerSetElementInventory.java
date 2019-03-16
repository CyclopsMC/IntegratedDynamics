package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeIngredientsLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeListLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for updating the element inventory inside the logic programmer.
 * @author rubensworks
 *
 */
public class LogicProgrammerSetElementInventory extends PacketCodec {

	@CodecField
	private String listValueType;
	@CodecField
	private int baseX;
	@CodecField
	private int baseY;

    public LogicProgrammerSetElementInventory() {

    }

    public LogicProgrammerSetElementInventory(IValueType listValueType, int baseX, int baseY) {
    	this.listValueType = listValueType.getTranslationKey();
		this.baseX = baseX;
		this.baseY = baseY;
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
			ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) player.openContainer;
			ILogicProgrammerElement element = container.getActiveElement();
			if (element instanceof ValueTypeListLPElement || element instanceof ValueTypeIngredientsLPElement) {
				IValueType valueType = ValueTypes.REGISTRY.getValueType(this.listValueType);
				if (valueType != null) {
					((ContainerLogicProgrammerBase) player.openContainer).setElementInventory(
							valueType.createLogicProgrammerElement(), baseX, baseY);
				} else {
					IntegratedDynamics.clog(Level.WARN,
							"Got an invalid LogicProgrammerSetElementInventory packet: " + this.listValueType);
				}
			}
		}
	}
	
}