package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.blockentity.BlockEntityEnergyBattery;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.function.Supplier;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.*;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadNetwork {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkApplicableTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_CABLE.get());
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.BOOLEAN_APPLICABLE, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkApplicableFalse(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.BOOLEAN_APPLICABLE, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkElementCountInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ELEMENT_COUNT, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkElementCountValid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ELEMENT_COUNT, ValueTypeInteger.ValueInteger.of(2));

        helper.setBlock(POS.west(), RegistryEntries.BLOCK_CABLE.get());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.west()), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.west()), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkBatteryCountInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ENERGY_BATTERY_COUNT, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkBatteryCountValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        helper.setBlock(POS.west().west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ENERGY_BATTERY_COUNT, ValueTypeInteger.ValueInteger.of(2));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkEnergyStoredInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ENERGY_STORED, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkEnergyStoredValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        helper.setBlock(POS.west().west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        ((BlockEntityEnergyBattery) helper.getBlockEntity(POS.west())).setEnergyStored(100);
        ((BlockEntityEnergyBattery) helper.getBlockEntity(POS.west().west())).setEnergyStored(200);
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ENERGY_STORED, ValueTypeInteger.ValueInteger.of(300));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkEnergyMaxInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ENERGY_MAX, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkEnergyMaxValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        helper.setBlock(POS.west().west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        ((BlockEntityEnergyBattery) helper.getBlockEntity(POS.west())).setEnergyStored(100);
        ((BlockEntityEnergyBattery) helper.getBlockEntity(POS.west().west())).setEnergyStored(200);
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ENERGY_MAX, ValueTypeInteger.ValueInteger.of(2_000_000));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkEnergConsumptionRateInvalid(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ENERGY_CONSUMPTION_RATE, ValueTypeInteger.ValueInteger.of(0));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkEnergConsumptionRateValid(GameTestHelper helper) {
        GeneralConfig.energyConsumptionMultiplier = 1;

        Supplier<IAspectVariable> variableSupplier = testReadAspectSetup(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.INTEGER_ENERGY_CONSUMPTION_RATE);
        helper.succeedWhen(() -> {
            assertValueEqual(variableSupplier.get(), ValueTypeInteger.ValueInteger.of(2));
            GeneralConfig.energyConsumptionMultiplier = 0;
        });

        helper.setBlock(POS.west(), RegistryEntries.BLOCK_CABLE.get());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.west()), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.west()), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkValueInvalidNoNetwork(GameTestHelper helper) {
        testReadAspectThrows(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.ANY_VALUE);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkValueInvalidNoPart(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_ENERGY_BATTERY.get());
        testReadAspectThrows(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.ANY_VALUE);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkValueInvalidWrongSide(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_CABLE.get());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.west()), Direction.NORTH, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.west()), Direction.NORTH), Aspects.Write.Redstone.BOOLEAN, new ItemStack(RegistryEntries.ITEM_VARIABLE));
        testReadAspectThrows(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.ANY_VALUE);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkValueValid(GameTestHelper helper) {
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_CABLE.get());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.west()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.west()), Direction.EAST), Aspects.Write.Redstone.BOOLEAN, new ItemStack(RegistryEntries.ITEM_VARIABLE));
        testReadAspect(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.ANY_VALUE, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkGetVariableByIdInvalidId(GameTestHelper helper) {
        // Create network reader and read OPERATOR_GETVARIABLEBYID
        testReadAspectSetup(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.OPERATOR_GETVARIABLEBYID);
        ItemStack variableAspect1 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Network.OPERATOR_GETVARIABLEBYID);

        // Add cables
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_CABLE.get());
        helper.setBlock(POS.north().west(), RegistryEntries.BLOCK_CABLE.get());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_CABLE.get());

        // Add display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.north()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable store
        helper.setBlock(POS.north().north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north().north());

        // Insert OPERATOR_GETVARIABLEBYID in variable store
        variableStore.getInventory().setItem(0, variableAspect1);

        // Create a constant value 10, and place it in variable store
        ItemStack variableConstant = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(10));
        variableStore.getInventory().setItem(1, variableConstant);

        // Create a variable referring the constant variable id, and place it in variable store
        ItemStack variableConstantId = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(-1));
        variableStore.getInventory().setItem(2, variableConstantId);

        // Create variable card for OPERATOR_GETVARIABLEBYID operator on variable aspect
        ItemStack variableApplied = createVariableForOperator(helper.getLevel(), Operators.OPERATOR_APPLY, new int[]{
                getVariableFacade(helper.getLevel(), variableAspect1).getId(),
                getVariableFacade(helper.getLevel(), variableConstantId).getId()
        });

        // Place applied variable in display panel
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.north()), Direction.EAST), variableApplied);

        helper.succeedWhen(() -> {
            assertValueEqual(partAndState.getRight().getDisplayValue(), null);
            helper.assertValueEqual(partAndState.getRight().getGlobalErrors(), Lists.newArrayList(
                    Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(-1))
            ), "Display panel errors do not match");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadNetworkGetVariableByIdValid(GameTestHelper helper) {
        // Create network reader and read OPERATOR_GETVARIABLEBYID
        testReadAspectSetup(POS, helper, PartTypes.NETWORK_READER, Aspects.Read.Network.OPERATOR_GETVARIABLEBYID);
        ItemStack variableAspect1 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Network.OPERATOR_GETVARIABLEBYID);

        // Add cables
        helper.setBlock(POS.west(), RegistryEntries.BLOCK_CABLE.get());
        helper.setBlock(POS.north().west(), RegistryEntries.BLOCK_CABLE.get());
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_CABLE.get());

        // Add display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.north()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable store
        helper.setBlock(POS.north().north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north().north());

        // Insert OPERATOR_GETVARIABLEBYID in variable store
        variableStore.getInventory().setItem(0, variableAspect1);

        // Create a constant value 10, and place it in variable store
        ItemStack variableConstant = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(10));
        variableStore.getInventory().setItem(1, variableConstant);

        // Create a variable referring the constant variable id, and place it in variable store
        ItemStack variableConstantId = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(getVariableFacade(helper.getLevel(), variableConstant).getId()));
        variableStore.getInventory().setItem(2, variableConstantId);

        // Create variable card for OPERATOR_GETVARIABLEBYID operator on variable aspect
        ItemStack variableApplied = createVariableForOperator(helper.getLevel(), Operators.OPERATOR_APPLY, new int[]{
                getVariableFacade(helper.getLevel(), variableAspect1).getId(),
                getVariableFacade(helper.getLevel(), variableConstantId).getId()
        });

        // Place applied variable in display panel
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.north()), Direction.EAST), variableApplied);

        helper.succeedWhen(() -> {
            assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(10));
        });
    }

}
