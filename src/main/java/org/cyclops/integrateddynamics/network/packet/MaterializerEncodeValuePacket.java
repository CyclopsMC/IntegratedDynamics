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
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.blockentity.BlockEntityMaterializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.ValueEncodingHelpers;
import org.cyclops.integrateddynamics.inventory.container.ContainerMaterializer;

/**
 * Packet for requesting the encoded materialized value of a materializer, so that it can be copied to the clipboard.
 * @author rubensworks
 *
 */
public class MaterializerEncodeValuePacket extends PacketCodec {

    public static final Type<MaterializerEncodeValuePacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "materializer_encode_value"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MaterializerEncodeValuePacket> CODEC = getCodec(MaterializerEncodeValuePacket::new);

    @CodecField
    private boolean compressed;

    public MaterializerEncodeValuePacket() {
        super(ID);
    }

    public MaterializerEncodeValuePacket(boolean compressed) {
        super(ID);
        this.compressed = compressed;
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
        if (!(player.containerMenu instanceof ContainerMaterializer container)) {
            return;
        }

        IValue value = null;
        if (container.getTileSupplier().isPresent()) {
            BlockEntityMaterializer tile = container.getTileSupplier().get();
            IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(tile.getNetwork()).orElse(null);
            if (partNetwork != null) {
                IVariable variable = tile.getVariable(partNetwork);
                if (variable != null) {
                    try {
                        value = (IValue) variable.getType().materialize(variable.getValue());
                    } catch (EvaluationException e) {
                        player.sendSystemMessage(e.getErrorMessage());
                        return;
                    }
                }
            }
        }
        if (value == null) {
            player.sendSystemMessage(Component.translatable(L10NValues.VALUE_ENCODING_ERROR_NOTHINGTOENCODE));
            return;
        }

        CompoundTag tag;
        try {
            tag = ValueEncodingHelpers.encodeToTag(ValueDeseralizationContext.of(world), value,
                    GeneralConfig.valueEncodingMaxLength);
        } catch (ValueEncodingHelpers.ValueEncodingException e) {
            player.sendSystemMessage(e.getErrorMessage());
            return;
        }

        IntegratedDynamics._instance.getPacketHandler().sendToPlayer(
                new EncodedValueClipboardPacket(tag, compressed), player);
    }

}
