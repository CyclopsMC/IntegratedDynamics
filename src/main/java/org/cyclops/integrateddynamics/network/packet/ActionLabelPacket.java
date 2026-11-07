package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;

/**
 * Packet for notifying onLabelPacket changes.
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
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        if(label == null) {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).removeUnsafe(variableId);
        } else {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).putUnsafe(variableId, label);
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        // Only the variable in the player's open labeller may be (un)labelled.
        if (!(player.containerMenu instanceof ContainerLabeller containerLabeller) || !containerLabeller.hasVariable(variableId)) {
            return;
        }
        if (label != null && label.length() > LabelsWorldStorage.MAX_LABEL_LENGTH) {
            return;
        }
        if(label == null) {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).remove(variableId);
        } else {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(variableId, label);
        }
    }

}
