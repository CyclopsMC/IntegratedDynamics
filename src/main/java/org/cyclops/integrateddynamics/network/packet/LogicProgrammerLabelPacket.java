package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet to trigger variable labeling.
 * @author rubensworks
 *
 */
public class LogicProgrammerLabelPacket extends PacketCodec {

    @CodecField
    private String label;

    public LogicProgrammerLabelPacket() {

    }

    public LogicProgrammerLabelPacket(String label) {
		this.label = label;
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
			((ContainerLogicProgrammerBase) player.containerMenu).onLabelPacket(label);
		}
	}
	
}