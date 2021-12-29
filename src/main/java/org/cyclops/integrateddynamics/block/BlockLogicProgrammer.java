package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.cyclops.cyclopscore.block.BlockGui;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

import javax.annotation.Nullable;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockLogicProgrammer extends BlockGui {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockLogicProgrammer(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockItemUseContext) {
        return this.defaultBlockState().setValue(FACING, blockItemUseContext.getPlayer().getDirection().getOpposite());
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState blockState, Level world, BlockPos blockPos) {
        return new SimpleMenuProvider(new MenuConstructor() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                return new ContainerLogicProgrammer(id, playerInventory);
            }
        }, new TranslatableComponent("block.integrateddynamics.logic_programmer"));
    }

}
