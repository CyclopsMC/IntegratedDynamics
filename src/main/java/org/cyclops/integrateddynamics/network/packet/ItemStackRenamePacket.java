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
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;

/**
 * Packet for renaming a regular itemstack.
 * @author rubensworks
 *
 */
public class ItemStackRenamePacket extends PacketCodec {

    public static final Type<ItemStackRenamePacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "item_stack_rename"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackRenamePacket> CODEC = getCodec(ItemStackRenamePacket::new);

    @CodecField
    private String name;

    public ItemStackRenamePacket() {
        super(ID);
    }

    public ItemStackRenamePacket(String name) {
        super(ID);
        this.name = name;
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
        if(player.containerMenu instanceof ContainerLabeller) {
            ((ContainerLabeller) player.containerMenu).setItemStackName(this.name);
        }
    }

}
