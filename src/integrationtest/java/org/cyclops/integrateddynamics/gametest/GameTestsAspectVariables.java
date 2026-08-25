package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.part.PartStateAspectVariablesHandler;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;

import java.util.function.Supplier;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.assertValueEqual;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.createVariableForValue;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.createVariableFromReader;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.getAspectPropertyVariableError;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.getEffectiveAspectProperty;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.setAspectProperty;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.setAspectPropertyVariable;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspectSetup;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testWriteAspectSetup;

/**
 * Tests for aspect settings (aspect properties) that are driven by variables.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectVariables {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    protected static PartPos partPos(GameTestHelper helper, BlockPos pos) {
        return PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.WEST);
    }

    /**
     * Construct the aspect settings container for the given part and aspect,
     * in the same way as it is constructed when a player opens the aspect settings gui.
     */
    protected static ContainerAspectSettings openAspectSettings(GameTestHelper helper, PartPos partPos, IAspect<?, ?> aspect) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = aspect.getPropertiesContainerProvider(partPos).createMenu(1, player.getInventory(), player);
        if (!(menu instanceof ContainerAspectSettings container)) {
            throw new GameTestAssertException("The aspect settings container could not be created");
        }
        return container;
    }

    /**
     * Fill the chest west of the given position with a unique item in the first few slots.
     */
    protected static ChestBlockEntity prepareChest(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(pos.west());
        chest.setItem(0, new ItemStack(Items.COAL, 1));
        chest.setItem(1, new ItemStack(Items.APPLE, 2));
        chest.setItem(2, new ItemStack(Items.DIAMOND, 3));
        return chest;
    }

    // Without any aspect setting variable, the statically configured (default) property value must be used.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesAbsentUsesDefaultProperty(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        helper.succeedWhen(() -> assertValueEqual(variableSupplier.get(),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.COAL, 1))));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesAbsentUsesStaticProperty(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        setAspectProperty(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(1));
        helper.succeedWhen(() -> assertValueEqual(variableSupplier.get(),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 2))));
    }

    // A constant variable in the aspect setting slot must override the statically configured value.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesConstant(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2)));
        helper.succeedWhen(() -> assertValueEqual(variableSupplier.get(),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.DIAMOND, 3))));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesConstantOverridesStaticProperty(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        setAspectProperty(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(1));
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2)));
        helper.succeedWhen(() -> {
            assertValueEqual(variableSupplier.get(),
                    ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.DIAMOND, 3)));

            // The statically configured value must remain untouched
            IPartState<?> partState = PartHelpers.getPart(partPos(helper, POS)).getState();
            assertValueEqual(partState.getAspectProperties(Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT)
                    .getValue(AspectReadBuilders.Inventory.PROPERTY_SLOTID), ValueTypeInteger.ValueInteger.of(1));
        });
    }

    // Removing the variable again must fall back to the statically configured value.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesRemoval(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        setAspectProperty(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(1));
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2)));
        helper.startSequence()
                .thenWaitUntil(() -> assertValueEqual(variableSupplier.get(),
                        ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.DIAMOND, 3))))
                .thenExecute(() -> setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                        AspectReadBuilders.Inventory.PROPERTY_SLOTID, ItemStack.EMPTY))
                .thenWaitUntil(() -> assertValueEqual(variableSupplier.get(),
                        ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 2))))
                .thenSucceed();
    }

    // A variable that changes value over time must be picked up.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesDynamic(GameTestHelper helper) {
        prepareChest(helper, POS);

        // Place an inventory reader, and a second inventory reader that reads a chest that will drive the slot id
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        helper.setBlock(POS.north(), Blocks.CHEST);
        ChestBlockEntity driverChest = helper.getBlockEntity(POS.north());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.INVENTORY_READER,
                new ItemStack(PartTypes.INVENTORY_READER.getItem()));
        ItemStack variableSlotsFilled = createVariableFromReader(helper.getLevel(),
                PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH), Aspects.Read.Inventory.INTEGER_SLOTSFILLED);
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID, variableSlotsFilled);

        helper.startSequence()
                // The driver chest is empty, so slot 0 is read
                .thenWaitUntil(() -> assertValueEqual(variableSupplier.get(),
                        ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.COAL, 1))))
                // Fill 2 slots in the driver chest, so slot 2 must be read
                .thenExecute(() -> {
                    driverChest.setItem(0, new ItemStack(Items.STICK));
                    driverChest.setItem(1, new ItemStack(Items.STICK));
                    driverChest.setChanged();
                })
                .thenWaitUntil(() -> assertValueEqual(variableSupplier.get(),
                        ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.DIAMOND, 3))))
                .thenSucceed();
    }

    // Invalid variables must produce an error, and must fall back to the statically configured value.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesInvalidType(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        setAspectProperty(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(1));
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true)));
        helper.succeedWhen(() -> {
            helper.assertTrue(getAspectPropertyVariableError(partPos(helper, POS),
                    Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT, AspectReadBuilders.Inventory.PROPERTY_SLOTID) != null,
                    "Expected an error for an invalid variable type");
            assertValueEqual(variableSupplier.get(),
                    ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 2)));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesInvalidValue(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        setAspectProperty(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(1));
        // The slot id property only allows positive values
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(-1)));
        helper.succeedWhen(() -> {
            helper.assertTrue(getAspectPropertyVariableError(partPos(helper, POS),
                    Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT, AspectReadBuilders.Inventory.PROPERTY_SLOTID) != null,
                    "Expected an error for an invalid variable value");
            assertValueEqual(variableSupplier.get(),
                    ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.APPLE, 2)));
        });
    }

    // Aspect setting variables must also apply to writers.

    @GameTest(template = TEMPLATE_EMPTY, batch = "integrateddynamics:aspectvariablesredstonepulse")
    public void testAspectVariablesWriteRedstonePulseEmitValue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.EAST, PartTypes.REDSTONE_READER,
                new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        testWriteAspectSetup(POS, helper, PartTypes.REDSTONE_WRITER, Aspects.Write.Redstone.BOOLEAN_PULSE,
                createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.EAST),
                        Aspects.Read.Redstone.BOOLEAN_CLOCK));
        setAspectProperty(partPos(helper, POS), Aspects.Write.Redstone.BOOLEAN_PULSE,
                AspectWriteBuilders.Redstone.PROP_PULSE_LENGTH, ValueTypeInteger.ValueInteger.of(4));
        // Drive the emitted redstone level with a variable
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Write.Redstone.BOOLEAN_PULSE,
                AspectWriteBuilders.Redstone.PROP_PULSE_EMIT_VALUE,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(7)));
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 0))
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 7))
                .thenWaitUntil(() -> helper.assertBlockProperty(POS.west(), RedStoneWireBlock.POWER, 0))
                .thenSucceed();
    }

    // Variables that are provided by the network (instead of being constants) must be resolvable.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesFromVariableStore(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);

        // Place a variable store holding the slot id variable
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());
        ItemStack variableSlotId = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2));
        variableStore.getInventory().setItem(0, variableSlotId);

        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID, variableSlotId.copy());

        helper.succeedWhen(() -> assertValueEqual(variableSupplier.get(),
                ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.DIAMOND, 3))));
    }

    // The variables must survive a serialization round-trip of the part state.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesPersistence(GameTestHelper helper) {
        prepareChest(helper, POS);
        testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2)));

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos(helper, POS));
            IPartType partType = (IPartType) partStateHolder.getPart();
            ValueDeseralizationContext context = ValueDeseralizationContext.of(helper.getLevel());

            ItemStack partItem = partType.getItemStack(context, partStateHolder.getState(), true);
            IPartState restoredState = (IPartState) partType.getState(context, partItem);

            SimpleInventory inventory = PartStateAspectVariablesHandler.getVariablesInventory(restoredState,
                    Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
            helper.assertTrue(!inventory.getItem(0).isEmpty(), "The aspect setting variable was not persisted");
            helper.assertValueEqual(inventory.getItem(0).getItem(), RegistryEntries.ITEM_VARIABLE.get(),
                    "The persisted aspect setting variable is not a variable");
        });
    }

    // Aspect setting variables must be dropped when the part is broken.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesDropOnBreak(GameTestHelper helper) {
        helper.setBlock(POS.above(), RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.above()), Direction.WEST, PartTypes.INVENTORY_READER,
                new ItemStack(PartTypes.INVENTORY_READER.getItem()));
        PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(POS.above()), Direction.WEST);
        setAspectPropertyVariable(partPos, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2)));

        // Remove the part with a pickaxe
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove part!
        GameTestsOffsets.facePlayerToPart(player, partPos);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.getBlockState(POS.above()).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS.above()), player, true,
                helper.getLevel().getFluidState(helper.absolutePos(POS.above())));

        helper.succeedWhen(() -> {
            helper.assertItemEntityPresent(PartTypes.INVENTORY_READER.getItem());
            helper.assertItemEntityPresent(RegistryEntries.ITEM_VARIABLE.get());
        });
    }

    // The aspect settings gui must store variables into the part state, and only expose the active property's slot.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesViaContainer(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        PartPos partPos = partPos(helper, POS);

        ContainerAspectSettings container = openAspectSettings(helper, partPos, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);

        helper.assertValueEqual(container.getPropertyTypes().size(), 1, "Unexpected number of properties");
        helper.assertTrue(container.slots.get(0).isActive(), "The first property slot must be visible");
        helper.assertFalse(container.isPropertyVariableFilled(0), "The property slot must be empty initially");

        // Insert a variable into the visible variable slot
        ItemStack variable = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2));
        helper.assertTrue(container.slots.get(0).mayPlace(variable), "The active property slot must accept variables");
        container.slots.get(0).set(variable);
        container.saveVariablesInventory();

        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertTrue(container.isPropertyVariableFilled(0), "The property slot must be filled");
                    IPartState<?> partState = PartHelpers.getPart(partPos).getState();
                    helper.assertTrue(partState.getInventoryNamed(PartStateAspectVariablesHandler
                                    .getInventoryName(Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT)) != null,
                            "The variable was not stored in the part state");
                })
                .thenWaitUntil(() -> assertValueEqual(variableSupplier.get(),
                        ValueObjectTypeItemStack.ValueItemStack.of(new ItemStack(Items.DIAMOND, 3))))
                .thenExecute(() -> assertValueEqual(
                        getEffectiveAspectProperty(partPos, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                                AspectReadBuilders.Inventory.PROPERTY_SLOTID),
                        ValueTypeInteger.ValueInteger.of(2)))
                .thenSucceed();
    }

    // Only the variable slot of the active property may be interacted with.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesContainerSlotPaging(GameTestHelper helper) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_WRITER,
                new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        PartPos partPos = partPos(helper, POS);

        // The redstone pulse writer has multiple properties
        ContainerAspectSettings container = openAspectSettings(helper, partPos, Aspects.Write.Redstone.BOOLEAN_PULSE);

        int propertyCount = container.getPropertyTypes().size();
        helper.assertTrue(propertyCount > 1, "Expected multiple properties on the redstone pulse writer");

        helper.assertTrue(container.slots.get(0).isActive(), "The first property slot must be visible by default");
        for (int i = 1; i < propertyCount; i++) {
            helper.assertFalse(container.slots.get(i).isActive(), "Only the active property slot may be visible");
        }

        // Page to the last property, as the gui does when the player presses the next button
        container.clickMenuButton(helper.makeMockPlayer(GameType.SURVIVAL), propertyCount - 1);
        helper.assertFalse(container.slots.get(0).isActive(), "Only the active property slot may be visible");
        helper.assertTrue(container.slots.get(propertyCount - 1).isActive(), "The active property slot must be visible");

        ItemStack variable = createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(3));
        helper.assertFalse(container.slots.get(0).mayPlace(variable), "Inactive property slots may not accept variables");

        // Inserting into the active slot must be persisted under the corresponding property index
        container.slots.get(propertyCount - 1).set(variable);
        container.saveVariablesInventory();

        helper.succeedWhen(() -> {
            IPartState<?> partState = PartHelpers.getPart(partPos).getState();
            SimpleInventory inventory = PartStateAspectVariablesHandler.getVariablesInventory(partState,
                    Aspects.Write.Redstone.BOOLEAN_PULSE);
            helper.assertTrue(inventory.getItem(0).isEmpty(), "Unexpected variable in the first property slot");
            helper.assertTrue(!inventory.getItem(propertyCount - 1).isEmpty(),
                    "The variable was not stored under the active property");
        });
    }

    // Aspects without variables must keep working exactly as before.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesUnaffectedAspects(GameTestHelper helper) {
        prepareChest(helper, POS);
        Supplier<IAspectVariable> variableSupplier =
                testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.INTEGER_SLOTSFILLED);
        // Set a variable for a different aspect on the same part
        setAspectPropertyVariable(partPos(helper, POS), Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2)));
        helper.succeedWhen(() -> assertValueEqual(variableSupplier.get(), ValueTypeInteger.ValueInteger.of(3)));
    }

    // Sanity check on the effective property value accessor.

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectVariablesEffectiveProperty(GameTestHelper helper) {
        prepareChest(helper, POS);
        testReadAspectSetup(POS, helper, PartTypes.INVENTORY_READER, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT);
        PartPos partPos = partPos(helper, POS);
        setAspectProperty(partPos, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(1));
        setAspectPropertyVariable(partPos, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                AspectReadBuilders.Inventory.PROPERTY_SLOTID,
                createVariableForValue(helper.getLevel(), ValueTypes.INTEGER, ValueTypeInteger.ValueInteger.of(2)));
        helper.succeedWhen(() -> {
            assertValueEqual(getEffectiveAspectProperty(partPos, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                    AspectReadBuilders.Inventory.PROPERTY_SLOTID), ValueTypeInteger.ValueInteger.of(2));
            // Updating the statically configured value must not affect the variable-driven value
            setAspectProperty(partPos, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                    AspectReadBuilders.Inventory.PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(0));
            assertValueEqual(getEffectiveAspectProperty(partPos, Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT,
                    AspectReadBuilders.Inventory.PROPERTY_SLOTID), ValueTypeInteger.ValueInteger.of(2));
        });
    }

}
