package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
	public void actionClient(Level world, Player player) {
		
	}

	@Override
	public void actionServer(Level world, ServerPlayer player) {
		if(player.containerMenu instanceof ContainerLogicProgrammerBase) {
			((ContainerLogicProgrammerBase) player.containerMenu).onLabelPacket(label);
		}
	}
	
}