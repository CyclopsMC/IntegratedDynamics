package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.VariableClipboardHelpers;
import org.cyclops.integrateddynamics.core.logicprogrammer.ClipboardLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a pasted value to the clipboard element of the logic programmer.
 *
 * The value is sent as a tag instead of a string,
 * as pasted values can easily exceed the maximum length of a string on the network.
 *
 * @author rubensworks
 *
 */
public class LogicProgrammerClipboardValueChangedPacket extends PacketCodec {

    public static final Type<LogicProgrammerClipboardValueChangedPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_clipboard_value_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerClipboardValueChangedPacket> CODEC = getCodec(LogicProgrammerClipboardValueChangedPacket::new);

    @CodecField
    private CompoundTag value;

    public LogicProgrammerClipboardValueChangedPacket() {
        super(ID);
    }

    public LogicProgrammerClipboardValueChangedPacket(CompoundTag value) {
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
        if (player.containerMenu instanceof ContainerLogicProgrammerBase container) {
            ILogicProgrammerElement element = container.getActiveElement();
            if (element instanceof ClipboardLPElement clipboardElement) {
                if (!GeneralConfig.variableClipboardPasteEnabled) {
                    clipboardElement.setError(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_ERROR_DISABLED));
                } else if (value == null || value.isEmpty()) {
                    clipboardElement.clear();
                } else {
                    try {
                        clipboardElement.setValue(VariableClipboardHelpers.deserialize(
                                ValueDeseralizationContext.of(world), value,
                                GeneralConfig.variableClipboardMaxPayloadLength));
                    } catch (VariableClipboardHelpers.VariableClipboardException e) {
                        clipboardElement.setError(e.getErrorMessage());
                    }
                }
                container.onDirty();
            }
        }
    }

}
