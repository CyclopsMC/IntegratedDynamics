package org.cyclops.integrateddynamics.core.evaluate;

import com.google.common.base.Optional;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeNumber;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.build.OperatorBuilder;
import org.cyclops.integrateddynamics.core.evaluate.operator.IterativeFunction;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;

/**
 * Collection of operator builders.
 * @author rubensworks
 */
public class OperatorBuilders {

    // --------------- Logical builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LOGICAL = OperatorBuilder.forType(ValueTypes.BOOLEAN).appendKind("logical");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LOGICAL_1_PREFIX = LOGICAL.inputTypes(1, ValueTypes.BOOLEAN).renderPattern(IConfigRenderPattern.PREFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LOGICAL_2 = LOGICAL.inputTypes(2, ValueTypes.BOOLEAN).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- Value propagators ---------------
    public static final IOperatorValuePropagator<Integer, IValue> PROPAGATOR_INTEGER_VALUE = new IOperatorValuePropagator<Integer, IValue>() {
        @Override
        public IValue getOutput(Integer input) throws EvaluationException {
            return ValueTypeInteger.ValueInteger.of(input);
        }
    };
    public static final IOperatorValuePropagator<Boolean, IValue> PROPAGATOR_BOOLEAN_VALUE = new IOperatorValuePropagator<Boolean, IValue>() {
        @Override
        public IValue getOutput(Boolean input) throws EvaluationException {
            return ValueTypeBoolean.ValueBoolean.of(input);
        }
    };
    public static final IOperatorValuePropagator<Double, IValue> PROPAGATOR_DOUBLE_VALUE = new IOperatorValuePropagator<Double, IValue>() {
        @Override
        public IValue getOutput(Double input) throws EvaluationException {
            return ValueTypeDouble.ValueDouble.of(input);
        }
    };
    public static final IOperatorValuePropagator<String, IValue> PROPAGATOR_STRING_VALUE = new IOperatorValuePropagator<String, IValue>() {
        @Override
        public IValue getOutput(String input) throws EvaluationException {
            return ValueTypeString.ValueString.of(input);
        }
    };
    public static final IOperatorValuePropagator<ResourceLocation, ValueTypeString.ValueString> PROPAGATOR_RESOURCELOCATION_MODNAME = new IOperatorValuePropagator<ResourceLocation, ValueTypeString.ValueString>() {
        @Override
        public ValueTypeString.ValueString getOutput(ResourceLocation resourceLocation) throws EvaluationException {
            String modName;
            try {
                String modId = Helpers.getModId(resourceLocation.getResourceDomain());
                ModContainer mod = Loader.instance().getIndexedModList().get(modId);
                modName = mod == null ? "Minecraft" : mod.getName();
            } catch (NullPointerException e) {
                modName = "";
            }
            return ValueTypeString.ValueString.of(modName);
        }
    };

    // --------------- Arithmetic builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC = OperatorBuilder.forType(ValueTypes.CATEGORY_NUMBER).appendKind("arithmetic").conditionalOutputTypeDeriver(new OperatorBuilder.IConditionalOutputTypeDeriver() {
        @Override
        public IValueType getConditionalOutputType(OperatorBase operator, IVariable[] input) {
            IValueType[] original = ValueHelpers.from(input);
            IValueTypeNumber[] types = new IValueTypeNumber[original.length];
            for(int i = 0; i < original.length; i++) {
                types[i] = (IValueTypeNumber) original[i];
            }
            return ValueTypes.CATEGORY_NUMBER.getLowestType(types);
        }
    });
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC_2 = ARITHMETIC.inputTypes(2, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.INFIX);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ARITHMETIC_2_PREFIX = ARITHMETIC.inputTypes(2, ValueTypes.CATEGORY_NUMBER).renderPattern(IConfigRenderPattern.PREFIX_2);

    // --------------- Integer builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INTEGER = OperatorBuilder.forType(ValueTypes.INTEGER).appendKind("integer");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INTEGER_1_SUFFIX = INTEGER.inputTypes(1, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.SUFFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> INTEGER_2 = INTEGER.inputTypes(2, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- Relational builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> RELATIONAL = OperatorBuilder.forType(ValueTypes.BOOLEAN).appendKind("relational");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> RELATIONAL_2 = RELATIONAL.inputTypes(2, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- Binary builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> BINARY = OperatorBuilder.forType(ValueTypes.INTEGER).appendKind("binary");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> BINARY_1_PREFIX = BINARY.inputTypes(1, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.PREFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> BINARY_2 = BINARY.inputTypes(2, ValueTypes.INTEGER).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- String builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> STRING = OperatorBuilder.forType(ValueTypes.STRING).appendKind("string");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> STRING_1_PREFIX = STRING.inputTypes(1, ValueTypes.STRING).renderPattern(IConfigRenderPattern.PREFIX_1);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> STRING_2 = STRING.inputTypes(2, ValueTypes.STRING).renderPattern(IConfigRenderPattern.INFIX);

    // --------------- Double builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> DOUBLE = OperatorBuilder.forType(ValueTypes.DOUBLE).appendKind("double");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> DOUBLE_1_PREFIX = DOUBLE.inputTypes(1, ValueTypes.DOUBLE).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- Nullable builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NULLABLE = OperatorBuilder.forType(ValueTypes.CATEGORY_NULLABLE).appendKind("general");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> NULLABLE_1_PREFIX = NULLABLE.inputTypes(1, ValueTypes.CATEGORY_NULLABLE).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- List builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LIST = OperatorBuilder.forType(ValueTypes.LIST).appendKind("list");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> LIST_1_PREFIX = LIST.inputTypes(1, ValueTypes.LIST).renderPattern(IConfigRenderPattern.PREFIX_1);

    // --------------- Block builders ---------------
    public static final OperatorBuilder BLOCK = OperatorBuilder.forType(ValueTypes.OBJECT_BLOCK).appendKind("block");
    public static final OperatorBuilder BLOCK_1_SUFFIX_LONG = BLOCK.inputTypes(1, ValueTypes.OBJECT_BLOCK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Optional<SoundType>> BLOCK_SOUND = new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Optional<SoundType>>() {
        @Override
        public Optional<SoundType> getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
            ValueObjectTypeBlock.ValueBlock block = input.getValue(0);
            if(block.getRawValue().isPresent()) {
                return Optional.of(block.getRawValue().get().getBlock().getSoundType());
            }
            return Optional.absent();
        }
    };

    // --------------- ItemStack builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK = OperatorBuilder.forType(ValueTypes.OBJECT_ITEMSTACK).appendKind("itemstack");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_1_SUFFIX_LONG = ITEMSTACK.inputTypes(1, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ITEMSTACK_2 = ITEMSTACK.inputTypes(2, ValueTypes.OBJECT_ITEMSTACK).renderPattern(IConfigRenderPattern.INFIX);
    public static final IterativeFunction.PrePostBuilder<ItemStack, IValue> FUNCTION_ITEMSTACK = IterativeFunction.PrePostBuilder.begin()
            .appendPre(new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, ItemStack>() {
                @Override
                public ItemStack getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
                    ValueObjectTypeItemStack.ValueItemStack a = input.getValue(0);
                    return a.getRawValue().isPresent() ? a.getRawValue().get() : null;
                }
            });
    public static final IterativeFunction.PrePostBuilder<ItemStack, Integer> FUNCTION_ITEMSTACK_TO_INT =
            FUNCTION_ITEMSTACK.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<ItemStack, Boolean> FUNCTION_ITEMSTACK_TO_BOOLEAN =
            FUNCTION_ITEMSTACK.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    // --------------- Entity builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY = OperatorBuilder.forType(ValueTypes.OBJECT_ENTITY).appendKind("entity");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> ENTITY_1_SUFFIX_LONG = ENTITY.inputTypes(1, ValueTypes.OBJECT_ENTITY).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final IterativeFunction.PrePostBuilder<Entity, IValue> FUNCTION_ENTITY = IterativeFunction.PrePostBuilder.begin()
            .appendPre(new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, Entity>() {
                @Override
                public Entity getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
                    ValueObjectTypeEntity.ValueEntity a = input.getValue(0);
                    return a.getRawValue().isPresent() ? a.getRawValue().get() : null;
                }
            });
    public static final IterativeFunction.PrePostBuilder<Entity, Double> FUNCTION_ENTITY_TO_DOUBLE =
            FUNCTION_ENTITY.appendPost(PROPAGATOR_DOUBLE_VALUE);
    public static final IterativeFunction.PrePostBuilder<Entity, Boolean> FUNCTION_ENTITY_TO_BOOLEAN =
            FUNCTION_ENTITY.appendPost(PROPAGATOR_BOOLEAN_VALUE);

    // --------------- FluidStack builders ---------------
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> FLUIDSTACK = OperatorBuilder.forType(ValueTypes.OBJECT_FLUIDSTACK).appendKind("fluidstack");
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> FLUIDSTACK_1_SUFFIX_LONG = FLUIDSTACK.inputTypes(1, ValueTypes.OBJECT_FLUIDSTACK).renderPattern(IConfigRenderPattern.SUFFIX_1_LONG);
    public static final OperatorBuilder<OperatorBase.SafeVariablesGetter> FLUIDSTACK_2 = FLUIDSTACK.inputTypes(2, ValueTypes.OBJECT_FLUIDSTACK).renderPattern(IConfigRenderPattern.INFIX);
    public static final IterativeFunction.PrePostBuilder<FluidStack, IValue> FUNCTION_FLUIDSTACK = IterativeFunction.PrePostBuilder.begin()
            .appendPre(new IOperatorValuePropagator<OperatorBase.SafeVariablesGetter, FluidStack>() {
                @Override
                public FluidStack getOutput(OperatorBase.SafeVariablesGetter input) throws EvaluationException {
                    ValueObjectTypeFluidStack.ValueFluidStack a = input.getValue(0);
                    return a.getRawValue().isPresent() ? a.getRawValue().get() : null;
                }
            });
    public static final IterativeFunction.PrePostBuilder<FluidStack, Integer> FUNCTION_FLUIDSTACK_TO_INT =
            FUNCTION_FLUIDSTACK.appendPost(PROPAGATOR_INTEGER_VALUE);
    public static final IterativeFunction.PrePostBuilder<FluidStack, Boolean> FUNCTION_FLUIDSTACK_TO_BOOLEAN =
            FUNCTION_FLUIDSTACK.appendPost(PROPAGATOR_BOOLEAN_VALUE);

}
