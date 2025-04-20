package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.createVariableFromReader;
import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.placeVariableInWriter;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsOffsets {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(4, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testOffsetsRedstoneReaderToWriter(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartPos posReader = PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        increaseMaxOffset(helper, posReader, 4);
        boolean changedOffsetReader = setOffset(posReader, new Vec3i(-2, 0, 0));

        // Place redstone writer
        PartPos posWriter = PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        increaseMaxOffset(helper, posWriter, 4);
        boolean changedOffsetWriter = setOffset(posWriter, new Vec3i(2, 0, 0));

        // Produce a redstone signal
        helper.setBlock(POS.offset(-2, 0, 0).west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), Aspects.Write.Redstone.INTEGER, variableAspect);

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.offset(2, 0, 0).east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(2, 0, 0).east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertTrue(changedOffsetReader, "Setting offset in reader failed");
            helper.assertTrue(changedOffsetWriter, "Setting offset in writer failed");

            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.POWER, 14);

            IPartStateWriter partStateWriter = (IPartStateWriter) PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)).getState();
            helper.assertFalse(partStateWriter.isDeactivated(), "Writer is deactivated");
            helper.assertValueEqual(
                    PartTypes.REDSTONE_WRITER.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ACTIVE,
                    "Block status is incorrect"
            );
            helper.assertValueEqual(partStateWriter.getActiveAspect(), Aspects.Write.Redstone.INTEGER, "Active aspect is incorrect");
            helper.assertTrue(partStateWriter.getErrors(Aspects.Write.Redstone.INTEGER).isEmpty(), "Active aspect has errors");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testOffsetsRedstoneReaderToWriterOffset(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartPos posReader = PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        increaseMaxOffset(helper, posReader, 4);
        boolean changedOffsetReader = setOffset(posReader, new Vec3i(-2, 0, 0));

        // Place redstone writer
        PartPos posWriter = PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        increaseMaxOffset(helper, posWriter, 4);
        boolean changedOffsetWriter = setOffset(posWriter, new Vec3i(2, 0, 0));

        // Produce a redstone signal
        helper.setBlock(POS.offset(-2, 0, 0).west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), Aspects.Write.Redstone.INTEGER, variableAspect);

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.offset(2, 0, 0).east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(2, 0, 0).east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertTrue(changedOffsetReader, "Setting offset in reader failed");
            helper.assertTrue(changedOffsetWriter, "Setting offset in writer failed");

            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.POWER, 14);

            IPartStateWriter partStateWriter = (IPartStateWriter) PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)).getState();
            helper.assertFalse(partStateWriter.isDeactivated(), "Writer is deactivated");
            helper.assertValueEqual(
                    PartTypes.REDSTONE_WRITER.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ACTIVE,
                    "Block status is incorrect"
            );
            helper.assertValueEqual(partStateWriter.getActiveAspect(), Aspects.Write.Redstone.INTEGER, "Active aspect is incorrect");
            helper.assertTrue(partStateWriter.getErrors(Aspects.Write.Redstone.INTEGER).isEmpty(), "Active aspect has errors");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testOffsetsRedstoneReaderToWriterOffsetAndSide(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartPos posReader = PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        increaseMaxOffset(helper, posReader, 4);
        boolean changedOffsetReader = setOffset(posReader, new Vec3i(-2, 0, 0));
        setOffsetSide(posReader, Direction.SOUTH);

        // Place redstone writer
        PartPos posWriter = PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        increaseMaxOffset(helper, posWriter, 4);
        boolean changedOffsetWriter = setOffset(posWriter, new Vec3i(2, 0, 0));
        setOffsetSide(posWriter, Direction.SOUTH);

        // Produce a redstone signal
        helper.setBlock(POS.offset(-2, 0, 0).west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), Aspects.Write.Redstone.INTEGER, variableAspect);

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.offset(2, 0, 0).east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(2, 0, 0).east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertTrue(changedOffsetReader, "Setting offset in reader failed");
            helper.assertTrue(changedOffsetWriter, "Setting offset in writer failed");

            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.POWER, 14);

            IPartStateWriter partStateWriter = (IPartStateWriter) PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)).getState();
            helper.assertFalse(partStateWriter.isDeactivated(), "Writer is deactivated");
            helper.assertValueEqual(
                    PartTypes.REDSTONE_WRITER.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ACTIVE,
                    "Block status is incorrect"
            );
            helper.assertValueEqual(partStateWriter.getActiveAspect(), Aspects.Write.Redstone.INTEGER, "Active aspect is incorrect");
            helper.assertTrue(partStateWriter.getErrors(Aspects.Write.Redstone.INTEGER).isEmpty(), "Active aspect has errors");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testOffsetsRedstoneReaderToWriterOnlySide(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartPos posReader = PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        increaseMaxOffset(helper, posReader, 4);
        setOffsetSide(posReader, Direction.SOUTH);

        // Place redstone writer
        PartPos posWriter = PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        increaseMaxOffset(helper, posWriter, 4);
        setOffsetSide(posWriter, Direction.SOUTH);

        // Produce a redstone signal
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), Aspects.Write.Redstone.INTEGER, variableAspect);

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(POS.east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.east().east(), RedStoneWireBlock.POWER, 14);

            IPartStateWriter partStateWriter = (IPartStateWriter) PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)).getState();
            helper.assertFalse(partStateWriter.isDeactivated(), "Writer is deactivated");
            helper.assertValueEqual(
                    PartTypes.REDSTONE_WRITER.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ACTIVE,
                    "Block status is incorrect"
            );
            helper.assertValueEqual(partStateWriter.getActiveAspect(), Aspects.Write.Redstone.INTEGER, "Active aspect is incorrect");
            helper.assertTrue(partStateWriter.getErrors(Aspects.Write.Redstone.INTEGER).isEmpty(), "Active aspect has errors");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testOffsetsRedstoneReaderToWriterNoMaxOffset(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartPos posReader = PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        boolean changedOffsetReader = setOffset(posReader, new Vec3i(-2, 0, 0));

        // Place redstone writer
        PartPos posWriter = PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        boolean changedOffsetWriter = setOffset(posWriter, new Vec3i(2, 0, 0));

        // Produce a redstone signal
        helper.setBlock(POS.offset(-2, 0, 0).west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), Aspects.Write.Redstone.INTEGER, variableAspect);

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.offset(2, 0, 0).east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(2, 0, 0).east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertFalse(changedOffsetReader, "Setting offset in reader did not fail");
            helper.assertFalse(changedOffsetWriter, "Setting offset in writer did not fail");

            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.POWER, 0);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testOffsetsRedstoneReaderToWriterInsufficientMaxOffset(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartPos posReader = PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        increaseMaxOffset(helper, posReader, 1);
        boolean changedOffsetReader = setOffset(posReader, new Vec3i(-2, 0, 0));

        // Place redstone writer
        PartPos posWriter = PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));
        increaseMaxOffset(helper, posWriter, 1);
        boolean changedOffsetWriter = setOffset(posWriter, new Vec3i(2, 0, 0));

        // Produce a redstone signal
        helper.setBlock(POS.offset(-2, 0, 0).west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(-2, 0, 0).west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), Aspects.Write.Redstone.INTEGER, variableAspect);

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.offset(2, 0, 0).east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.offset(2, 0, 0).east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertFalse(changedOffsetReader, "Setting offset in reader did not fail");
            helper.assertFalse(changedOffsetWriter, "Setting offset in writer did not fail");

            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.offset(2, 0, 0).east().east(), RedStoneWireBlock.POWER, 0);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testOffsetsDropOnBreak(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS.above(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.above().east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartPos posReader = PartPos.of(helper.getLevel(), helper.absolutePos(POS.above()), Direction.WEST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.above()), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        increaseMaxOffset(helper, posReader, 4);
        boolean changedOffsetReader = setOffset(posReader, new Vec3i(-2, 0, 0));

        // Remove redstone reader with pickaxe
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove part!
        facePlayerToPart(player, posReader);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.getBlockState(POS.above()).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS.above()), player, true, helper.getLevel().getFluidState(helper.absolutePos(POS.above())));

        helper.succeedWhen(() -> {
            helper.assertTrue(changedOffsetReader, "Setting offset in reader failed");

            helper.assertItemEntityPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertItemEntityPresent(RegistryEntries.ITEM_ENHANCEMENT_OFFSET.value());
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testOffsetsNoDropOnBreakWithoutOffsets(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS.above(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.above().east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartPos posReader = PartPos.of(helper.getLevel(), helper.absolutePos(POS.above()), Direction.WEST);
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.above()), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Remove redstone reader with pickaxe
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true); // To remove part!
        facePlayerToPart(player, posReader);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.getBlockState(POS.above()).onDestroyedByPlayer(helper.getLevel(), helper.absolutePos(POS.above()), player, true, helper.getLevel().getFluidState(helper.absolutePos(POS.above())));

        helper.succeedWhen(() -> {
            helper.assertItemEntityPresent(PartTypes.REDSTONE_READER.getItem());
            helper.assertItemEntityNotPresent(RegistryEntries.ITEM_ENHANCEMENT_OFFSET.value());
        });
    }

    public static void increaseMaxOffset(GameTestHelper helper, PartPos partPos, int offset) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_ENHANCEMENT_OFFSET.value());
        RegistryEntries.ITEM_ENHANCEMENT_OFFSET.value().setEnhancementValue(itemStack, offset);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        facePlayerToPart(player, partPos);

        partPos.getPos().getLevel(true).getBlockState(partPos.getPos().getBlockPos()).useItemOn(itemStack, helper.getLevel(), player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        partPos.getPos().getBlockPos().getCenter(),
                        partPos.getSide(),
                        partPos.getPos().getBlockPos(),
                        false));
    }

    public static void facePlayerToPart(Player player, PartPos partPos) {
        player.setYRot(partPos.getSide().getRotation().y() * 180);
        player.setPos(partPos.getPos().getBlockPos().getCenter()
                .add(0, -1.5, 0)
                .add(Vec3.atLowerCornerOf(partPos.getSide().getNormal()).multiply(0.75, 0.75, 0.75))
        );

//        helper.getLevel().sendParticles(new ParticleBlurData(1, 1, 1, 1, 100), player.position().x, player.position().y + player.getEyeHeight(), player.position().z, 10, 0, 0, 0, 0); // For debugging
    }

    public static boolean setOffset(PartPos partPos, Vec3i offset) {
        PartHelpers.PartStateHolder<?, ?> partAndState = PartHelpers.getPart(partPos);
        return ((IPartType) partAndState.getPart()).setTargetOffset(partAndState.getState(), partPos, offset);
    }

    public static void setOffsetSide(PartPos partPos, Direction direction) {
        PartHelpers.PartStateHolder<?, ?> partAndState = PartHelpers.getPart(partPos);
        ((IPartType) partAndState.getPart()).setTargetSideOverride(partAndState.getState(), direction);
    }

}
