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
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet to trigger variable labeling.
 * @author rubensworks
 *
 */
public class LogicProgrammerLabelPacket extends PacketCodec {

    public static final Type<LogicProgrammerLabelPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_label"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerLabelPacket> CODEC = getCodec(LogicProgrammerLabelPacket::new);

    @CodecField
    private String label;

    public LogicProgrammerLabelPacket() {
        super(ID);
    }

    public LogicProgrammerLabelPacket(String label) {
        super(ID);
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
