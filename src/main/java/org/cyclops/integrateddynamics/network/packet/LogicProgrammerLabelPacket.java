package org.cyclops.integrateddynamics.network.packet;

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

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "logic_programmer_label");

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
