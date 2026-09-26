package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.client.Minecraft;
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
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.VariableClipboardHelpers;

/**
 * Packet for placing a serialized value on the clipboard of a player.
 * @author rubensworks
 *
 */
public class VariableClipboardCopyPacket extends PacketCodec {

    public static final Type<VariableClipboardCopyPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "variable_clipboard_copy"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VariableClipboardCopyPacket> CODEC = getCodec(VariableClipboardCopyPacket::new);

    @CodecField
    private CompoundTag value;
    @CodecField
    private boolean compressed;

    public VariableClipboardCopyPacket() {
        super(ID);
    }

    public VariableClipboardCopyPacket(CompoundTag value, boolean compressed) {
        super(ID);
        this.value = value;
        this.compressed = compressed;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        try {
            Minecraft.getInstance().keyboardHandler.setClipboard(VariableClipboardHelpers.format(value, compressed));
            // An overlay message would be hidden behind the open gui, so this goes to the chat
            player.displayClientMessage(Component.translatable(L10NValues.VARIABLE_CLIPBOARD_COPIED), false);
        } catch (VariableClipboardHelpers.VariableClipboardException e) {
            player.displayClientMessage(e.getErrorMessage(), false);
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
