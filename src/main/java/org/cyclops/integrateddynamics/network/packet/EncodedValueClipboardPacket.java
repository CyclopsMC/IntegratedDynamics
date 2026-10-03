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
import org.cyclops.integrateddynamics.core.helper.ValueEncodingHelpers;

/**
 * Packet for placing an encoded value on the clipboard of a player.
 * @author rubensworks
 *
 */
public class EncodedValueClipboardPacket extends PacketCodec {

    public static final Type<EncodedValueClipboardPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "encoded_value_clipboard"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EncodedValueClipboardPacket> CODEC = getCodec(EncodedValueClipboardPacket::new);

    @CodecField
    private CompoundTag value;
    @CodecField
    private boolean compressed;

    public EncodedValueClipboardPacket() {
        super(ID);
    }

    public EncodedValueClipboardPacket(CompoundTag value, boolean compressed) {
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
            Minecraft.getInstance().keyboardHandler.setClipboard(ValueEncodingHelpers.format(value, compressed));
            // An overlay message would be hidden behind the open gui, so this goes to the chat
            player.displayClientMessage(Component.translatable(L10NValues.VALUE_ENCODING_COPIED), false);
        } catch (ValueEncodingHelpers.ValueEncodingException e) {
            player.displayClientMessage(e.getErrorMessage(), false);
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
