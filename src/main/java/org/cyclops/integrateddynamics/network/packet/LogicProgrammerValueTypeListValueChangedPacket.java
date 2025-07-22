package org.cyclops.integrateddynamics.network.packet;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
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
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeListLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.slf4j.Logger;

/**
 * Packet for sending a button packet for the exalted crafting.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeListValueChangedPacket extends PacketCodec {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Type<LogicProgrammerValueTypeListValueChangedPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_value_type_list_value_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerValueTypeListValueChangedPacket> CODEC = getCodec(LogicProgrammerValueTypeListValueChangedPacket::new);

    @CodecField
    private CompoundTag value;

    public LogicProgrammerValueTypeListValueChangedPacket() {
        super(ID);
    }

    public LogicProgrammerValueTypeListValueChangedPacket(ValueDeseralizationContext valueDeseralizationContext, ValueTypeList.ValueList value) {
        super(ID);
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new PathElement(), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, valueDeseralizationContext.holderLookupProvider());
            ValueHelpers.serializeRaw(valueOutput, value);
            this.value = valueOutput.buildResult();
        }
    }

    protected ValueTypeList.ValueList getListValue(Level level) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(new PathElement(), LOGGER)) {
            ValueInput input = TagValueInput.create(
                    scopedCollector,
                    level.registryAccess(),
                    value
            );
            return ValueHelpers.deserializeRaw(input, ValueTypes.LIST);
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
            if(element instanceof ValueTypeListLPElement) {
                ((ValueTypeListLPElement) element).setServerValue(getListValue(world));
                ((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
            }
        }
    }

    public static record PathElement() implements ProblemReporter.PathElement {
        @Override
        public String get() {
            return "packet";
        }
    }

}
