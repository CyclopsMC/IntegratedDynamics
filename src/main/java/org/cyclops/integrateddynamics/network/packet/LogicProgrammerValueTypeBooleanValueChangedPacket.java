package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeBooleanLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeStringLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for changing a boolean LP value.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeBooleanValueChangedPacket extends PacketCodec {

	@CodecField
	private boolean checked;

    public LogicProgrammerValueTypeBooleanValueChangedPacket() {

    }

    public LogicProgrammerValueTypeBooleanValueChangedPacket(boolean checked) {
		this.checked = checked;
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
		if(player.openContainer instanceof ContainerLogicProgrammerBase) {
			ILogicProgrammerElement element = ((ContainerLogicProgrammerBase) player.openContainer).getActiveElement();
			if(element instanceof ValueTypeBooleanLPElement) {
				((ValueTypeBooleanLPElement) element).setInputBoolean(checked);
                ((ContainerLogicProgrammerBase) player.openContainer).onDirty();
			}
		}
	}
	
}