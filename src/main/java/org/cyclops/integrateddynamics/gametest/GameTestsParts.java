package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPathElement;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.part.PartTypeRedstoneReader;

public class GameTestsParts {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderOnCable(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertTrue(partStateHolder.getState().isEnabled(), Component.literal("Placed part is not enabled"));
            helper.assertTrue(partStateHolder.getState().getChannel() == 0, Component.literal("Placed part is not on channel 0"));
            helper.assertTrue(CableHelpers.isNoFakeCable(helper.getLevel(), helper.absolutePos(POS), null), Component.literal("Cable is fake"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderOnCableAsPlayer(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(PartTypes.REDSTONE_READER.getItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertTrue(partStateHolder.getState().isEnabled(), Component.literal("Placed part is not enabled"));
            helper.assertTrue(partStateHolder.getState().getChannel() == 0, Component.literal("Placed part is not on channel 0"));
            helper.assertTrue(CableHelpers.isNoFakeCable(helper.getLevel(), helper.absolutePos(POS), null), Component.literal("Cable is fake"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithoutCableAsPlayer(GameTestHelper helper) {
        // No cable!

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(PartTypes.REDSTONE_READER.getItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.north()), Direction.SOUTH));
            helper.assertTrue(partStateHolder != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertTrue(partStateHolder.getState().isEnabled(), Component.literal("Placed part is not enabled"));
            helper.assertTrue(partStateHolder.getState().getChannel() == 0, Component.literal("Placed part is not on channel 0"));
            helper.assertTrue(!CableHelpers.isNoFakeCable(helper.getLevel(), helper.absolutePos(POS.north()), null), Component.literal("Cable is not fake"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderOnCableRemove(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Remove cable and redstone reader
        helper.destroyBlock(POS);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder == null, Component.literal("Part was not removed"));

            helper.assertItemEntityNotPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertItemEntityNotPresent(RegistryEntries.ITEM_CABLE.get());
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithoutCableRemove(GameTestHelper helper) {
        // No cable!

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(PartTypes.REDSTONE_READER.getItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.north().north(), Direction.SOUTH);

        // Remove cable and redstone reader
        helper.destroyBlock(POS);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder == null, Component.literal("Part was not removed"));

            helper.assertItemEntityNotPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertItemEntityNotPresent(RegistryEntries.ITEM_CABLE.get());
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderOnCableRemoveWrench(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Remove redstone reader as player with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove part!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(Vec3.atCenterOf(helper.absolutePos(POS)).add(0.25, -1.5, -0.5));
        helper.getBlockState(POS).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(helper.absolutePos(POS)),
                        Direction.NORTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder == null, Component.literal("Part was not removed"));

            helper.assertItemEntityPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertTrue(helper.getEntities(EntityTypes.ITEM).get(0).getItem().has(RegistryEntries.DATACOMPONENT_PART_STATE), Component.literal("Dropped part must have a state"));
            helper.assertItemEntityNotPresent(RegistryEntries.ITEM_CABLE.get());
            helper.assertBlockPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithoutCableRemoveWrench(GameTestHelper helper) {
        // No cable!

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(PartTypes.REDSTONE_READER.getItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.north().north(), Direction.SOUTH);

        // Remove redstone reader as player with wrench
        player.setShiftKeyDown(true); // To remove part!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(Vec3.atCenterOf(helper.absolutePos(POS)).add(0.25, -1.5, -0.5));
        helper.getBlockState(POS).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(helper.absolutePos(POS)),
                        Direction.NORTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder == null, Component.literal("Part was not removed"));

            helper.assertItemEntityPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertTrue(helper.getEntities(EntityTypes.ITEM).get(0).getItem().has(RegistryEntries.DATACOMPONENT_PART_STATE), Component.literal("Dropped part must have a state"));
            helper.assertItemEntityNotPresent(RegistryEntries.ITEM_CABLE.get());
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderOnCableRemovePickaxe(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Remove redstone reader as player with pickaxe
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack toolStack = new ItemStack(Items.DIAMOND_PICKAXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, toolStack);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(POS)).add(0.25, -1.5, -0.5));
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, toolStack, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder == null, Component.literal("Part was not removed"));

            helper.assertItemEntityPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertTrue(!helper.getEntities(EntityTypes.ITEM).get(0).getItem().has(RegistryEntries.DATACOMPONENT_PART_STATE), Component.literal("Dropped part must not have a state"));
            helper.assertItemEntityNotPresent(RegistryEntries.ITEM_CABLE.get());
            helper.assertBlockPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithoutCableRemovePickaxe(GameTestHelper helper) {
        // No cable!

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(PartTypes.REDSTONE_READER.getItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.north().north(), Direction.SOUTH);

        // Remove redstone reader as player with pickaxe
        ItemStack toolStack = new ItemStack(Items.DIAMOND_PICKAXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, toolStack);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(POS)).add(0.25, -1.5, -0.5));
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, toolStack, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder == null, Component.literal("Part was not removed"));

            helper.assertItemEntityPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertTrue(!helper.getEntities(EntityTypes.ITEM).get(0).getItem().has(RegistryEntries.DATACOMPONENT_PART_STATE), Component.literal("Dropped part must not have a state"));
            helper.assertItemEntityNotPresent(RegistryEntries.ITEM_CABLE.get());
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithoutCableRemoveTnt(GameTestHelper helper) {
        // No cable!

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(PartTypes.REDSTONE_READER.getItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.north().north(), Direction.SOUTH);

        // Destroy with TNT
        helper.setBlock(POS.above(), Blocks.TNT);
        helper.setBlock(POS.above().above(), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder == null, Component.literal("Placed part is not missing"));

            helper.assertBlockNotPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderOnCableRemoveCableWrench(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Remove redstone reader as player with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove part!
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RegistryEntries.ITEM_WRENCH.value()));
        player.setPos(Vec3.atCenterOf(helper.absolutePos(POS)).add(0, -1.5, 0.5));
        player.setYRot(180);
        helper.getBlockState(POS).useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(helper.absolutePos(POS)),
                        Direction.SOUTH,
                        helper.absolutePos(POS),
                        false)
        );

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertTrue(partStateHolder.getState().isEnabled(), Component.literal("Placed part is not enabled"));
            helper.assertTrue(partStateHolder.getState().getChannel() == 0, Component.literal("Placed part is not on channel 0"));
            helper.assertTrue(!CableHelpers.isNoFakeCable(helper.getLevel(), helper.absolutePos(POS), null), Component.literal("Cable is not fake"));

            helper.assertItemEntityNotPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertItemEntityPresent(RegistryEntries.ITEM_CABLE.get());
            helper.assertBlockPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderOnCableRemoveCableWithPickaxe(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Remove redstone reader as player with pickaxe
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack toolStack = new ItemStack(Items.DIAMOND_PICKAXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, toolStack);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(POS)).add(0, -1.5, 0.5));
        player.setYRot(180);
        helper.getBlockState(POS).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS), player, toolStack, true, helper.getLevel().getFluidState(helper.absolutePos(POS)));

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertTrue(partStateHolder.getState().isEnabled(), Component.literal("Placed part is not enabled"));
            helper.assertTrue(partStateHolder.getState().getChannel() == 0, Component.literal("Placed part is not on channel 0"));
            helper.assertTrue(!CableHelpers.isNoFakeCable(helper.getLevel(), helper.absolutePos(POS), null), Component.literal("Cable is not fake"));

            helper.assertItemEntityNotPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertItemEntityPresent(RegistryEntries.ITEM_CABLE.get());
            helper.assertBlockPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderOnCableRemoveTnt(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Destroy with TNT
        helper.setBlock(POS.above(), Blocks.TNT);
        helper.setBlock(POS.above().above(), Blocks.REDSTONE_BLOCK);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder == null, Component.literal("Placed part is not missing"));

            helper.assertItemEntityPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertTrue(!helper.getEntities(EntityTypes.ITEM).get(0).getItem().has(RegistryEntries.DATACOMPONENT_PART_STATE), Component.literal("Dropped part must not have a state"));
            helper.assertBlockNotPresent(RegistryEntries.BLOCK_CABLE.value(), POS);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithStateOnCable(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartStateReaderBase<PartTypeRedstoneReader> state = PartTypes.REDSTONE_READER.constructDefaultState();
        state.generateId();
        ItemStack partStack = PartTypes.REDSTONE_READER.getItemStack(ValueDeseralizationContext.of(helper.getLevel()), new PartPathElement(POS), state, true);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, partStack);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertValueEqual(partStateHolder.getState().getId(), state.getId(), Component.literal("Part id is incorrect"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithStateOnCableAsPlayer(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        PartStateReaderBase<PartTypeRedstoneReader> state = PartTypes.REDSTONE_READER.constructDefaultState();
        state.generateId();
        ItemStack partStack = PartTypes.REDSTONE_READER.getItemStack(ValueDeseralizationContext.of(helper.getLevel()), new PartPathElement(POS), state, true);
        player.setItemInHand(InteractionHand.MAIN_HAND, partStack);
        helper.placeAt(player, partStack, POS.south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH));
            helper.assertTrue(partStateHolder != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertValueEqual(partStateHolder.getState().getId(), state.getId(), Component.literal("Part id is incorrect"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsBlocksCableConnection(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place cable facing redstone reader
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.north()), null);
            helper.assertTrue(network1 != network2, Component.literal("Networks of cables with part inbetween are equal"));

            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS)),
                    Sets.newHashSet(),
                    Component.literal("Connected cables are invalid")
            );
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.north())),
                    Sets.newHashSet(),
                    Component.literal("Connected cables are invalid")
            );
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithLever(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.NORTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Attempt to place lever as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(Items.LEVER);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            helper.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(), Component.literal("Item hand is not empty"));
            helper.assertBlockPresent(Blocks.LEVER, POS.north());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithoutCableAsPlayerAddCable(GameTestHelper helper) {
        // No cable!

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(PartTypes.REDSTONE_READER.getItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        helper.placeAt(player, itemStack, POS.south(), Direction.NORTH);

        // Place cable afterwards as player
        ItemStack itemStackCable = new ItemStack(RegistryEntries.ITEM_CABLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackCable);
        helper.placeAt(player, itemStackCable, POS.south(), Direction.NORTH);

        helper.succeedWhen(() -> {
            PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.north()), Direction.SOUTH));
            helper.assertTrue(partStateHolder != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertTrue(partStateHolder.getState().isEnabled(), Component.literal("Placed part is not enabled"));
            helper.assertTrue(partStateHolder.getState().getChannel() == 0, Component.literal("Placed part is not on channel 0"));
            helper.assertTrue(CableHelpers.isNoFakeCable(helper.getLevel(), helper.absolutePos(POS.north()), null), Component.literal("Cable is fake"));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartsRedstoneReaderWithoutCableAsPlayerAddCableTwo(GameTestHelper helper) {
        // No cable!

        // Place redstone reader as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(PartTypes.REDSTONE_READER.getItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack.copy());
        helper.placeAt(player, itemStack.copy(), POS.south(), Direction.NORTH);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack.copy());
        helper.placeAt(player, itemStack.copy(), POS.south().east(), Direction.NORTH);

        // Place cable afterwards as player
        ItemStack itemStackCable = new ItemStack(RegistryEntries.ITEM_CABLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackCable.copy());
        helper.placeAt(player, itemStackCable.copy(), POS.south(), Direction.NORTH);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStackCable.copy());
        helper.placeAt(player, itemStackCable.copy(), POS.south().east(), Direction.NORTH);

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.north()), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.north().east()), null);
            helper.assertTrue(network1 == network2, Component.literal("Networks of connected cables are not equal"));

            PartHelpers.PartStateHolder<?, ?> partStateHolder1 = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.north()), Direction.SOUTH));
            helper.assertTrue(partStateHolder1 != null, Component.literal("Placed part is missing"));
            helper.assertTrue(partStateHolder1.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part is incorrect"));
            helper.assertTrue(partStateHolder1.getState().isEnabled(), Component.literal("Placed part is not enabled"));
            helper.assertTrue(partStateHolder1.getState().getChannel() == 0, Component.literal("Placed part is not on channel 0"));
            helper.assertTrue(CableHelpers.isNoFakeCable(helper.getLevel(), helper.absolutePos(POS.north()), null), Component.literal("Cable is fake"));
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.north())),
                    Sets.newHashSet(Direction.EAST),
                    Component.literal("Connected cables are invalid")
            );

            PartHelpers.PartStateHolder<?, ?> partStateHolder2 = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.north().east()), Direction.SOUTH));
            helper.assertTrue(partStateHolder2 != null, Component.literal("Placed part 2 is missing"));
            helper.assertTrue(partStateHolder2.getPart() == PartTypes.REDSTONE_READER, Component.literal("Placed part 2 is incorrect"));
            helper.assertTrue(partStateHolder2.getState().isEnabled(), Component.literal("Placed part 2 is not enabled"));
            helper.assertTrue(partStateHolder2.getState().getChannel() == 0, Component.literal("Placed part 2 is not on channel 0"));
            helper.assertTrue(CableHelpers.isNoFakeCable(helper.getLevel(), helper.absolutePos(POS.north().east()), null), Component.literal("Cable 2 is fake"));
            helper.assertValueEqual(
                    CableHelpers.getExternallyConnectedCables(helper.getLevel(), helper.absolutePos(POS.north().east())),
                    Sets.newHashSet(Direction.WEST),
                    Component.literal("Connected cables 2 are invalid")
            );
        });
    }

}
