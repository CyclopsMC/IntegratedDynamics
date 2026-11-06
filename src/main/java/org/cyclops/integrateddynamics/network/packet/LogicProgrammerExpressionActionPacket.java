package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.nbt.CompoundTag;
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
 * Packet for applying an expression edit action on an operator element in the logic programmer.
 * @author rubensworks
 */
public class LogicProgrammerExpressionActionPacket extends PacketCodec {

    public static final Type<LogicProgrammerExpressionActionPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_expression_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerExpressionActionPacket> CODEC = getCodec(LogicProgrammerExpressionActionPacket::new);

    @CodecField
    private CompoundTag action;

    public LogicProgrammerExpressionActionPacket() {
        super(ID);
    }

    public LogicProgrammerExpressionActionPacket(CompoundTag action) {
        super(ID);
        this.action = action;
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
        if(player.containerMenu instanceof ContainerLogicProgrammerBase container && action != null) {
            container.applyExpressionAction(action);
        }
    }

}
