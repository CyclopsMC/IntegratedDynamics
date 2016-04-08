package org.cyclops.integrateddynamics.part.aspect.write;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectWriteActivator;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.part.aspect.write.redstone.IWriteRedstoneComponent;
import org.cyclops.integrateddynamics.part.aspect.write.redstone.WriteRedstoneComponent;

import java.util.List;

/**
 * Collection of aspect write builders and value propagators.
 * @author rubensworks
 */
public class AspectWriteBuilders {

    // --------------- Value type builders ---------------
    public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Triple<PartTarget, IAspectProperties, ValueTypeBoolean.ValueBoolean>>
            BUILDER_BOOLEAN = getValue(AspectBuilder.forWriteType(ValueTypes.BOOLEAN));
    public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Triple<PartTarget, IAspectProperties, ValueTypeInteger.ValueInteger>>
            BUILDER_INTEGER = getValue(AspectBuilder.forWriteType(ValueTypes.INTEGER));
    public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, Triple<PartTarget, IAspectProperties, ValueTypeDouble.ValueDouble>>
            BUILDER_DOUBLE = getValue(AspectBuilder.forWriteType(ValueTypes.DOUBLE));
    public static final AspectBuilder<ValueTypeString.ValueString, ValueTypeString, Triple<PartTarget, IAspectProperties, ValueTypeString.ValueString>>
            BUILDER_STRING = getValue(AspectBuilder.forWriteType(ValueTypes.STRING));
    public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, Triple<PartTarget, IAspectProperties, ValueTypeList.ValueList>>
            BUILDER_LIST = getValue(AspectBuilder.forWriteType(ValueTypes.LIST));
    public static final AspectBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, Triple<PartTarget, IAspectProperties, ValueObjectTypeItemStack.ValueItemStack>>
            BUILDER_ITEMSTACK = getValue(AspectBuilder.forWriteType(ValueTypes.OBJECT_ITEMSTACK));

    // --------------- Value type propagators ---------------
    public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeBoolean.ValueBoolean>, Triple<PartTarget, IAspectProperties, Boolean>> PROP_GET_BOOLEAN = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeBoolean.ValueBoolean>, Triple<PartTarget, IAspectProperties, Boolean>>() {
        @Override
        public Triple<PartTarget, IAspectProperties, Boolean> getOutput(Triple<PartTarget, IAspectProperties, ValueTypeBoolean.ValueBoolean> input) throws EvaluationException {
            return Triple.of(input.getLeft(), input.getMiddle(), input.getRight().getRawValue());
        }
    };
    public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties,ValueTypeInteger.ValueInteger>, Triple<PartTarget, IAspectProperties, Integer>> PROP_GET_INTEGER = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeInteger.ValueInteger>, Triple<PartTarget, IAspectProperties, Integer>>() {
        @Override
        public Triple<PartTarget, IAspectProperties, Integer> getOutput(Triple<PartTarget, IAspectProperties, ValueTypeInteger.ValueInteger> input) throws EvaluationException {
            return Triple.of(input.getLeft(), input.getMiddle(), input.getRight().getRawValue());
        }
    };
    public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeDouble.ValueDouble>, Triple<PartTarget, IAspectProperties, Double>> PROP_GET_DOUBLE = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeDouble.ValueDouble>, Triple<PartTarget, IAspectProperties, Double>>() {
        @Override
        public Triple<PartTarget, IAspectProperties, Double> getOutput(Triple<PartTarget, IAspectProperties, ValueTypeDouble.ValueDouble> input) throws EvaluationException {
            return Triple.of(input.getLeft(), input.getMiddle(), input.getRight().getRawValue());
        }
    };
    public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeLong.ValueLong>, Triple<PartTarget, IAspectProperties, Long>> PROP_GET_LONG = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeLong.ValueLong>, Triple<PartTarget, IAspectProperties, Long>>() {
        @Override
        public Triple<PartTarget, IAspectProperties, Long> getOutput(Triple<PartTarget, IAspectProperties, ValueTypeLong.ValueLong> input) throws EvaluationException {
            return Triple.of(input.getLeft(), input.getMiddle(), input.getRight().getRawValue());
        }
    };
    public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueObjectTypeItemStack.ValueItemStack>, Triple<PartTarget, IAspectProperties, ItemStack>> PROP_GET_ITEMSTACK = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueObjectTypeItemStack.ValueItemStack>, Triple<PartTarget, IAspectProperties, ItemStack>>() {
        @Override
        public Triple<PartTarget, IAspectProperties, ItemStack> getOutput(Triple<PartTarget, IAspectProperties, ValueObjectTypeItemStack.ValueItemStack> input) throws EvaluationException {
            Optional<ItemStack> optional = input.getRight().getRawValue();
            return Triple.of(input.getLeft(), input.getMiddle(), optional.isPresent() ? optional.get() : null);
        }
    };
    public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeString.ValueString>, Triple<PartTarget, IAspectProperties, String>> PROP_GET_STRING = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueTypeString.ValueString>, Triple<PartTarget, IAspectProperties, String>>() {
        @Override
        public Triple<PartTarget, IAspectProperties, String> getOutput(Triple<PartTarget, IAspectProperties, ValueTypeString.ValueString> input) throws EvaluationException {
            return Triple.of(input.getLeft(), input.getMiddle(), input.getRight().getRawValue());
        }
    };
    public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueObjectTypeBlock.ValueBlock>, Triple<PartTarget, IAspectProperties, IBlockState>> PROP_GET_BLOCK = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueObjectTypeBlock.ValueBlock>, Triple<PartTarget, IAspectProperties, IBlockState>>() {
        @Override
        public Triple<PartTarget, IAspectProperties, IBlockState> getOutput(Triple<PartTarget, IAspectProperties, ValueObjectTypeBlock.ValueBlock> input) throws EvaluationException {
            Optional<IBlockState> optional = input.getRight().getRawValue();
            return Triple.of(input.getLeft(), input.getMiddle(), optional.isPresent() ? optional.get() : null);
        }
    };
    public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueObjectTypeFluidStack.ValueFluidStack>, Triple<PartTarget, IAspectProperties, FluidStack>> PROP_GET_FLUIDSTACK = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueObjectTypeFluidStack.ValueFluidStack>, Triple<PartTarget, IAspectProperties, FluidStack>>() {
        @Override
        public Triple<PartTarget, IAspectProperties, FluidStack> getOutput(Triple<PartTarget, IAspectProperties, ValueObjectTypeFluidStack.ValueFluidStack> input) throws EvaluationException {
            Optional<FluidStack> optional = input.getRight().getRawValue();
            return Triple.of(input.getLeft(), input.getMiddle(), optional.isPresent() ? optional.get() : null);
        }
    };

    public static final class Audio {

        public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_VOLUME =
                new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.volume.name");
        public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_FREQUENCY =
                new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.frequency.name");
        public static final IAspectProperties PROPERTIES_NOTE = new AspectProperties(Sets.<IAspectPropertyTypeInstance>newHashSet(
                PROP_VOLUME
        ));
        public static final IAspectProperties PROPERTIES_SOUND = new AspectProperties(Lists.<IAspectPropertyTypeInstance>newArrayList(
                PROP_VOLUME,
                PROP_FREQUENCY
        ));
        static {
            PROPERTIES_NOTE.setValue(PROP_VOLUME, ValueTypeDouble.ValueDouble.of(3D));
            PROPERTIES_SOUND.setValue(PROP_VOLUME, ValueTypeDouble.ValueDouble.of(3D));
            PROPERTIES_SOUND.setValue(PROP_FREQUENCY, ValueTypeDouble.ValueDouble.of(1D));
        }

        private static final List<String> INSTRUMENTS = Lists.newArrayList(new String[]{"harp", "bd", "snare", "hat", "bassattack"});
        private static String getInstrument(int id) {
            if (id < 0 || id >= INSTRUMENTS.size()) {
                id = 0;
            }
            return INSTRUMENTS.get(id);
        }

        public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Pair<NoteBlockEvent.Instrument, Integer>>, Void> PROP_SET = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Pair<NoteBlockEvent.Instrument, Integer>>, Void>() {
            @Override
            public Void getOutput(Triple<PartTarget, IAspectProperties, Pair<NoteBlockEvent.Instrument, Integer>> input) {
                IAspectProperties properties = input.getMiddle();
                BlockPos pos = input.getLeft().getTarget().getPos().getBlockPos();

                int eventID = input.getRight().getLeft().ordinal();
                int eventParam = input.getRight().getRight();
                if(eventParam >= 0 && eventParam <= 24) {
                    World world = input.getLeft().getTarget().getPos().getWorld();
                    NoteBlockEvent.Play e = new NoteBlockEvent.Play(world, pos, world.getBlockState(pos), eventParam, eventID);
                    if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(e)) {
                        float f = (float) Math.pow(2.0D, (double) (eventParam - 12) / 12.0D);
                        float volume = (float) properties.getValue(PROP_VOLUME).getRawValue();
                        IntegratedDynamics.proxy.sendSoundMinecraft(
                                (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                                "note." + getInstrument(eventID), SoundCategory.RECORDS, volume, f);
                    }
                }
                return null;
            }
        };

        public static IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Integer>, Triple<PartTarget, IAspectProperties, Pair<NoteBlockEvent.Instrument, Integer>>>
            propWithInstrument(final NoteBlockEvent.Instrument instrument) {
            return new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Integer>, Triple<PartTarget, IAspectProperties, Pair<NoteBlockEvent.Instrument, Integer>>>() {
                @Override
                public Triple<PartTarget, IAspectProperties, Pair<NoteBlockEvent.Instrument, Integer>> getOutput(Triple<PartTarget, IAspectProperties, Integer> input) throws EvaluationException {
                    return Triple.of(input.getLeft(), input.getMiddle(), Pair.of(instrument, input.getRight()));
                }
            };
        }

        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Triple<PartTarget, IAspectProperties, Integer>>
                BUILDER_INTEGER = AspectWriteBuilders.BUILDER_INTEGER.appendKind("audio").handle(PROP_GET_INTEGER);
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Triple<PartTarget, IAspectProperties, Integer>>
                BUILDER_INTEGER_INSTRUMENT = BUILDER_INTEGER.appendKind("instrument").withProperties(PROPERTIES_NOTE);
        public static final AspectBuilder<ValueTypeString.ValueString, ValueTypeString, Triple<PartTarget, IAspectProperties, String>>
                BUILDER_STRING = AspectWriteBuilders.BUILDER_STRING.appendKind("audio").handle(PROP_GET_STRING);

    }

    public static final class Effect {

        public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_OFFSET_X =
                new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.offsetX.name");
        public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_OFFSET_Y =
                new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.offsetY.name");
        public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_OFFSET_Z =
                new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.offsetZ.name");
        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_PARTICLES =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.particles.name");
        public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_SPREAD_X =
                new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.spreadX.name");
        public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_SPREAD_Y =
                new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.spreadY.name");
        public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_SPREAD_Z =
                new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.spreadZ.name");
        public static final IAspectPropertyTypeInstance<ValueTypeBoolean, ValueTypeBoolean.ValueBoolean> PROP_FORCE =
                new AspectPropertyTypeInstance<>(ValueTypes.BOOLEAN, "aspect.aspecttypes.integrateddynamics.boolean.forceParticle.name");
        public static final IAspectProperties PROPERTIES_PARTICLE = new AspectProperties(Lists.<IAspectPropertyTypeInstance>newArrayList(
                PROP_OFFSET_X,
                PROP_OFFSET_Y,
                PROP_OFFSET_Z,
                PROP_PARTICLES,
                PROP_SPREAD_X,
                PROP_SPREAD_Y,
                PROP_SPREAD_Z,
                PROP_FORCE
        ));

        static {
            PROPERTIES_PARTICLE.setValue(PROP_OFFSET_X, ValueTypeDouble.ValueDouble.of(0.5D));
            PROPERTIES_PARTICLE.setValue(PROP_OFFSET_Y, ValueTypeDouble.ValueDouble.of(0.5D));
            PROPERTIES_PARTICLE.setValue(PROP_OFFSET_Z, ValueTypeDouble.ValueDouble.of(0.5D));
            PROPERTIES_PARTICLE.setValue(PROP_PARTICLES, ValueTypeInteger.ValueInteger.of(1));
            PROPERTIES_PARTICLE.setValue(PROP_SPREAD_X, ValueTypeDouble.ValueDouble.of(0.0D));
            PROPERTIES_PARTICLE.setValue(PROP_SPREAD_Y, ValueTypeDouble.ValueDouble.of(0.0D));
            PROPERTIES_PARTICLE.setValue(PROP_SPREAD_Z, ValueTypeDouble.ValueDouble.of(0.0D));
            PROPERTIES_PARTICLE.setValue(PROP_FORCE, ValueTypeBoolean.ValueBoolean.of(false));
        }

        public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, Triple<PartTarget, IAspectProperties, Double>>
                BUILDER_DOUBLE = AspectWriteBuilders.BUILDER_DOUBLE.appendKind("effect").handle(PROP_GET_DOUBLE);
        public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, Triple<PartTarget, IAspectProperties, Double>>
                BUILDER_DOUBLE_PARTICLE = BUILDER_DOUBLE.withProperties(PROPERTIES_PARTICLE);

    }

    public static final class Redstone {

        private static final IWriteRedstoneComponent WRITE_REDSTONE_COMPONENT = new WriteRedstoneComponent();

        public static final IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Integer>, Void> PROP_SET = new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Integer>, Void>() {
            @Override
            public Void getOutput(Triple<PartTarget, IAspectProperties, Integer> input) {
                WRITE_REDSTONE_COMPONENT.setRedstoneLevel(input.getLeft(), input.getRight());
                return null;
            }
        };

        public static final IAspectWriteActivator ACTIVATOR = new IAspectWriteActivator() {
            @Override
            public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onActivate(P partType, PartTarget target, S state) {
                WRITE_REDSTONE_COMPONENT.deactivate(target);
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Triple<PartTarget, IAspectProperties, Boolean>>
                BUILDER_BOOLEAN = AspectWriteBuilders.BUILDER_BOOLEAN.appendKind("redstone").handle(PROP_GET_BOOLEAN).appendActivator(ACTIVATOR);
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Triple<PartTarget, IAspectProperties, Integer>>
                BUILDER_INTEGER = AspectWriteBuilders.BUILDER_INTEGER.appendKind("redstone").handle(PROP_GET_INTEGER).appendActivator(ACTIVATOR);

    }

    public static <V extends IValue, T extends IValueType<V>> AspectBuilder<V, T, Triple<PartTarget, IAspectProperties, V>> getValue(AspectBuilder<V, T, Triple<PartTarget, IAspectProperties, IVariable<V>>> builder) {
        return builder.handle(new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, IVariable<V>>, Triple<PartTarget, IAspectProperties, V>>() {
            @Override
            public Triple<PartTarget, IAspectProperties, V> getOutput(Triple<PartTarget, IAspectProperties, IVariable<V>> input) throws EvaluationException {
                return Triple.of(input.getLeft(), input.getMiddle(), input.getRight().getValue());
            }
        });
    }

}
