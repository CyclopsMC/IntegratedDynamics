package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeOperatorLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeOperatorValueChangedPacket extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "logic_programmer_value_type_operator_value_changed");

    @CodecField
    private Tag operatorValue;

    public LogicProgrammerValueTypeOperatorValueChangedPacket() {
        super(ID);
    }

    public LogicProgrammerValueTypeOperatorValueChangedPacket(ValueTypeOperator.ValueOperator value) {
        super(ID);
        try {
            this.operatorValue = ValueHelpers.serializeRaw(value);
        } catch (Exception e) {
            this.operatorValue = ByteTag.valueOf((byte) 0);
        }
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
            if(element instanceof ValueTypeOperatorLPElement) {
                IOperator operator;
                try {
                    operator = ValueHelpers.deserializeRaw(ValueDeseralizationContext.of(world), ValueTypes.OPERATOR, operatorValue).getRawValue();
                } catch (IllegalArgumentException e) {
                    operator = null;
                }
                ((ValueTypeOperatorLPElement) element).setSelectedOperator(operator);
                ((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
            }
        }
    }

}
