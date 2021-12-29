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
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        if (labels != null) {
            for (Map.Entry<Integer, String> entry : labels.entrySet()) {
                LabelsWorldStorage.getInstance(IntegratedDynamics._instance).putUnsafe(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
