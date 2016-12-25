package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockEnergyBattery extends BlockEnergyBatteryBase {

    @BlockProperty
    public static final PropertyInteger FILL = PropertyInteger.create("fill", 0, 3);

    private static BlockEnergyBattery _instance = null;

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockEnergyBattery getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockEnergyBattery(ExtendedConfig eConfig) {
        super(eConfig);

        setHardness(5.0F);
        setSoundType(SoundType.METAL);
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
        ItemStack empty = new ItemStack(this);
        ItemStack full = new ItemStack(this);
        IEnergyStorage energyStorage = full.getCapability(CapabilityEnergy.ENERGY, null);
        energyStorage.receiveEnergy(energyStorage.getMaxEnergyStored(), false);
        list.add(empty);
        list.add(full);
    }

    public boolean isCreative() {
        return false;
    }

}
