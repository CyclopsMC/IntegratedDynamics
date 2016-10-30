package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockLeaves;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.item.ItemMenrilBerriesConfig;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Config for the Menril Leaves.
 * @author rubensworks
 *
 */
public class BlockMenrilLeavesConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilLeavesConfig _instance;

    /**
     * A 1/x chance menril berries will be dropped when breaking a leaves block.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.BLOCK, comment = "A 1/x chance menril berries will be dropped when breaking a leaves block.", isCommandable = true)
    public static int berriesDropChance = 4;

    /**
     * Make a new instance.
     */
    public BlockMenrilLeavesConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menrilLeaves",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        return (ConfigurableBlockLeaves) new ConfigurableBlockLeaves(this) {
            @Override
            public Item getItemDropped(IBlockState iBlockState, Random random, int i) {
                return Item.getItemFromBlock(BlockMenrilSaplingConfig._instance.getBlockInstance());
            }

            @Override
            public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
                List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
                if(world instanceof World && !((World) world).isRemote) {
                    if(((World) world).rand.nextInt(berriesDropChance) == 0) {
                        drops.add(new ItemStack(ItemMenrilBerriesConfig._instance.getItemInstance()));
                    }
                }
                return drops;
            }

            @SuppressWarnings("deprecation")
            @Override
            public SoundType getSoundType() {
                return SoundType.GROUND;
            }

            @Nullable
            @Override
            protected ItemStack createStackedBlock(IBlockState state) {
                return new ItemStack(this);
            }
        }.setHardness(0.2F).setLightLevel(0.65F).setLightOpacity(1);
    }
    
    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_TREELEAVES;
    }
    
    @Override
    public void onRegistered() {
    	Blocks.FIRE.setFireInfo(getBlockInstance(), 5, 20);
    }
    
}
