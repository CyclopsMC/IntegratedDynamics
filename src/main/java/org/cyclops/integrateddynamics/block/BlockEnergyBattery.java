package org.cyclops.integrateddynamics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockEnergyBattery extends BlockEnergyBatteryBase {

    public static final MapCodec<BlockEnergyBattery> CODEC = simpleCodec(BlockEnergyBattery::new);

    public BlockEnergyBattery(Block.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public boolean isCreative() {
        return false;
    }

}
