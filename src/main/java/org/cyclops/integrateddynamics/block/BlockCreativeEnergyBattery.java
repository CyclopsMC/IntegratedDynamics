package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockCreativeEnergyBattery extends BlockEnergyBatteryBase {

    private static BlockCreativeEnergyBattery _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockCreativeEnergyBattery getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockCreativeEnergyBattery(ExtendedConfig<BlockConfig> eConfig) {
        super(eConfig);

        setHardness(5.0F);
        setSoundType(SoundType.METAL);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (!BlockHelpers.isValidCreativeTab(this, tab)) return;
        ItemStack full = new ItemStack(this);
        IEnergyStorage energyStorage = full.getCapability(CapabilityEnergy.ENERGY, null);
        fill(energyStorage);
        list.add(full);
    }

    public boolean isCreative() {
        return true;
    }

}
