package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerActivateOperatorPacket extends PacketCodec {

    @CodecField
    private String operatorName;

    public LogicProgrammerActivateOperatorPacket() {

    }

    public LogicProgrammerActivateOperatorPacket(String operatorName) {
        this.operatorName = operatorName;
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
		if(player.openContainer instanceof ContainerLogicProgrammer) {
			((ContainerLogicProgrammer) player.openContainer).setActiveOperator(Operators.REGISTRY.getOperator(operatorName), 0, 0);
		}
	}
	
}