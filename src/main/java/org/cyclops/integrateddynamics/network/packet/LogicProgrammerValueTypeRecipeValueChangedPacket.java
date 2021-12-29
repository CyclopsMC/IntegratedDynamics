package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeRecipeLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a to the server if a recipe string value has changed.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeRecipeValueChangedPacket extends PacketCodec {

    @CodecField
    private String value;
    @CodecField
    private int type;

    public LogicProgrammerValueTypeRecipeValueChangedPacket() {

    }

    public LogicProgrammerValueTypeRecipeValueChangedPacket(String value, Type type) {
        this.value = value;
        this.type = type.ordinal();
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
            if(element instanceof ValueTypeRecipeLPElement) {
                Type type = Type.values()[this.type];
                switch (type) {
                    case INPUT_FLUID:
                        ((ValueTypeRecipeLPElement) element).setInputFluidAmount(value);
                        break;
                    case INPUT_ENERGY:
                        ((ValueTypeRecipeLPElement) element).setInputEnergy(value);
                        break;
                    case OUTPUT_FLUID:
                        ((ValueTypeRecipeLPElement) element).setOutputFluidAmount(value);
                        break;
                    case OUTPUT_ENERGY:
                        ((ValueTypeRecipeLPElement) element).setOutputEnergy(value);
                        break;
                }
                ((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
            }
        }
    }

    public static enum Type {
        INPUT_FLUID,
        INPUT_ENERGY,
        OUTPUT_FLUID,
        OUTPUT_ENERGY,
    }

}
