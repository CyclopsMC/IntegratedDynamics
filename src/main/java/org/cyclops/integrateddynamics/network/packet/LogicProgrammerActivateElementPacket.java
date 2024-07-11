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
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerActivateElementPacket extends PacketCodec {

    public static final Type<LogicProgrammerActivateElementPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_activate_element"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerActivateElementPacket> CODEC = getCodec(LogicProgrammerActivateElementPacket::new);

    @CodecField
    private String typeId;
    @CodecField
    private String elementId;

    public LogicProgrammerActivateElementPacket() {
        super(ID);
    }

    public LogicProgrammerActivateElementPacket(ResourceLocation typeId, ResourceLocation elementId) {
        super(ID);
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
            ((ContainerLogicProgrammerBase) player.containerMenu).setActiveElementById(
                    ResourceLocation.parse(typeId), ResourceLocation.parse(elementId));
        }
    }

}
