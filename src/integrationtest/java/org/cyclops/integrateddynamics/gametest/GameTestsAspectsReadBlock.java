package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspect;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadBlock {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockBooleanBlockTrue(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.BOOLEAN_BLOCK, ValueTypeBoolean.ValueBoolean.of(true));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockBooleanBlockFalse(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.BOOLEAN_BLOCK, ValueTypeBoolean.ValueBoolean.of(false));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockDimension(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.STRING_DIMENSION, ValueTypeString.ValueString.of(helper.getLevel().dimension().location().toString()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockPosX(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.INTEGER_POSX, ValueTypeInteger.ValueInteger.of(helper.absolutePos(POS.west()).getX()));
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockPosY(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.INTEGER_POSY, ValueTypeInteger.ValueInteger.of(helper.absolutePos(POS.west()).getY()));
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockPosZ(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.INTEGER_POSZ, ValueTypeInteger.ValueInteger.of(helper.absolutePos(POS.west()).getZ()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockBlock(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.STONE);
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.BLOCK, ValueObjectTypeBlock.ValueBlock.of(Blocks.STONE.defaultBlockState()));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockNbt(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.FURNACE);
        CompoundTag tag = helper.getBlockEntity(POS.west())
                .saveWithFullMetadata(helper.getLevel().registryAccess());
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.NBT, ValueTypeNbt.ValueNbt.of(tag));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockBiome(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.STRING_DIMENSION, ValueTypeString.ValueString.of("minecraft:overworld"));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadBlockLight(GameTestHelper helper) {
        testReadAspect(POS, helper, PartTypes.BLOCK_READER, Aspects.Read.Block.INTEGER_LIGHT, ValueTypeInteger.ValueInteger.of(helper.getLevel().getMaxLocalRawBrightness(helper.absolutePos(POS.west()))));
    }

}
