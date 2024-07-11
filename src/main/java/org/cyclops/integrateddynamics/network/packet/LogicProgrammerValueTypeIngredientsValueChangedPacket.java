package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.nbt.Tag;
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
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeIngredientsLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a button packet for a change in current ingredients value.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeIngredientsValueChangedPacket extends PacketCodec<LogicProgrammerValueTypeIngredientsValueChangedPacket> {

    public static final Type<LogicProgrammerValueTypeIngredientsValueChangedPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_value_type_ingredients_value_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerValueTypeIngredientsValueChangedPacket> CODEC = getCodec(LogicProgrammerValueTypeIngredientsValueChangedPacket::new);

    @CodecField
    private Tag value;

    public LogicProgrammerValueTypeIngredientsValueChangedPacket() {
        super(ID);
    }

    public LogicProgrammerValueTypeIngredientsValueChangedPacket(ValueDeseralizationContext valueDeseralizationContext, ValueObjectTypeIngredients.ValueIngredients value) {
        super(ID);
        this.value = value.getType().serialize(valueDeseralizationContext, value);
    }

    protected ValueObjectTypeIngredients.ValueIngredients getValue(Level level) {
        return ValueHelpers.deserializeRaw(ValueDeseralizationContext.of(level), ValueTypes.OBJECT_INGREDIENTS, value);
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
            if(element instanceof ValueTypeIngredientsLPElement) {
                ((ValueTypeIngredientsLPElement) element).setServerValue(getValue(world));
                ((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
            }
        }
    }

}
