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

/**
 * Packet for notifying label changes.
 * @author rubensworks
 *
 */
public class ActionLabelPacket extends PacketCodec {

	@CodecField
	private int variableId;
    @CodecField
    private String label; // If null, this action is assumed to be a removal.

    public ActionLabelPacket() {

    }

    public ActionLabelPacket(int variableId, String label) {
        this.variableId = variableId;
        this.label = label;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
        if(label == null) {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).removeUnsafe(variableId);
        } else {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).putUnsafe(variableId, label);
        }
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {
        if(label == null) {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).remove(variableId);
        } else {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(variableId, label);
        }
	}
	
}