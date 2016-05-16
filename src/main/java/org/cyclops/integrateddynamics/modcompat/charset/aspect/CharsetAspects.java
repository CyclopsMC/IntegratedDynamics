package org.cyclops.integrateddynamics.modcompat.charset.aspect;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectWriteActivator;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectWriteDeactivator;
import org.cyclops.integrateddynamics.modcompat.charset.CharsetPipesModCompat;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;
import pl.asie.charset.api.pipes.IPipe;

import java.util.Collections;

/**
 * Builders for charset aspects
 * @author rubensworks
 */
public class CharsetAspects {

    public static final class Read {

        public static final class Pipe {

            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IPipe> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IPipe>() {
                @Override
                public IPipe getOutput(Pair<PartTarget, IAspectProperties> input) {
                    DimPos pos = input.getLeft().getTarget().getPos();
                    return CableHelpers.getInterface(pos, IPipe.class);
                }
            };

            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IPipe>
                    BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "charsetpipe");
            public static final AspectBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, IPipe>
                    BUILDER_OBJECT_ITEMSTACK = AspectReadBuilders.BUILDER_OBJECT_ITEMSTACK.handle(PROP_GET, "charsetpipe");

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISAPPLICABLE =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IPipe, Boolean>() {
                        @Override
                        public Boolean getOutput(IPipe pipe) {
                            return pipe != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HASCONTENTS =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IPipe, Boolean>() {
                        @Override
                        public Boolean getOutput(IPipe pipe) {
                            if(pipe != null) {
                                return pipe.getTravellingStack(null) != null;
                            }
                            return false;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "hascontents").buildRead();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> ITEMSTACK_CONTENTS =
                    BUILDER_OBJECT_ITEMSTACK.handle(new IAspectValuePropagator<IPipe, ItemStack>() {
                        @Override
                        public ItemStack getOutput(IPipe pipe) {
                            if(pipe != null) {
                                return pipe.getTravellingStack(null);
                            }
                            return null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_ITEMSTACK, "contents").buildRead();

        }

    }

    public static final class Write {

        public static final class Pipe {

            public static <V extends IValue, T extends IValueType<V>, O> AspectBuilder<V, T, Pair<ShifterPart, O>> getShifter(AspectBuilder<V, T, Triple<PartTarget, IAspectProperties, O>> builder) {
                return builder.handle(new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, O>, Pair<ShifterPart, O>>() {
                    @Override
                    public Pair<ShifterPart, O> getOutput(Triple<PartTarget, IAspectProperties, O> input) throws EvaluationException {
                        PartPos center = input.getLeft().getCenter();
                        return Pair.of(((ShifterPart) TileHelpers.getCapability(center.getPos().getWorld(),
                                center.getPos().getBlockPos(), center.getSide(), CharsetPipesModCompat.SHIFTER)), input.getRight());
                    }
                });
            }

            protected static void notifyNeighbours(PartTarget target) {
                DimPos dimPos = target.getCenter().getPos();
                dimPos.getWorld().notifyNeighborsOfStateChange(dimPos.getBlockPos(), dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock());
            }

            public static final IAspectWriteActivator ACTIVATOR = new IAspectWriteActivator() {
                @Override
                public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onActivate(P partType, PartTarget target, S state) {
                    state.addVolatileCapability(CharsetPipesModCompat.SHIFTER, new ShifterPart(target.getCenter().getSide()));
                    notifyNeighbours(target);
                }
            };
            public static final IAspectWriteDeactivator DEACTIVATOR = new IAspectWriteDeactivator() {
                @Override
                public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onDeactivate(P partType, PartTarget target, S state) {
                    state.removeVolatileCapability(CharsetPipesModCompat.SHIFTER);
                    notifyNeighbours(target);
                }
            };

            public static final IAspectWrite<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> SHIFTER_BOOLEAN =
                    getShifter(AspectWriteBuilders.BUILDER_BOOLEAN.appendKind("charsetpipe")
                            .appendActivator(ACTIVATOR).appendDeactivator(DEACTIVATOR).appendKind("shifter"))
                            .handle(new IAspectValuePropagator<Pair<ShifterPart, ValueTypeBoolean.ValueBoolean>, Void>() {
                                @Override
                                public Void getOutput(Pair<ShifterPart, ValueTypeBoolean.ValueBoolean> input) throws EvaluationException {
                                    ShifterPart shifter = input.getLeft();
                                    shifter.setShifting(input.getRight().getRawValue());
                                    return null;
                                }
                            }).buildWrite();

            public static final IAspectWrite<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> SHIFTER_ITEMSTACK =
                    getShifter(AspectWriteBuilders.BUILDER_ITEMSTACK.appendKind("charsetpipe")
                            .appendActivator(ACTIVATOR).appendDeactivator(DEACTIVATOR).appendKind("shifter"))
                            .handle(new IAspectValuePropagator<Pair<ShifterPart, ValueObjectTypeItemStack.ValueItemStack>, Void>() {
                                @Override
                                public Void getOutput(Pair<ShifterPart, ValueObjectTypeItemStack.ValueItemStack> input) throws EvaluationException {
                                    ShifterPart shifter = input.getLeft();
                                    shifter.setFilterItem(Collections.singleton(input.getRight()));
                                    shifter.setShifting(true);
                                    return null;
                                }
                            }).buildWrite();

            public static final IAspectWrite<ValueTypeList.ValueList, ValueTypeList> SHIFTER_LISTITEMSTACK =
                    getShifter(AspectWriteBuilders.BUILDER_LIST.appendKind("charsetpipe")
                            .appendActivator(ACTIVATOR).appendDeactivator(DEACTIVATOR).appendKind("shifter"))
                            .handle(new IAspectValuePropagator<Pair<ShifterPart, ValueTypeList.ValueList>, Void>() {
                                @Override
                                public Void getOutput(Pair<ShifterPart, ValueTypeList.ValueList> input) throws EvaluationException {
                                    ShifterPart shifter = input.getLeft();
                                    if (input.getRight().getRawValue().getValueType() == ValueTypes.OBJECT_ITEMSTACK) {
                                        shifter.setFilterItem(input.getRight().getRawValue());
                                        shifter.setShifting(true);
                                    } else {
                                        throw new EvaluationException(new L10NHelpers.UnlocalizedString(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                                                new L10NHelpers.UnlocalizedString(ValueTypes.OBJECT_ITEMSTACK.getUnlocalizedName()),
                                                new L10NHelpers.UnlocalizedString(input.getRight().getRawValue().getValueType().getUnlocalizedName())).localize());
                                    }
                                    return null;
                                }
                            }).buildWrite();

            public static final IAspectWrite<ValueObjectTypeFluidStack.ValueFluidStack, ValueObjectTypeFluidStack> SHIFTER_FLUIDSTACK =
                    getShifter(AspectWriteBuilders.BUILDER_FLUIDSTACK.appendKind("charsetpipe")
                            .appendActivator(ACTIVATOR).appendDeactivator(DEACTIVATOR).appendKind("shifterfluid"))
                            .handle(new IAspectValuePropagator<Pair<ShifterPart, ValueObjectTypeFluidStack.ValueFluidStack>, Void>() {
                                @Override
                                public Void getOutput(Pair<ShifterPart, ValueObjectTypeFluidStack.ValueFluidStack> input) throws EvaluationException {
                                    ShifterPart shifter = input.getLeft();
                                    shifter.setFilterFluid(Collections.singleton(input.getRight()));
                                    shifter.setShifting(true);
                                    return null;
                                }
                            }).buildWrite();

            public static final IAspectWrite<ValueTypeList.ValueList, ValueTypeList> SHIFTER_LISTFLUIDSTACK =
                    getShifter(AspectWriteBuilders.BUILDER_LIST.appendKind("charsetpipe")
                            .appendActivator(ACTIVATOR).appendDeactivator(DEACTIVATOR).appendKind("shifterfluid"))
                            .handle(new IAspectValuePropagator<Pair<ShifterPart, ValueTypeList.ValueList>, Void>() {
                                @Override
                                public Void getOutput(Pair<ShifterPart, ValueTypeList.ValueList> input) throws EvaluationException {
                                    ShifterPart shifter = input.getLeft();
                                    if (input.getRight().getRawValue().getValueType() == ValueTypes.OBJECT_FLUIDSTACK) {
                                        shifter.setFilterFluid(input.getRight().getRawValue());
                                        shifter.setShifting(true);
                                    } else {
                                        throw new EvaluationException(new L10NHelpers.UnlocalizedString(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                                                new L10NHelpers.UnlocalizedString(ValueTypes.OBJECT_FLUIDSTACK.getUnlocalizedName()),
                                                new L10NHelpers.UnlocalizedString(input.getRight().getRawValue().getValueType().getUnlocalizedName())).localize());
                                    }
                                    return null;
                                }
                            }).buildWrite();

        }

        }

}
