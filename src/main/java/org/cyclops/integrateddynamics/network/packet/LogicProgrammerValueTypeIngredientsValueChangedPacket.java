package org.cyclops.integrateddynamics.network.packet;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
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
import org.slf4j.Logger;

/**
 * Packet for sending a button packet for a change in current ingredients value.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeIngredientsValueChangedPacket extends PacketCodec<LogicProgrammerValueTypeIngredientsValueChangedPacket> {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<LogicProgrammerValueTypeIngredientsValueChangedPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_value_type_ingredients_value_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerValueTypeIngredientsValueChangedPacket> CODEC = getCodec(LogicProgrammerValueTypeIngredientsValueChangedPacket::new);

    @CodecField
    private CompoundTag value;

    public LogicProgrammerValueTypeIngredientsValueChangedPacket() {
        super(ID);
    }

    public LogicProgrammerValueTypeIngredientsValueChangedPacket(ValueDeseralizationContext valueDeseralizationContext, ValueObjectTypeIngredients.ValueIngredients value) {
        super(ID);
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new LogicProgrammerValueTypeListValueChangedPacket.PathElement(), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, valueDeseralizationContext.holderLookupProvider());
            ValueHelpers.serializeRaw(valueOutput, value);
            this.value = valueOutput.buildResult();
        }
    }

    protected ValueObjectTypeIngredients.ValueIngredients getValue(Level level) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new LogicProgrammerValueTypeListValueChangedPacket.PathElement(), LOGGER)) {
            ValueInput input = TagValueInput.create(
                    scopedCollector,
                    level.registryAccess(),
                    value
            );
            return ValueHelpers.deserializeRaw(input, ValueTypes.OBJECT_INGREDIENTS);
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
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
