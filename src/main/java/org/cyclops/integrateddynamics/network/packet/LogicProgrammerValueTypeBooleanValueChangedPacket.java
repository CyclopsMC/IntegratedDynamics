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
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeBooleanLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for changing a boolean LP value.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeBooleanValueChangedPacket extends PacketCodec {

    public static final Type<LogicProgrammerValueTypeBooleanValueChangedPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_value_type_boolean_value_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerValueTypeBooleanValueChangedPacket> CODEC = getCodec(LogicProgrammerValueTypeBooleanValueChangedPacket::new);

    @CodecField
    private boolean checked;

    public LogicProgrammerValueTypeBooleanValueChangedPacket() {
        super(ID);
    }

    public LogicProgrammerValueTypeBooleanValueChangedPacket(boolean checked) {
        super(ID);
        this.checked = checked;
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
            if(element instanceof ValueTypeBooleanLPElement) {
                ((ValueTypeBooleanLPElement) element).getInnerGuiElement().setInputBoolean(checked);
                ((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
            }
        }
    }

}
