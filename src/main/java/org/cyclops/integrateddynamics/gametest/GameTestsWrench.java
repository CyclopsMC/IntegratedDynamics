package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.cyclops.cyclopscore.gametest.GameTest;
import org.cyclops.integrateddynamics.RegistryEntries;

public class GameTestsWrench {

    public static final String TEMPLATE_EMPTY = "integrateddynamics:empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);

    protected void instantiateBarrelTest(GameTestHelper helper, Direction barrelDirectionStart, Direction facingDirection, Direction barrelDirectionExpected) {
        // Place log
        helper.setBlock(POS, Blocks.BARREL.defaultBlockState()
                .setValue(BlockStateProperties.FACING, barrelDirectionStart));

        // Right click with wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        InteractionResult result = itemStack.useOn(new UseOnContext(player, player.getUsedItemHand(), new BlockHitResult(Vec3.atCenterOf(POS), facingDirection, helper.absolutePos(POS), false)));

        helper.succeedWhen(() -> {
            helper.assertValueEqual(result, InteractionResult.SUCCESS, Component.literal("Interaction failed"));
            helper.assertBlockProperty(POS, BlockStateProperties.FACING, barrelDirectionExpected);
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelUp1(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.UP, Direction.UP, Direction.DOWN);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelUp2(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.UP, Direction.DOWN, Direction.DOWN);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelUp3(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.UP, Direction.NORTH, Direction.NORTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelUp4(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.UP, Direction.EAST, Direction.EAST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelUp5(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.UP, Direction.SOUTH, Direction.SOUTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelUp6(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.UP, Direction.WEST, Direction.WEST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelDown1(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.DOWN, Direction.UP, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelDown2(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.DOWN, Direction.DOWN, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelDown3(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.DOWN, Direction.NORTH, Direction.NORTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelDown4(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.DOWN, Direction.EAST, Direction.EAST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelDown5(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.DOWN, Direction.SOUTH, Direction.SOUTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelDown6(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.DOWN, Direction.WEST, Direction.WEST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelNorth1(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.NORTH, Direction.UP, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelNorth2(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.NORTH, Direction.DOWN, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelNorth3(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.NORTH, Direction.NORTH, Direction.EAST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelNorth4(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.NORTH, Direction.EAST, Direction.EAST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelNorth5(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.NORTH, Direction.SOUTH, Direction.EAST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelNorth6(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.NORTH, Direction.WEST, Direction.EAST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelEast1(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.EAST, Direction.UP, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelEast2(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.EAST, Direction.DOWN, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelEast3(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.EAST, Direction.NORTH, Direction.SOUTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelEast4(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.EAST, Direction.EAST, Direction.SOUTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelEast5(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.EAST, Direction.SOUTH, Direction.SOUTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelEast6(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.EAST, Direction.WEST, Direction.SOUTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelSouth1(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.SOUTH, Direction.UP, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelSouth2(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.SOUTH, Direction.DOWN, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelSouth3(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.SOUTH, Direction.NORTH, Direction.WEST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelSouth4(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.SOUTH, Direction.EAST, Direction.WEST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelSouth5(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.SOUTH, Direction.SOUTH, Direction.WEST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelSouth6(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.SOUTH, Direction.WEST, Direction.WEST);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelWest1(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.WEST, Direction.UP, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelWest2(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.WEST, Direction.DOWN, Direction.UP);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelWest3(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.WEST, Direction.NORTH, Direction.NORTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelWest4(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.WEST, Direction.EAST, Direction.NORTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelWest5(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.WEST, Direction.SOUTH, Direction.NORTH);
    }
    @GameTest(template = TEMPLATE_EMPTY)
    public void testWrenchBarrelWest6(GameTestHelper helper) {
        instantiateBarrelTest(helper, Direction.WEST, Direction.WEST, Direction.NORTH);
    }

}
