package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    	this.listValueType = listValueType.getUniqueName().toString();
		this.baseX = baseX;
		this.baseY = baseY;
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
			ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) player.containerMenu;
			ILogicProgrammerElement element = container.getActiveElement();
			if (element instanceof ValueTypeListLPElement || element instanceof ValueTypeIngredientsLPElement) {
				IValueType valueType = ValueTypes.REGISTRY.getValueType(new ResourceLocation(this.listValueType));
				if (valueType != null) {
					((ContainerLogicProgrammerBase) player.containerMenu).setElementInventory(
							valueType.createLogicProgrammerElement(), baseX, baseY);
				} else {
					IntegratedDynamics.clog(org.apache.logging.log4j.Level.WARN,
							"Got an invalid LogicProgrammerSetElementInventory packet: " + this.listValueType);
				}
			}
		}
	}
	
}