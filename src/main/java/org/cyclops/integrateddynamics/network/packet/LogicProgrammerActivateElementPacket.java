package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerActivateElementPacket extends PacketCodec {

    @CodecField
    private String typeId;
    @CodecField
    private String elementId;

    public LogicProgrammerActivateElementPacket() {

    }

    public LogicProgrammerActivateElementPacket(ResourceLocation typeId, ResourceLocation elementId) {
        this.typeId = typeId.toString();
        this.elementId = elementId.toString();
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
            ResourceLocation type = new ResourceLocation(typeId);
            if (type.getPath().isEmpty()) {
                ((ContainerLogicProgrammerBase) player.containerMenu).returnWriteItemToPlayer();
            }
            ((ContainerLogicProgrammerBase) player.containerMenu).setActiveElementById(
                    type, new ResourceLocation(elementId));
        }
    }

}
