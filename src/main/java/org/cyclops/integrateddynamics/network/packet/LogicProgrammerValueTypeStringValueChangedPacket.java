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
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeStringLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeStringValueChangedPacket extends PacketCodec {

    public static final Type<LogicProgrammerValueTypeStringValueChangedPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_value_type_string_value_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerValueTypeStringValueChangedPacket> CODEC = getCodec(LogicProgrammerValueTypeStringValueChangedPacket::new);

    @CodecField
    private String value;

    public LogicProgrammerValueTypeStringValueChangedPacket() {
        super(ID);
    }

    public LogicProgrammerValueTypeStringValueChangedPacket(String value) {
        super(ID);
        this.value = value;
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
            ILogicProgrammerElement element = ((ContainerLogicProgrammerBase) player.containerMenu).getActiveElement();
            if(element instanceof ValueTypeStringLPElement) {
                ((ValueTypeStringLPElement) element).getInnerGuiElement().setInputString(value);
                ((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
            }
        }
    }

}
