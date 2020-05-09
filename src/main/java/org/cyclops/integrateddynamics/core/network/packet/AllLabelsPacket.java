package org.cyclops.integrateddynamics.core.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

import java.util.Map;

/**
 * Packet for notifying onLabelPacket changes.
 * @author rubensworks
 *
 */
public class AllLabelsPacket extends PacketCodec {

	@CodecField
	private Map<Integer, String> labels;

    public AllLabelsPacket() {

    }

    public AllLabelsPacket(Map<Integer, String> labels) {
        this.labels = labels;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
        if (labels != null) {
            for (Map.Entry<Integer, String> entry : labels.entrySet()) {
                LabelsWorldStorage.getInstance(IntegratedDynamics._instance).putUnsafe(entry.getKey(), entry.getValue());
            }
        }
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}