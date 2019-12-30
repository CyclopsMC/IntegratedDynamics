package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Menril Leaves.
 * @author rubensworks
 *
 */
public class BlockMenrilLeavesConfig extends BlockConfig {

    @ConfigurableProperty(category = "block", comment = "A 1/x chance menril berries will be dropped when breaking a leaves block.", isCommandable = true, minimalValue = 0)
    public static int berriesDropChance = 4;

    public BlockMenrilLeavesConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_leaves",
                eConfig -> new LeavesBlock(Block.Properties.create(Material.LEAVES)
                        .hardnessAndResistance(0.2F)
                        .tickRandomly()
                        .sound(SoundType.PLANT)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    // TODO: loot tables
    /*@Override
    protected ConfigurableBlockLeaves initSubInstance() {
        return (ConfigurableBlockLeaves) new ConfigurableBlockLeaves(this) {
            @Override
            public Item getItemDropped(BlockState iBlockState, Random random, int i) {
                return Item.getItemFromBlock(BlockMenrilSaplingConfig._instance.getBlockInstance());
            }

            @Override
            public void getDrops(NonNullList<ItemStack> drops, IBlockReader world, BlockPos pos, BlockState state, int fortune) {
                super.getDrops(drops, world, pos, state, fortune);
                if(world instanceof World && !((World) world).isRemote) {
                    if(((World) world).rand.nextInt(berriesDropChance) == 0) {
                        drops.add(new ItemStack(ItemMenrilBerriesConfig._instance.getItemInstance()));
                    }
                }
            }

            @Override
            public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos, PlayerEntity player) {
                return new ItemStack(this);
            }
        }.setHardness(0.2F).setLightLevel(0.65F).setLightOpacity(1);
    }*/
    
    @Override
    public void onRegistered() {
        ((FireBlock) Blocks.FIRE).setFireInfo(RegistryEntries.BLOCK_MENRIL_LEAVES, 5, 20);
    }
    
}
