package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

/**
 * Packet for notifying onLabelPacket changes.
 * @author rubensworks
 *
 */
public class ActionLabelPacket extends PacketCodec {

    public static final Type<ActionLabelPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "action_label"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActionLabelPacket> CODEC = getCodec(ActionLabelPacket::new);

    @CodecField
    private int variableId;
    @CodecField
    private String label; // If null, this action is assumed to be a removal.

    public ActionLabelPacket() {
        super(TYPE);
    }

    public ActionLabelPacket(int variableId, String label) {
        super(TYPE);
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
        if(label == null) {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).remove(variableId);
        } else {
            LabelsWorldStorage.getInstance(IntegratedDynamics._instance).put(variableId, label);
        }
    }

}
