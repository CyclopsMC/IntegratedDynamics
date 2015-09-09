package org.cyclops.integrateddynamics.part.aspect;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.part.aspect.read.fluid.*;
import org.cyclops.integrateddynamics.part.aspect.read.inventory.*;
import org.cyclops.integrateddynamics.part.aspect.read.redstone.*;
import org.cyclops.integrateddynamics.part.aspect.read.world.*;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBooleanRedstone;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteIntegerRedstone;

/**
 * Collection of all aspects.
 * @author rubensworks
 */
public class Aspects {

    public static final IAspectRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IAspectRegistry.class);

    public static void load() {}

    // --------------- Read ---------------
    // --- Redstone ---
    public static final AspectReadBooleanRedstoneLow READ_BOOLEAN_REDSTONE_LOW = new AspectReadBooleanRedstoneLow();
    public static final AspectReadBooleanRedstoneNonLow READ_BOOLEAN_REDSTONE_NONLOW = new AspectReadBooleanRedstoneNonLow();
    public static final AspectReadBooleanRedstoneHigh READ_BOOLEAN_REDSTONE_HIGH = new AspectReadBooleanRedstoneHigh();

    public static final AspectReadIntegerRedstoneValue READ_INTEGER_REDSTONE_VALUE = new AspectReadIntegerRedstoneValue();
    public static final AspectReadIntegerRedstoneComparator READ_INTEGER_REDSTONE_COMPARATOR = new AspectReadIntegerRedstoneComparator();

    // --- Inventory ---
    public static final AspectReadBooleanInventoryFull READ_BOOLEAN_INVENTORY_FULL = new AspectReadBooleanInventoryFull();
    public static final AspectReadBooleanInventoryEmpty READ_BOOLEAN_INVENTORY_EMPTY = new AspectReadBooleanInventoryEmpty();
    public static final AspectReadBooleanInventoryNonEmpty READ_BOOLEAN_INVENTORY_NONEMPTY = new AspectReadBooleanInventoryNonEmpty();
    public static final AspectReadBooleanInventoryApplicable READ_BOOLEAN_INVENTORY_APPLICABLE = new AspectReadBooleanInventoryApplicable();

    public static final AspectReadIntegerInventory READ_INTEGER_INVENTORY_COUNT = new AspectReadIntegerInventory();

    // --- World ---
    public static final AspectReadBooleanWorldBlock READ_BOOLEAN_WORLD_BLOCK = new AspectReadBooleanWorldBlock();
    public static final AspectReadBooleanWorldEntity READ_BOOLEAN_WORLD_ENTITY = new AspectReadBooleanWorldEntity();
    public static final AspectReadBooleanWorldMob READ_BOOLEAN_WORLD_MOB = new AspectReadBooleanWorldMob();
    public static final AspectReadBooleanWorldPlayer READ_BOOLEAN_WORLD_PLAYER = new AspectReadBooleanWorldPlayer();
    public static final AspectReadBooleanWorldItem READ_BOOLEAN_WORLD_ITEM = new AspectReadBooleanWorldItem();
    public static final AspectReadBooleanWorldWeatherClear READ_BOOLEAN_WORLD_WEATHER_CLEAR = new AspectReadBooleanWorldWeatherClear();
    public static final AspectReadBooleanWorldWeatherRaining READ_BOOLEAN_WORLD_WEATHER_RAINING = new AspectReadBooleanWorldWeatherRaining();
    public static final AspectReadBooleanWorldWeatherThunder READ_BOOLEAN_WORLD_WEATHER_THUNDER = new AspectReadBooleanWorldWeatherThunder();

    public static final AspectReadIntegerWorldEntity READ_INTEGER_WORLD_ENTITY = new AspectReadIntegerWorldEntity();
    public static final AspectReadIntegerWorldTime READ_INTEGER_WORLD_TIME = new AspectReadIntegerWorldTime();
    public static final AspectReadIntegerWorldTotalTime READ_INTEGER_WORLD_TOTALTIME = new AspectReadIntegerWorldTotalTime();
    public static final AspectReadIntegerWorldRainCountdown READ_INTEGER_WORLD_RAINCOUNTDOWN = new AspectReadIntegerWorldRainCountdown();

    public static final AspectReadStringWorldBlockName READ_STRING_WORLD_BLOCKNAME = new AspectReadStringWorldBlockName();

    // --- Fluid ---
    public static final AspectReadBooleanFluidFull READ_BOOLEAN_FLUID_FULL = new AspectReadBooleanFluidFull();
    public static final AspectReadBooleanFluidEmpty READ_BOOLEAN_FLUID_EMPTY = new AspectReadBooleanFluidEmpty();
    public static final AspectReadBooleanFluidNonEmpty READ_BOOLEAN_FLUID_NONEMPTY = new AspectReadBooleanFluidNonEmpty();
    public static final AspectReadBooleanFluidApplicable READ_BOOLEAN_FLUID_APPLICABLE = new AspectReadBooleanFluidApplicable();
    public static final AspectReadBooleanFluidGaseous READ_BOOLEAN_FLUID_GASEOUS = new AspectReadBooleanFluidGaseous();

    public static final AspectReadIntegerFluidAmount READ_INTEGER_FLUID_AMOUNT = new AspectReadIntegerFluidAmount();
    public static final AspectReadIntegerFluidAmountTotal READ_INTEGER_FLUID_AMOUNTTOTAL = new AspectReadIntegerFluidAmountTotal();
    public static final AspectReadIntegerFluidCapacity READ_INTEGER_FLUID_CAPACITY = new AspectReadIntegerFluidCapacity();
    public static final AspectReadIntegerFluidCapacityTotal READ_INTEGER_FLUID_CAPACITYTOTAL = new AspectReadIntegerFluidCapacityTotal();
    public static final AspectReadIntegerFluidTanks READ_INTEGER_FLUID_TANKS = new AspectReadIntegerFluidTanks();
    public static final AspectReadIntegerFluidDensity READ_INTEGER_FLUID_DENSITY = new AspectReadIntegerFluidDensity();
    public static final AspectReadIntegerFluidLuminosity READ_INTEGER_FLUID_LUMINOSITY = new AspectReadIntegerFluidLuminosity();
    public static final AspectReadIntegerFluidTemperature READ_INTEGER_FLUID_TEMPERATURE = new AspectReadIntegerFluidTemperature();
    public static final AspectReadIntegerFluidViscosity READ_INTEGER_FLUID_VISCOSITY = new AspectReadIntegerFluidViscosity();

    public static final AspectReadStringFluidName READ_STRING_FLUID_NAME = new AspectReadStringFluidName();
    public static final AspectReadStringFluidBlockName READ_STRING_FLUID_BLOCKNAME = new AspectReadStringFluidBlockName();
    public static final AspectReadStringFluidRarity READ_STRING_FLUID_RARITY = new AspectReadStringFluidRarity();

    // --------------- Write ---------------
    public static final AspectWriteBooleanRedstone WRITE_BOOLEAN_REDSTONE = new AspectWriteBooleanRedstone();

    public static final AspectWriteIntegerRedstone WRITE_INTEGER_REDSTONE = new AspectWriteIntegerRedstone();

}
