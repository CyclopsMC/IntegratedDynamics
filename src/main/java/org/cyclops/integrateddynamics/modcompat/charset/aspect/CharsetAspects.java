package org.cyclops.integrateddynamics.modcompat.charset.aspect;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.OperatorBuilders;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectWriteActivator;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectWriteDeactivator;
import org.cyclops.integrateddynamics.modcompat.charset.CharsetPipesModCompat;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;
import pl.asie.charset.api.pipes.IPipeView;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Builders for charset aspects
 * @author rubensworks
 */
public class CharsetAspects {

    public static final class Read {

        public static final class Pipe {

            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IPipeView> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IPipeView>() {
                @Override
                public IPipeView getOutput(Pair<PartTarget, IAspectProperties> input) {
                    DimPos pos = input.getLeft().getTarget().getPos();
                    return TileHelpers.getCapability(pos, CharsetPipesModCompat.PIPE);
                }
            };

            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IPipeView>
                    BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "charsetpipe");
            public static final AspectBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, IPipeView>
                    BUILDER_OBJECT_ITEMSTACK = AspectReadBuilders.BUILDER_OBJECT_ITEMSTACK.handle(PROP_GET, "charsetpipe");
            public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, IPipeView>
                    BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET, "charsetpipe");

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISAPPLICABLE =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IPipeView, Boolean>() {
                        @Override
                        public Boolean getOutput(IPipeView pipe) {
                            return pipe != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HASCONTENTS =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IPipeView, Boolean>() {
                        @Override
                        public Boolean getOutput(IPipeView pipe) {
                            return pipe != null && !pipe.getTravellingStacks().isEmpty();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "hascontents").buildRead();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> ITEMSTACK_CONTENT =
                    BUILDER_OBJECT_ITEMSTACK.handle(new IAspectValuePropagator<IPipeView, ItemStack>() {
                        @Override
                        public ItemStack getOutput(IPipeView pipe) {
                            if(pipe != null) {
                                return Iterables.getFirst(pipe.getTravellingStacks(), ItemStack.EMPTY);
                            }
                            return ItemStack.EMPTY;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_ITEMSTACK, "content").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_CONTENTS =
                    BUILDER_LIST.handle(new IAspectValuePropagator<IPipeView, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(IPipeView pipe) {
                            if(pipe != null) {
                                return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Lists.newArrayList(Iterables.transform(pipe.getTravellingStacks(), new Function<ItemStack, ValueObjectTypeItemStack.ValueItemStack>() {
                                    @Nullable
                                    @Override
                                    public ValueObjectTypeItemStack.ValueItemStack apply(ItemStack input) {
                                        return ValueObjectTypeItemStack.ValueItemStack.of(input);
                                    }
                                })));
                            }
                            return ValueTypeList.ValueList.ofAll();
                        }
                    }).appendKind("contents").buildRead();

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
                dimPos.getWorld().notifyNeighborsOfStateChange(dimPos.getBlockPos(), dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock(), true);
            }

            public static final IAspectWriteActivator ACTIVATOR = new IAspectWriteActivator() {
                @Override
                public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void onActivate(P partType, PartTarget target, S state) {
                    state.addVolatileCapability(CharsetPipesModCompat.SHIFTER, new ShifterPart<>(target.getCenter().getSide(), partType, state));
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

            public static final IAspectWrite<ValueTypeOperator.ValueOperator, ValueTypeOperator> SHIFTER_PREDICATEITEMSTACK =
                    getShifter(AspectWriteBuilders.BUILDER_OPERATOR.appendKind("charsetpipe")
                            .appendActivator(ACTIVATOR).appendDeactivator(DEACTIVATOR).appendKind("shifter"))
                            .handle(new IAspectValuePropagator<Pair<ShifterPart, ValueTypeOperator.ValueOperator>, Void>() {
                                @Override
                                public Void getOutput(Pair<ShifterPart, ValueTypeOperator.ValueOperator> input) throws EvaluationException {
                                    ShifterPart shifter = input.getLeft();
                                    IOperator predicate = OperatorBuilders.getSafePredictate(input.getRight());
                                    if (predicate.getInputTypes().length == 1 && ValueHelpers.correspondsTo(predicate.getInputTypes()[0], ValueTypes.OBJECT_ITEMSTACK)) {
                                        shifter.setFilterItemPredicate(predicate);
                                        shifter.setShifting(true);
                                    } else {
                                        String current = ValueTypeOperator.getSignature(predicate);
                                        String expected = ValueTypeOperator.getSignature(new IValueType[]{ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.BOOLEAN);
                                        throw new EvaluationException(new L10NHelpers.UnlocalizedString(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                                                expected, current).localize());
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

            public static final IAspectWrite<ValueTypeOperator.ValueOperator, ValueTypeOperator> SHIFTER_PREDICATEFLUIDSTACK =
                    getShifter(AspectWriteBuilders.BUILDER_OPERATOR.appendKind("charsetpipe")
                            .appendActivator(ACTIVATOR).appendDeactivator(DEACTIVATOR).appendKind("shifterfluid"))
                            .handle(new IAspectValuePropagator<Pair<ShifterPart, ValueTypeOperator.ValueOperator>, Void>() {
                                @Override
                                public Void getOutput(Pair<ShifterPart, ValueTypeOperator.ValueOperator> input) throws EvaluationException {
                                    ShifterPart shifter = input.getLeft();
                                    IOperator predicate = OperatorBuilders.getSafePredictate(input.getRight());
                                    if (predicate.getInputTypes().length == 1 && ValueHelpers.correspondsTo(predicate.getInputTypes()[0], ValueTypes.OBJECT_FLUIDSTACK)) {
                                        shifter.setFilterFluidPredicate(predicate);
                                        shifter.setShifting(true);
                                    } else {
                                        String current = ValueTypeOperator.getSignature(predicate);
                                        String expected = ValueTypeOperator.getSignature(new IValueType[]{ValueTypes.OBJECT_FLUIDSTACK}, ValueTypes.BOOLEAN);
                                        throw new EvaluationException(new L10NHelpers.UnlocalizedString(L10NValues.ASPECT_ERROR_INVALIDTYPE,
                                                expected, current).localize());
                                    }
                                    return null;
                                }
                            }).buildWrite();

        }

        }

}
