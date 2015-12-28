package org.cyclops.integrateddynamics.part.aspect;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.part.aspect.read.fluid.*;
import org.cyclops.integrateddynamics.part.aspect.read.inventory.*;
import org.cyclops.integrateddynamics.part.aspect.read.minecraft.AspectReadIntegerMinecraftPlayerCount;
import org.cyclops.integrateddynamics.part.aspect.read.minecraft.AspectReadIntegerMinecraftRandom;
import org.cyclops.integrateddynamics.part.aspect.read.minecraft.AspectReadIntegerMinecraftTicktime;
import org.cyclops.integrateddynamics.part.aspect.read.network.*;
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

    public static final AspectReadIntegerInventoryCount READ_INTEGER_INVENTORY_COUNT = new AspectReadIntegerInventoryCount();

    public static final AspectReadListInventoryItemStacks READ_LIST_INVENTORY_ITEMSTACKS = new AspectReadListInventoryItemStacks();

    public static final AspectReadObjectItemStackInventorySlot READ_OBJECT_ITEM_STACK_INVENTORY_SLOT = new AspectReadObjectItemStackInventorySlot();

    // --- World ---
    public static final AspectReadBooleanWorldBlock READ_BOOLEAN_WORLD_BLOCK = new AspectReadBooleanWorldBlock();
    public static final AspectReadBooleanWorldWeatherClear READ_BOOLEAN_WORLD_WEATHER_CLEAR = new AspectReadBooleanWorldWeatherClear();
    public static final AspectReadBooleanWorldWeatherRaining READ_BOOLEAN_WORLD_WEATHER_RAINING = new AspectReadBooleanWorldWeatherRaining();
    public static final AspectReadBooleanWorldWeatherThunder READ_BOOLEAN_WORLD_WEATHER_THUNDER = new AspectReadBooleanWorldWeatherThunder();
    public static final AspectReadBooleanWorldIsDay READ_BOOLEAN_WORLD_ISDAY = new AspectReadBooleanWorldIsDay();
    public static final AspectReadBooleanWorldIsNight READ_BOOLEAN_WORLD_ISNIGHT = new AspectReadBooleanWorldIsNight();

    public static final AspectReadIntegerWorldRainCountdown READ_INTEGER_WORLD_RAINCOUNTDOWN = new AspectReadIntegerWorldRainCountdown();
    public static final AspectReadIntegerWorldTicktime READ_INTEGER_WORLD_TICKTIME = new AspectReadIntegerWorldTicktime();
    public static final AspectReadIntegerWorldDayTime READ_INTEGER_WORLD_DAYTIME = new AspectReadIntegerWorldDayTime();
    public static final AspectReadIntegerWorldLightLevel READ_INTEGER_WORLD_LIGHT_LEVEL = new AspectReadIntegerWorldLightLevel();
    public static final AspectReadIntegerWorldPlayerCount READ_INTEGER_WORLD_PLAYERCOUNT = new AspectReadIntegerWorldPlayerCount();
    public static final AspectReadIntegerWorldDimension READ_INTEGER_WORLD_DIMENSION = new AspectReadIntegerWorldDimension();
    public static final AspectReadIntegerWorldPosX READ_INTEGER_WORLD_POSX = new AspectReadIntegerWorldPosX();
    public static final AspectReadIntegerWorldPosY READ_INTEGER_WORLD_POSY = new AspectReadIntegerWorldPosY();
    public static final AspectReadIntegerWorldPosZ READ_INTEGER_WORLD_POSZ = new AspectReadIntegerWorldPosZ();

    public static final AspectReadLongWorldTime READ_LONG_WORLD_TIME = new AspectReadLongWorldTime();
    public static final AspectReadLongWorldTotalTime READ_LONG_WORLD_TOTALTIME = new AspectReadLongWorldTotalTime();

    public static final AspectReadStringWorldName READ_STRING_WORLD_NAME = new AspectReadStringWorldName();

    public static final AspectReadObjectBlockWorld READ_OBJECT_BLOCK_WORLD_BLOCK = new AspectReadObjectBlockWorld();

    public static final AspectReadListWorldEntities READ_LIST_WORLD_ENTITIES = new AspectReadListWorldEntities();

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

    public static final AspectReadDoubleFluidFillRatio READ_DOUBLE_FLUID_FILLRATIO = new AspectReadDoubleFluidFillRatio();

    public static final AspectReadStringFluidName READ_STRING_FLUID_NAME = new AspectReadStringFluidName();
    public static final AspectReadStringFluidRarity READ_STRING_FLUID_RARITY = new AspectReadStringFluidRarity();

    public static final AspectReadObjectBlockFluid READ_BLOCK_FLUID_BLOCK = new AspectReadObjectBlockFluid();

    // --- Minecraft ---
    public static final AspectReadIntegerMinecraftRandom READ_INTEGER_MINECRAFT_RANDOM = new AspectReadIntegerMinecraftRandom();
    public static final AspectReadIntegerMinecraftPlayerCount READ_INTEGER_MINECRAFT_PLAYERCOUNT = new AspectReadIntegerMinecraftPlayerCount();
    public static final AspectReadIntegerMinecraftTicktime READ_INTEGER_MINECRAFT_TICKTIME = new AspectReadIntegerMinecraftTicktime();

    // --- Network ---
    public static final AspectReadBooleanNetworkApplicable READ_BOOLEAN_NETWORK_APPLICABLE = new AspectReadBooleanNetworkApplicable();

    public static final AspectReadIntegerNetworkElementCount READ_INTEGER_NETWORK_ELEMENT_COUNT = new AspectReadIntegerNetworkElementCount();
    public static final AspectReadIntegerNetworkEnergyBatteryCount READ_INTEGER_NETWORK_ENERGY_BATTERY_COUNT = new AspectReadIntegerNetworkEnergyBatteryCount();
    public static final AspectReadIntegerNetworkEnergyStored READ_INTEGER_NETWORK_ENERGY_STORED = new AspectReadIntegerNetworkEnergyStored();
    public static final AspectReadIntegerNetworkEnergyMax READ_INTEGER_NETWORK_ENERGY_MAX = new AspectReadIntegerNetworkEnergyMax();

    // --------------- Write ---------------
    public static final AspectWriteBooleanRedstone WRITE_BOOLEAN_REDSTONE = new AspectWriteBooleanRedstone();

    public static final AspectWriteIntegerRedstone WRITE_INTEGER_REDSTONE = new AspectWriteIntegerRedstone();

}
