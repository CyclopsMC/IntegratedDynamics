package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.function.Supplier;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.assertValueEqual;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsRead {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadRedstoneBooleanLow(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_BLOCK);
        testReadAspect(helper, PartTypes.REDSTONE_READER, Aspects.Read.Redstone.BOOLEAN_LOW, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadRedstoneBooleanNonLow(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_BLOCK);
        testReadAspect(helper, PartTypes.REDSTONE_READER, Aspects.Read.Redstone.BOOLEAN_NONLOW, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadRedstoneBooleanHigh(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_BLOCK);
        testReadAspect(helper, PartTypes.REDSTONE_READER, Aspects.Read.Redstone.BOOLEAN_HIGH, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadRedstoneBooleanClock(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_BLOCK);
        Supplier<IAspectVariable> variableSupplier = testReadAspectSetup(helper, PartTypes.REDSTONE_READER, Aspects.Read.Redstone.BOOLEAN_CLOCK);
        helper.startSequence()
                .thenWaitUntil(() -> assertValueEqual(variableSupplier.get(), ValueTypeBoolean.ValueBoolean.of(false)))
                .thenWaitUntil(() -> assertValueEqual(variableSupplier.get(), ValueTypeBoolean.ValueBoolean.of(true)))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadRedstoneInteger(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(POS.west());
        chest.setItem(0, new ItemStack(Items.COAL));
        testReadAspect(helper, PartTypes.REDSTONE_READER, Aspects.Read.Redstone.INTEGER_COMPARATOR, ValueTypeInteger.ValueInteger.of(1));
    }

    public static Supplier<IAspectVariable> testReadAspectSetup(GameTestHelper helper, IPartType<?, ?> partType, IAspectRead<?, ?> aspectRead) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place part
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, partType, new ItemStack(partType.getItem()));
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
        PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
        IPartTypeReader partReader = (IPartTypeReader) partStateHolder.getPart();
        IPartStateReader partStateReader = (IPartStateReader) partStateHolder.getState();
        return () -> partReader.getVariable(PartTarget.fromCenter(partPos), partStateReader, aspectRead);
    }

    public static void testReadAspect(GameTestHelper helper, IPartType<?, ?> partType, IAspectRead<?, ?> aspectRead, IValue expectedValue) {
        Supplier<IAspectVariable> variableSupplier = testReadAspectSetup(helper, partType, aspectRead);
        helper.succeedWhen(() -> assertValueEqual(variableSupplier.get(), expectedValue));
    }

}
