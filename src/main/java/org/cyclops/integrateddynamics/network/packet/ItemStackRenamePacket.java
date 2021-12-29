package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.inventory.container.ContainerLabeller;

/**
 * Packet for renaming a regular itemstack.
 * @author rubensworks
 *
 */
public class ItemStackRenamePacket extends PacketCodec {

    @CodecField
    private String name;

    public ItemStackRenamePacket() {

    }

    public ItemStackRenamePacket(String name) {
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
