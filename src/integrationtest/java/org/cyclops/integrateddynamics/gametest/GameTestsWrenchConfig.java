package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartConfigHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.part.PartConfigApplyResult;
import org.cyclops.integrateddynamics.core.part.PartConfigSection;
import org.cyclops.integrateddynamics.core.part.PartConfigSnapshot;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.item.ItemWrench;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.createVariableForValue;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.getEffectiveAspectProperty;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.placeVariableInWriter;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.setAspectProperty;

/**
 * Tests for copying and pasting part configurations with the Wrench.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsWrenchConfig {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS_SOURCE = BlockPos.ZERO.offset(2, 0, 2);
    public static final BlockPos POS_TARGET = BlockPos.ZERO.offset(2, 0, 4);

    protected static PartPos placePart(GameTestHelper helper, BlockPos pos, IPartType<?, ?> partType) {
        helper.setBlock(pos, RegistryEntries.BLOCK_CABLE.value());
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(pos), Direction.WEST, partType,
                new ItemStack(partType.getItem()));
        return PartPos.of(helper.getLevel(), helper.absolutePos(pos), Direction.WEST);
    }

    protected static ItemWrench wrenchItem() {
        return (ItemWrench) RegistryEntries.ITEM_WRENCH.value();
    }

    protected static ItemStack createWrench(ItemWrench.Mode mode) {
        ItemStack wrench = new ItemStack(wrenchItem());
        wrenchItem().setMode(wrench, mode);
        return wrench;
    }

    /**
     * Click on the given part with the given item, in the same way as a player would.
     */
    protected static void clickPart(GameTestHelper helper, Player player, ItemStack itemStack, PartPos partPos,
                                    boolean sneaking) {
        player.setShiftKeyDown(sneaking);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        GameTestsOffsets.facePlayerToPart(player, partPos);
        BlockPos blockPos = partPos.getPos().getBlockPos();
        helper.getLevel().getBlockState(blockPos).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(blockPos.getCenter(), partPos.getSide(), blockPos, false));
    }

    protected static IPartType<?, ?> partType(PartPos partPos) {
        return PartHelpers.getPart(partPos).getPart();
    }

    protected static IPartState<?> partState(PartPos partPos) {
        return PartHelpers.getPart(partPos).getState();
    }

    /**
     * Give the given part a non-default configuration, so that copying it is observable.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static void configurePart(GameTestHelper helper, PartPos partPos, Vec3i offset) {
        IPartType partType = partType(partPos);
        IPartState state = partState(partPos);
        partType.setUpdateInterval(state, 40);
        state.setPriority(3);
        state.setChannel(2);
        partType.setTargetSideOverride(state, Direction.SOUTH);
        if (offset.compareTo(Vec3i.ZERO) != 0) {
            GameTestsOffsets.increaseMaxOffset(helper, partPos, 4);
            partType.setTargetOffset(state, partPos, offset);
        }
        setAspectProperty(partPos, Aspects.Write.Redstone.BOOLEAN,
                AspectWriteBuilders.Redstone.PROP_STRONG_POWER, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static PartConfigApplyResult applyConfig(GameTestHelper helper, PartPos partPos,
                                                       PartConfigSnapshot snapshot, Player player) {
        IPartType partType = partType(partPos);
        IPartState state = partState(partPos);
        INetwork network = NetworkHelpers.getNetwork(partPos).orElse(null);
        PartTarget target = partType.getTarget(partPos, state);
        return partType.applyConfig(ValueDeseralizationContext.of(helper.getLevel()), network,
                NetworkHelpers.getPartNetwork(network).orElse(null), target, state, snapshot,
                PartConfigSection.ALL, player);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static PartConfigSnapshot snapshotConfig(GameTestHelper helper, PartPos partPos) {
        IPartType partType = partType(partPos);
        return partType.snapshotConfig(ValueDeseralizationContext.of(helper.getLevel()),
                partState(partPos), PartConfigSection.ALL);
    }

    @Nullable
    protected static ItemStack getActiveVariable(PartPos partPos) {
        SimpleInventory inventory = ((PartStateActiveVariableBase<?>) partState(partPos)).getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                return inventory.getItem(slot);
            }
        }
        return null;
    }

    protected static int getVariableId(GameTestHelper helper, ItemStack itemStack) {
        return RegistryEntries.ITEM_VARIABLE.get()
                .getVariableFacade(ValueDeseralizationContext.of(helper.getLevel()), itemStack).getId();
    }

    protected static int countBlankVariables(Player player) {
        return PartConfigHelpers.countBlankVariables(player);
    }

    protected static void giveBlankVariables(Player player, int count) {
        player.getInventory().add(new ItemStack(RegistryEntries.ITEM_VARIABLE.value(), count));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigCopyPasteSamePartType(GameTestHelper helper) {
        PartPos source = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);
        PartPos target = placePart(helper, POS_TARGET, PartTypes.REDSTONE_WRITER);
        configurePart(helper, source, new Vec3i(1, 0, 0));
        GameTestsOffsets.increaseMaxOffset(helper, target, 4);
        placeVariableInWriter(helper, source, Aspects.Write.Redstone.BOOLEAN,
                createVariableForValue(helper.getLevel(), ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true)));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrench = createWrench(ItemWrench.Mode.CONFIG);
        // The wrench occupies the selected hotbar slot, so it must be given before the blank variable cards
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        giveBlankVariables(player, 1);
        clickPart(helper, player, wrench, source, true);
        clickPart(helper, player, wrench, target, false);

        helper.succeedWhen(() -> {
            IPartType partType = partType(target);
            IPartState state = partState(target);
            helper.assertValueEqual(partType.getUpdateInterval(state), 40, "Update interval was not pasted");
            helper.assertValueEqual(partType.getPriority(state), 3, "Priority was not pasted");
            helper.assertValueEqual(partType.getChannel(state), 2, "Channel was not pasted");
            helper.assertValueEqual(partType.getTargetSideOverride(state), Direction.SOUTH, "Target side was not pasted");
            helper.assertValueEqual(partType.getTargetOffset(state), new Vec3i(1, 0, 0), "Target offset was not pasted");
            helper.assertValueEqual(
                    getEffectiveAspectProperty(target, Aspects.Write.Redstone.BOOLEAN,
                            AspectWriteBuilders.Redstone.PROP_STRONG_POWER),
                    ValueTypeBoolean.ValueBoolean.of(true), "Aspect property was not pasted");
            helper.assertTrue(getActiveVariable(target) != null, "Variable card was not pasted");
            helper.assertValueEqual(countBlankVariables(player), 0, "Blank variable card was not consumed");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigPasteOtherPartType(GameTestHelper helper) {
        PartPos source = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);
        PartPos target = placePart(helper, POS_TARGET, PartTypes.REDSTONE_READER);
        configurePart(helper, source, Vec3i.ZERO);

        int updateInterval = ((IPartType) partType(target)).getUpdateInterval(partState(target));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrench = createWrench(ItemWrench.Mode.CONFIG);
        clickPart(helper, player, wrench, source, true);
        clickPart(helper, player, wrench, target, false);

        helper.succeedWhen(() -> {
            IPartType partType = partType(target);
            IPartState state = partState(target);
            helper.assertValueEqual(partType.getUpdateInterval(state), updateInterval, "Update interval was pasted");
            helper.assertValueEqual(partType.getPriority(state), 0, "Priority was pasted");
            helper.assertValueEqual(partType.getChannel(state), 0, "Channel was pasted");
            helper.assertTrue(partType.getTargetSideOverride(state) == null, "Target side was pasted");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigPasteCardsGetNewIdAndEjectExisting(GameTestHelper helper) {
        PartPos source = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);
        PartPos target = placePart(helper, POS_TARGET, PartTypes.REDSTONE_WRITER);
        ItemStack sourceVariable = createVariableForValue(helper.getLevel(), ValueTypes.BOOLEAN,
                ValueTypeBoolean.ValueBoolean.of(true));
        ItemStack targetVariable = createVariableForValue(helper.getLevel(), ValueTypes.BOOLEAN,
                ValueTypeBoolean.ValueBoolean.of(false));
        placeVariableInWriter(helper, source, Aspects.Write.Redstone.BOOLEAN, sourceVariable);
        placeVariableInWriter(helper, target, Aspects.Write.Redstone.BOOLEAN, targetVariable);
        int sourceId = getVariableId(helper, sourceVariable);
        int ejectedId = getVariableId(helper, targetVariable);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrench = createWrench(ItemWrench.Mode.CONFIG);
        // The wrench occupies the selected hotbar slot, so it must be given before the blank variable cards
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        giveBlankVariables(player, 3);
        clickPart(helper, player, wrench, source, true);
        clickPart(helper, player, wrench, target, false);

        helper.succeedWhen(() -> {
            ItemStack pasted = getActiveVariable(target);
            helper.assertTrue(pasted != null, "Variable card was not pasted");
            helper.assertTrue(getVariableId(helper, pasted) != sourceId,
                    "Pasted variable card has the same id as the copied one");
            helper.assertValueEqual(countBlankVariables(player), 2, "Wrong number of blank variable cards consumed");
            helper.assertTrue(hasVariableWithId(helper, player, ejectedId),
                    "The variable card that was in the target part was not given back to the player");
        });
    }

    protected static boolean hasVariableWithId(GameTestHelper helper, Player player, int id) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (itemStack.is(RegistryEntries.ITEM_VARIABLE.value())
                    && itemStack.has(RegistryEntries.DATACOMPONENT_VARIABLE_FACADE.get())
                    && getVariableId(helper, itemStack) == id) {
                return true;
            }
        }
        return false;
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigPasteWithoutBlanks(GameTestHelper helper) {
        PartPos source = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);
        PartPos target = placePart(helper, POS_TARGET, PartTypes.REDSTONE_WRITER);
        configurePart(helper, source, Vec3i.ZERO);
        ItemStack sourceVariable = createVariableForValue(helper.getLevel(), ValueTypes.BOOLEAN,
                ValueTypeBoolean.ValueBoolean.of(true));
        placeVariableInWriter(helper, source, Aspects.Write.Redstone.BOOLEAN, sourceVariable);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        PartConfigApplyResult result = applyConfig(helper, target, snapshotConfig(helper, source), player);

        helper.succeedWhen(() -> {
            helper.assertValueEqual(result.getMissingBlanks(), 1, "Wrong number of missing blank variable cards");
            helper.assertValueEqual(result.getCardsPasted(), 0, "Variable cards were pasted");
            helper.assertTrue(getActiveVariable(target) == null, "The target part received a variable card");
            helper.assertTrue(result.isPartSettingsApplied(), "The part settings were not applied");
            helper.assertValueEqual(((IPartType) partType(target)).getUpdateInterval(partState(target)), 40,
                    "Update interval was not pasted");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigPasteCreativeWithoutBlanks(GameTestHelper helper) {
        PartPos source = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);
        PartPos target = placePart(helper, POS_TARGET, PartTypes.REDSTONE_WRITER);
        placeVariableInWriter(helper, source, Aspects.Write.Redstone.BOOLEAN,
                createVariableForValue(helper.getLevel(), ValueTypes.BOOLEAN, ValueTypeBoolean.ValueBoolean.of(true)));

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        PartConfigApplyResult result = applyConfig(helper, target, snapshotConfig(helper, source), player);

        helper.succeedWhen(() -> {
            helper.assertValueEqual(result.getCardsPasted(), 1, "The variable card was not pasted");
            helper.assertValueEqual(result.getMissingBlanks(), 0, "Blank variable cards were reported as missing");
            helper.assertValueEqual(countBlankVariables(player), 0, "Blank variable cards were consumed");
            helper.assertTrue(getActiveVariable(target) != null, "The target part did not receive a variable card");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigPasteOffsetOutOfRange(GameTestHelper helper) {
        PartPos source = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);
        PartPos target = placePart(helper, POS_TARGET, PartTypes.REDSTONE_WRITER);
        // The target part has no offset enhancements, so the copied offset can not be applied
        configurePart(helper, source, new Vec3i(3, 0, 0));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        PartConfigApplyResult result = applyConfig(helper, target, snapshotConfig(helper, source), player);

        helper.succeedWhen(() -> {
            helper.assertTrue(result.isOffsetFailed(), "The offset failure was not reported");
            helper.assertValueEqual(((IPartType) partType(target)).getTargetOffset(partState(target)), Vec3i.ZERO,
                    "The offset was changed");
            helper.assertValueEqual(((IPartType) partType(target)).getUpdateInterval(partState(target)), 40,
                    "Update interval was not pasted");
            helper.assertValueEqual(((IPartType) partType(target)).getTargetSideOverride(partState(target)),
                    Direction.SOUTH, "Target side was not pasted");
        });
    }

    /**
     * The aspect settings container, with all value syncing recorded in-memory,
     * as there is no client to sync to in game tests.
     */
    public static class RecordingContainerAspectSettings extends ContainerAspectSettings {

        private final Map<Integer, CompoundTag> recordedValues = Maps.newHashMap();

        public RecordingContainerAspectSettings(int id, Inventory playerInventory, Container inventory,
                                                Optional<PartTarget> target, Optional<IPartContainer> partContainer,
                                                Optional<IPartType> partType, IAspect<?, ?> aspect) {
            super(id, playerInventory, inventory, target, partContainer, partType, aspect);
        }

        @Override
        public void setValue(int id, CompoundTag value) {
            this.recordedValues.put(id, value);
        }

        @Override
        public CompoundTag getValue(int id) {
            return this.recordedValues.get(id);
        }
    }

    /**
     * Construct the aspect settings container for the given part and aspect,
     * in the same way as it is constructed when the given player opens the aspect settings gui.
     */
    protected static ContainerAspectSettings openAspectSettings(Player player, PartPos partPos, IAspect<?, ?> aspect) {
        Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(partPos);
        return new RecordingContainerAspectSettings(1, player.getInventory(), new SimpleContainer(0),
                Optional.of(data.getRight()), Optional.of(data.getLeft()), Optional.of(data.getMiddle()), aspect);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectSettingsConfigCopyPasteAcrossAspects(GameTestHelper helper) {
        PartPos partPos = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);
        // The pulse aspect has a strong power setting that the plain aspect also has,
        // and a pulse length setting that it does not have.
        setAspectProperty(partPos, Aspects.Write.Redstone.BOOLEAN_PULSE,
                AspectWriteBuilders.Redstone.PROP_STRONG_POWER, ValueTypeBoolean.ValueBoolean.of(true));
        setAspectProperty(partPos, Aspects.Write.Redstone.BOOLEAN_PULSE,
                AspectWriteBuilders.Redstone.PROP_PULSE_LENGTH, ValueTypeInteger.ValueInteger.of(5));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, createWrench(ItemWrench.Mode.CONFIG));
        openAspectSettings(player, partPos, Aspects.Write.Redstone.BOOLEAN_PULSE)
                .onButtonClick(ContainerAspectSettings.BUTTON_CONFIG_COPY);
        openAspectSettings(player, partPos, Aspects.Write.Redstone.BOOLEAN)
                .onButtonClick(ContainerAspectSettings.BUTTON_CONFIG_PASTE);

        helper.succeedWhen(() -> {
            helper.assertValueEqual(
                    getEffectiveAspectProperty(partPos, Aspects.Write.Redstone.BOOLEAN,
                            AspectWriteBuilders.Redstone.PROP_STRONG_POWER),
                    ValueTypeBoolean.ValueBoolean.of(true), "The shared setting was not pasted");
            helper.assertValueEqual(
                    getEffectiveAspectProperty(partPos, Aspects.Write.Redstone.BOOLEAN_PULSE,
                            AspectWriteBuilders.Redstone.PROP_PULSE_LENGTH),
                    ValueTypeInteger.ValueInteger.of(5), "The copied aspect was changed");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigSneakDoesNotRemovePart(GameTestHelper helper) {
        PartPos partPos = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        clickPart(helper, player, createWrench(ItemWrench.Mode.CONFIG), partPos, true);

        helper.succeedWhen(() -> {
            helper.assertTrue(PartHelpers.getPart(partPos) != null, "The part was removed");
            helper.assertItemEntityNotPresent(PartTypes.REDSTONE_WRITER.getItem());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigSurvivesModeCycling(GameTestHelper helper) {
        PartPos partPos = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack wrench = createWrench(ItemWrench.Mode.CONFIG);
        clickPart(helper, player, wrench, partPos, true);
        for (int i = 0; i < ItemWrench.Mode.values().length; i++) {
            wrenchItem().incrementMode(wrench);
        }

        helper.succeedWhen(() -> {
            helper.assertValueEqual(wrenchItem().getMode(wrench), ItemWrench.Mode.CONFIG, "The mode did not cycle back");
            helper.assertTrue(
                    PartConfigHelpers.getSnapshot(helper.getLevel().registryAccess(), wrench)
                            .map(snapshot -> snapshot.hasSection(PartConfigSection.PART_SETTINGS))
                            .orElse(false),
                    "The copied configuration was lost when cycling the wrench mode");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchConfigPasteWithoutCopy(GameTestHelper helper) {
        PartPos partPos = placePart(helper, POS_SOURCE, PartTypes.REDSTONE_WRITER);
        IPartType partType = partType(partPos);
        int updateInterval = partType.getUpdateInterval(partState(partPos));

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        clickPart(helper, player, createWrench(ItemWrench.Mode.CONFIG), partPos, false);

        helper.succeedWhen(() -> {
            helper.assertValueEqual(partType.getUpdateInterval(partState(partPos)), updateInterval,
                    "The part was changed");
        });
    }

}
