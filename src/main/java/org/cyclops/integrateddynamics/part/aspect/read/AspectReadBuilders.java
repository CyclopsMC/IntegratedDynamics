package org.cyclops.integrateddynamics.part.aspect.read;

import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.part.aspect.build.AspectReadBuilder;
import org.cyclops.integrateddynamics.part.aspect.build.IAspectValuePropagator;

/**
 * Collection of aspect read builders and value propagators.
 * @author rubensworks
 */
public class AspectReadBuilders {

    // --------------- Value type builders ---------------
    private static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Pair<PartTarget, IAspectProperties>>
            BUILDER_BOOLEAN = AspectReadBuilder.forType(ValueTypes.BOOLEAN);
    private static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<PartTarget, IAspectProperties>>
            BUILDER_INTEGER = AspectReadBuilder.forType(ValueTypes.INTEGER);
    private static final AspectReadBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, Pair<PartTarget, IAspectProperties>>
            BUILDER_DOUBLE = AspectReadBuilder.forType(ValueTypes.DOUBLE);
    private static final AspectReadBuilder<ValueTypeLong.ValueLong, ValueTypeLong, Pair<PartTarget, IAspectProperties>>
            BUILDER_LONG = AspectReadBuilder.forType(ValueTypes.LONG);
    private static final AspectReadBuilder<ValueTypeString.ValueString, ValueTypeString, Pair<PartTarget, IAspectProperties>>
            BUILDER_STRING = AspectReadBuilder.forType(ValueTypes.STRING);
    private static final AspectReadBuilder<ValueTypeList.ValueList, ValueTypeList, Pair<PartTarget, IAspectProperties>>
            BUILDER_LIST = AspectReadBuilder.forType(ValueTypes.LIST);

    // --------------- Value type propagators ---------------
    public static final IAspectValuePropagator<Boolean, ValueTypeBoolean.ValueBoolean> PROP_GET_BOOLEAN = new IAspectValuePropagator<Boolean, ValueTypeBoolean.ValueBoolean>() {
        @Override
        public ValueTypeBoolean.ValueBoolean getOutput(Boolean input) {
            return ValueTypeBoolean.ValueBoolean.of(input);
        }
    };
    public static final IAspectValuePropagator<Integer, ValueTypeInteger.ValueInteger> PROP_GET_INTEGER = new IAspectValuePropagator<Integer, ValueTypeInteger.ValueInteger>() {
        @Override
        public ValueTypeInteger.ValueInteger getOutput(Integer input) {
            return ValueTypeInteger.ValueInteger.of(input);
        }
    };

    // --------------- Redstone ---------------
    public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer> PROP_GET_REDSTONE = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer>() {
        @Override
        public Integer getOutput(Pair<PartTarget, IAspectProperties> input) {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            return dimPos.getWorld().getRedstonePower(dimPos.getBlockPos(), input.getLeft().getCenter().getSide());
        }
    };
    public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer> PROP_GET_COMPARATOR = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer>() {
        @Override
        public Integer getOutput(Pair<PartTarget, IAspectProperties> input) {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            return dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock().getComparatorInputOverride(dimPos.getWorld(), dimPos.getBlockPos());
        }
    };

    public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Integer>
            BUILDER_BOOLEAN_REDSTONE = BUILDER_BOOLEAN.handle(PROP_GET_REDSTONE, "redstone");
    public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Integer>
            BUILDER_INTEGER_REDSTONE = BUILDER_INTEGER.handle(PROP_GET_REDSTONE, "redstone");
    public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Integer>
            BUILDER_INTEGER_COMPARATOR = BUILDER_INTEGER.handle(PROP_GET_COMPARATOR, "redstone");





}
