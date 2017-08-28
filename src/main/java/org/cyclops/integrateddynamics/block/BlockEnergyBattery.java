package org.cyclops.integrateddynamics.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.client.icon.Icon;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.capability.energystorage.IEnergyStorageCapacity;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockEnergyBattery extends BlockEnergyBatteryBase {

    // TODO: remove in 1.13
    @Deprecated
    @BlockProperty
    public static final PropertyInteger FILL = PropertyInteger.create("fill", 0, 3);

    @Icon(location = "blocks/energy_battery_overlay_side_2")
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite iconOverlay;

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

        if(MinecraftHelpers.isClientSide()) {
            eConfig.getMod().getIconProvider().registerIconHolderObject(this);
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (!BlockHelpers.isValidCreativeTab(this, tab)) return;
        ItemStack itemStack = new ItemStack(this);

        int capacityOriginal = BlockEnergyBatteryConfig.capacity;
        int capacity = capacityOriginal;
        int lastCapacity;
        do{
            ItemStack currentStack = itemStack.copy();
            IEnergyStorageCapacity energyStorage = (IEnergyStorageCapacity) currentStack.getCapability(CapabilityEnergy.ENERGY, null);
            energyStorage.setCapacity(capacity);
            list.add(currentStack.copy());
            energyStorage.receiveEnergy(capacity, false);
            list.add(currentStack.copy());
            lastCapacity = capacity;
            capacity = capacity << 2;
        } while(capacity < Math.min(BlockEnergyBatteryConfig.maxCreativeCapacity, BlockEnergyBatteryConfig.maxCapacity) && capacity > lastCapacity);
    }

    public boolean isCreative() {
        return false;
    }

}
