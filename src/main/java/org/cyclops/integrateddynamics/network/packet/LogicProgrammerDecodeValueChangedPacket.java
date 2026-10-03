package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.ValueEncodingHelpers;
import org.cyclops.integrateddynamics.core.logicprogrammer.DecodeLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a pasted encoded value to the decode element of the logic programmer.
 *
 * The value is sent as a tag instead of a string,
 * as encoded values can easily exceed the maximum length of a string on the network.
 *
 * @author rubensworks
 *
 */
public class LogicProgrammerDecodeValueChangedPacket extends PacketCodec {

    public static final Type<LogicProgrammerDecodeValueChangedPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_decode_value_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerDecodeValueChangedPacket> CODEC = getCodec(LogicProgrammerDecodeValueChangedPacket::new);

    @CodecField
    private CompoundTag value;

    public LogicProgrammerDecodeValueChangedPacket() {
        super(ID);
    }

    public LogicProgrammerDecodeValueChangedPacket(CompoundTag value) {
        super(ID);
        this.value = value;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        if (player.containerMenu instanceof ContainerLogicProgrammerBase container) {
            ILogicProgrammerElement element = container.getActiveElement();
            if (element instanceof DecodeLPElement decodeElement) {
                if (!GeneralConfig.valueDecodingEnabled) {
                    decodeElement.setError(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_DISABLED));
                } else if (value == null || value.isEmpty()) {
                    decodeElement.clear();
                } else {
                    try {
                        decodeElement.setValue(ValueEncodingHelpers.decode(
                                ValueDeseralizationContext.of(world), value,
                                GeneralConfig.valueEncodingMaxLength));
                    } catch (ValueEncodingHelpers.ValueEncodingException e) {
                        decodeElement.setError(e.getErrorMessage());
                    }
                }
                container.onDirty();
            }
        }
    }

}
