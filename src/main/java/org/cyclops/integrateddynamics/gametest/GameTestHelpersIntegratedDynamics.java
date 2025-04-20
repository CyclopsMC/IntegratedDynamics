package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeLPElementBase;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author rubensworks
 */
public class GameTestHelpersIntegratedDynamics {

    public static void assertValueEqual(IValue value1, IValue value2) {
        if (!Objects.equals(value1, value2)) {
            throw new GameTestAssertException(String.format("Value %s does not equal value %s", value1, value2));
        }
    }

    public static void assertValueEqual(@Nullable IVariable value1, IValue value2) {
        if (value1 == null) {
            throw new GameTestAssertException("Variable is null");
        }
        try {
            assertValueEqual(value1.getValue(), value2);
        } catch (EvaluationException e) {
            throw new GameTestAssertException(e.getMessage());
        }
    }

    public static ItemStack createVariableForValue(Level level, IValueType valueType, IValue value) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_VARIABLE);
        return registry.writeVariableFacadeItem(true, itemStack, ValueTypes.REGISTRY,
                new ValueTypeLPElementBase.ValueTypeVariableFacadeFactory(valueType, value), level, null, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get().defaultBlockState());
    }

    public static ItemStack createVariableForOperator(Level level, IOperator operator, int[] variableIds) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_VARIABLE);
        return registry.writeVariableFacadeItem(true, itemStack, Operators.REGISTRY,
                new OperatorLPElement.OperatorVariableFacadeFactory(operator, variableIds), level, null, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get().defaultBlockState());
    }

    public static IVariableFacade getVariableFacade(Level level, ItemStack itemStack) {
        return RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.of(level), itemStack);
    }

    public static ItemStack createVariableFromReader(Level level, PartPos partPos, final IAspect aspect) {
        return createVariableFromReader(level, aspect, PartHelpers.getPart(partPos).getState());
    }

    public static ItemStack createVariableFromReader(Level level, final IAspect aspect, IPartState partState) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_VARIABLE);
        return registry.writeVariableFacadeItem(true, itemStack, Aspects.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<>() {
            @Override
            public IAspectVariableFacade create(boolean generateId) {
                return new AspectVariableFacade(generateId, partState.getId(), aspect);
            }

            @Override
            public IAspectVariableFacade create(int id) {
                return new AspectVariableFacade(id, partState.getId(), aspect);
            }
        }, level, null, null);
    }

    public static void placeVariableInWriter(Level level, PartPos partPos, final IAspectWrite<?, ?> aspect, ItemStack variableAspect) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
        IPartTypeWriter<?, ?> part = (IPartTypeWriter<?, ?>) partStateHolder.getPart();
        IPartStateWriter<?> state = (IPartStateWriter<?>) partStateHolder.getState();

        // Find aspect index
        int aspectIndex = -1;
        for (int i = 0; i < part.getWriteAspects().size(); i++) {
            if (part.getWriteAspects().get(i) == aspect) {
                aspectIndex = i;
            }
        }
        if (aspectIndex < 0) {
            throw new GameTestAssertException("Aspect " + aspect + " not found");
        }

        // Insert variable
        state.getInventory().setItem(aspectIndex, variableAspect);

        // Activate aspect
        ((IPartTypeWriter) part).updateActivation(PartTarget.fromCenter(partPos), state, null);
    }

    public static Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> placeVariableInDisplayPanel(Level level, PartPos partPos, ItemStack variableAspect) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
        PartTypePanelDisplay part = (PartTypePanelDisplay) partStateHolder.getPart();
        PartTypePanelDisplay.State state = (PartTypePanelDisplay.State) partStateHolder.getState();

        // Insert variable
        state.getInventory().setItem(0, variableAspect);

        return Pair.of(part, state);
    }

    public static Supplier<IAspectVariable> testReadAspectSetup(BlockPos pos, GameTestHelper helper, IPartTypeReader<?, ?> partType, IAspectRead<?, ?> aspectRead) {
        // Place cable
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());

        // Place part
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.WEST, partType, new ItemStack(partType.getItem()));
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.WEST);
        PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
        IPartTypeReader partReader = (IPartTypeReader) partStateHolder.getPart();
        IPartStateReader partStateReader = (IPartStateReader) partStateHolder.getState();
        return () -> partReader.getVariable(PartTarget.fromCenter(partPos), partStateReader, aspectRead);
    }

    public static <V extends IValue> void testReadAspectThrows(BlockPos pos, GameTestHelper helper, IPartTypeReader<?, ?> partType, IAspectRead<V, ?> aspectRead) {
        Supplier<IAspectVariable> variableSupplier = testReadAspectSetup(pos, helper, partType, aspectRead);
        helper.succeedWhen(() -> {
            try {
                variableSupplier.get().getValue();
                helper.assertTrue(false, "The aspect did not throw");
            } catch (EvaluationException e) {
                helper.assertTrue(true, "The aspect did successfully throw");
            }
        });
    }

    public static <V extends IValue> void testReadAspect(BlockPos pos, GameTestHelper helper, IPartTypeReader<?, ?> partType, IAspectRead<V, ?> aspectRead, V expectedValue) {
        Supplier<IAspectVariable> variableSupplier = testReadAspectSetup(pos, helper, partType, aspectRead);
        helper.succeedWhen(() -> assertValueEqual(variableSupplier.get(), expectedValue));
    }

    public static <V extends IValue> void testReadAspectPredicate(BlockPos pos, GameTestHelper helper, IPartTypeReader<?, ?> partType, IAspectRead<V, ?> aspectRead, Predicate<V> asserter) {
        Supplier<IAspectVariable> variableSupplier = testReadAspectSetup(pos, helper, partType, aspectRead);
        helper.succeedWhen(() -> {
            try {
                helper.assertTrue(asserter.test((V) variableSupplier.get().getValue()), "Value was not asserted correctly");
            } catch (EvaluationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <V extends IValue> void testReadAspectOperator(BlockPos pos, GameTestHelper helper, IPartTypeReader<?, ?> partType, IAspectRead<V, ?> aspectRead, List<IValue> operatorInputs, IValue expectedOperatorOutput) {
        Supplier<IAspectVariable> variableSupplier = testReadAspectSetup(pos, helper, partType, aspectRead);
        helper.succeedWhen(() -> {
            try {
                IValue operatorValue = variableSupplier.get().getValue();
                if (!(operatorValue instanceof ValueTypeOperator.ValueOperator operator)) {
                    throw new GameTestAssertException("Return value is not an operator");
                }
                IValue output = operator.getRawValue().evaluate(operatorInputs.stream().map(Variable::new).toArray(Variable[]::new));
                assertValueEqual(output, expectedOperatorOutput);
            } catch (EvaluationException e) {
                throw new GameTestAssertException(e.getMessage());
            }
        });
    }

    public static <V extends IValue> void testReadAspectOperatorPredicate(BlockPos pos, GameTestHelper helper, IPartTypeReader<?, ?> partType, IAspectRead<V, ?> aspectRead, List<IValue> operatorInputs, Predicate<IValue> asserter) {
        Supplier<IAspectVariable> variableSupplier = testReadAspectSetup(pos, helper, partType, aspectRead);
        helper.succeedWhen(() -> {
            try {
                IValue operatorValue = variableSupplier.get().getValue();
                if (!(operatorValue instanceof ValueTypeOperator.ValueOperator operator)) {
                    throw new GameTestAssertException("Return value is not an operator");
                }
                IValue output = operator.getRawValue().evaluate(operatorInputs.stream().map(Variable::new).toArray(Variable[]::new));
                helper.assertTrue(asserter.test(output), "Value was not asserted correctly");
            } catch (EvaluationException e) {
                throw new GameTestAssertException(e.getMessage());
            }
        });
    }

    public static <V extends IValue> void testWriteAspectSetup(BlockPos pos, GameTestHelper helper, IPartTypeWriter<?, ?> partType, IAspectWrite<V, ?> aspectWrite, ItemStack variableAspect) {
        // Place cable
        if (helper.getLevel().isEmptyBlock(helper.absolutePos(pos))) {
            helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        }

        // Place part
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.WEST, partType, new ItemStack(partType.getItem()));
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.WEST);
        placeVariableInWriter(helper.getLevel(), partPos, aspectWrite, variableAspect);
    }

    public static <V extends IValue> void testWriteAspectSetup(BlockPos pos, GameTestHelper helper, IPartTypeWriter<?, ?> partType, IAspectWrite<V, ?> aspectWrite, V value) {
        testWriteAspectSetup(pos, helper, partType, aspectWrite, createVariableForValue(helper.getLevel(), value.getType(), value));
    }

}
