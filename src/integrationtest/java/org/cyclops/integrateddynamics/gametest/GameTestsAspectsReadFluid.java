package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.blockentity.BlockEntityDryingBasin;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspect;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadFluid {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidFullTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 1_000));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BOOLEAN_FULL, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidFullFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 10));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BOOLEAN_FULL, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidEmptyTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BOOLEAN_EMPTY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidEmptyFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 10));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BOOLEAN_EMPTY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidNonEmptyTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 10));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BOOLEAN_NONEMPTY, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidNonEmptyFalse(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BOOLEAN_NONEMPTY, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidApplicableTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BOOLEAN_APPLICABLE, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidApplicableFalse(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BOOLEAN_APPLICABLE, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidAmount(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 10));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.INTEGER_AMOUNT, ValueTypeInteger.ValueInteger.of(10));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidAmountTotal(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 10));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.INTEGER_AMOUNTTOTAL, ValueTypeInteger.ValueInteger.of(10));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidCapacityValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.INTEGER_CAPACITY, ValueTypeInteger.ValueInteger.of(1_000));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidCapacityInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.INTEGER_CAPACITY, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidCapacityTotalValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.INTEGER_CAPACITYTOTAL, ValueTypeInteger.ValueInteger.of(1_000));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidCapacityTotalInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.INTEGER_CAPACITYTOTAL, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidTanksValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.INTEGER_TANKS, ValueTypeInteger.ValueInteger.of(1));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidTanksInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.INTEGER_TANKS, ValueTypeInteger.ValueInteger.of(1));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidFillRatio(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 500));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.DOUBLE_FILLRATIO, ValueTypeDouble.ValueDouble.of(0.5D));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidTankFluids(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 500));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.LIST_TANKFLUIDS, ValueTypeList.ValueList.ofAll(
                ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(Fluids.WATER, 500))
        ));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidTankCapacities(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 500));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.LIST_TANKCAPACITIES, ValueTypeList.ValueList.ofAll(
                ValueTypeInteger.ValueInteger.of(1_000)
        ));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidFluidStack(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_DRYING_BASIN.get());
        BlockEntityDryingBasin dryingBasin = helper.getBlockEntity(POS.west());
        dryingBasin.getTank().setFluid(new FluidStack(Fluids.WATER, 500));
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.FLUIDSTACK, ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(Fluids.WATER, 500)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidBlockValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.WATER);
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BLOCK, ValueObjectTypeFluidStack.ValueFluidStack.of(new FluidStack(Fluids.WATER, 1_000)));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadFluidBlockInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.FLUID_READER, Aspects.Read.Fluid.BLOCK, ValueObjectTypeFluidStack.ValueFluidStack.of(FluidStack.EMPTY));
    }

}
