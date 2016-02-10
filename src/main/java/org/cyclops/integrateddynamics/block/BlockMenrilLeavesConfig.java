package org.cyclops.integrateddynamics.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockLeaves;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

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
        }.setHardness(0.2F).setLightLevel(0.65F).setLightOpacity(1).setStepSound(Block.soundTypeGrass);
    }
    
    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_TREELEAVES;
    }
    
    @Override
    public void onRegistered() {
    	Blocks.fire.setFireInfo(getBlockInstance(), 5, 20);
    }
    
}
