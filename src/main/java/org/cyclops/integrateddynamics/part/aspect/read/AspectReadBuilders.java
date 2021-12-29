package org.cyclops.integrateddynamics.part.aspect.read;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.recipehandler.IRecipeHandler;
import org.cyclops.commoncapabilities.api.capability.temperature.ITemperature;
import org.cyclops.commoncapabilities.api.capability.work.IWorker;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.modcompat.commoncapabilities.BlockCapabilitiesHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.capability.network.EnergyNetworkConfig;
import org.cyclops.integrateddynamics.core.NoteBlockEventReceiver;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Collection of aspect read builders and value propagators.
 * @author rubensworks
 */
public class AspectReadBuilders {

    // --------------- Value type builders ---------------
    public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Pair<PartTarget, IAspectProperties>>
            BUILDER_BOOLEAN = AspectBuilder.forReadType(ValueTypes.BOOLEAN);
    public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<PartTarget, IAspectProperties>>
            BUILDER_INTEGER = AspectBuilder.forReadType(ValueTypes.INTEGER);
    public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, Pair<PartTarget, IAspectProperties>>
            BUILDER_DOUBLE = AspectBuilder.forReadType(ValueTypes.DOUBLE);
    public static final AspectBuilder<ValueTypeLong.ValueLong, ValueTypeLong, Pair<PartTarget, IAspectProperties>>
            BUILDER_LONG = AspectBuilder.forReadType(ValueTypes.LONG);
    public static final AspectBuilder<ValueTypeString.ValueString, ValueTypeString, Pair<PartTarget, IAspectProperties>>
            BUILDER_STRING = AspectBuilder.forReadType(ValueTypes.STRING);
    public static final AspectBuilder<ValueObjectTypeEntity.ValueEntity, ValueObjectTypeEntity, Pair<PartTarget, IAspectProperties>>
            BUILDER_ENTITY = AspectBuilder.forReadType(ValueTypes.OBJECT_ENTITY);
    public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, Pair<PartTarget, IAspectProperties>>
            BUILDER_LIST = AspectBuilder.forReadType(ValueTypes.LIST);
    public static final AspectBuilder<ValueTypeNbt.ValueNbt, ValueTypeNbt, Pair<PartTarget, IAspectProperties>>
            BUILDER_NBT = AspectBuilder.forReadType(ValueTypes.NBT);
    public static final AspectBuilder<IValue, ValueTypeCategoryAny, Pair<PartTarget, IAspectProperties>>
            BUILDER_ANY = AspectBuilder.forReadType(ValueTypes.CATEGORY_ANY);
    public static final AspectBuilder<ValueTypeOperator.ValueOperator, ValueTypeOperator, Pair<PartTarget, IAspectProperties>>
            BUILDER_OPERATOR = AspectBuilder.forReadType(ValueTypes.OPERATOR);

    public static final AspectBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, Pair<PartTarget, IAspectProperties>>
            BUILDER_OBJECT_ITEMSTACK = AspectBuilder.forReadType(ValueTypes.OBJECT_ITEMSTACK);
    public static final AspectBuilder<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock, Pair<PartTarget, IAspectProperties>>
            BUILDER_OBJECT_BLOCK = AspectBuilder.forReadType(ValueTypes.OBJECT_BLOCK);
    public static final AspectBuilder<ValueObjectTypeFluidStack.ValueFluidStack, ValueObjectTypeFluidStack, Pair<PartTarget, IAspectProperties>>
            BUILDER_OBJECT_FLUIDSTACK = AspectBuilder.forReadType(ValueTypes.OBJECT_FLUIDSTACK);

    // --------------- Value type propagators ---------------
    public static final IAspectValuePropagator<Boolean, ValueTypeBoolean.ValueBoolean>
        PROP_GET_BOOLEAN = ValueTypeBoolean.ValueBoolean::of;

    public static final IAspectValuePropagator<Integer, ValueTypeInteger.ValueInteger>
        PROP_GET_INTEGER = ValueTypeInteger.ValueInteger::of;

    public static final IAspectValuePropagator<Double, ValueTypeDouble.ValueDouble>
        PROP_GET_DOUBLE = ValueTypeDouble.ValueDouble::of;

    public static final IAspectValuePropagator<Long, ValueTypeLong.ValueLong>
        PROP_GET_LONG = ValueTypeLong.ValueLong::of;

    public static final IAspectValuePropagator<ItemStack, ValueObjectTypeItemStack.ValueItemStack>
        PROP_GET_ITEMSTACK = ValueObjectTypeItemStack.ValueItemStack::of;

    public static final IAspectValuePropagator<String, ValueTypeString.ValueString>
        PROP_GET_STRING = ValueTypeString.ValueString::of;

    public static final IAspectValuePropagator<BlockState, ValueObjectTypeBlock.ValueBlock>
        PROP_GET_BLOCK = ValueObjectTypeBlock.ValueBlock::of;

    public static final IAspectValuePropagator<FluidStack, ValueObjectTypeFluidStack.ValueFluidStack>
        PROP_GET_FLUIDSTACK = ValueObjectTypeFluidStack.ValueFluidStack::of;

    public static final IAspectValuePropagator<Optional<Tag>,ValueTypeNbt.ValueNbt>
        PROP_GET_NBT = ValueTypeNbt.ValueNbt::of;

    // --------------- Value type validators ---------------
    public static final Predicate<ValueTypeInteger.ValueInteger>
        VALIDATOR_INTEGER_POSITIVE = input -> input.getRawValue() >= 0;

    public static final Predicate<ValueTypeDouble.ValueDouble>
        VALIDATOR_DOUBLE_POSITIVE = input -> input.getRawValue() >= 0;

    // --------------- Generic properties ---------------
    public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_LISTINDEX =
            new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.listindex", VALIDATOR_INTEGER_POSITIVE);
    public static final IAspectProperties LIST_PROPERTIES = new AspectProperties(ImmutableList.<IAspectPropertyTypeInstance>of(
            PROPERTY_LISTINDEX
    ));
    static {
        LIST_PROPERTIES.setValue(PROPERTY_LISTINDEX, ValueTypeInteger.ValueInteger.of(0));
    }

    public static final class Audio {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_RANGE =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.range", VALIDATOR_INTEGER_POSITIVE);
        public static final IAspectProperties NOTE_PROPERTIES = new AspectProperties(ImmutableList.<IAspectPropertyTypeInstance>of(
                PROPERTY_RANGE
        ));
        static {
            NOTE_PROPERTIES.setValue(PROPERTY_RANGE, ValueTypeInteger.ValueInteger.of(64));
        }

        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<PartTarget, IAspectProperties>>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.appendKind("audio");

        public static AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Integer> forInstrument(final NoteBlockInstrument instrument) {
            return BUILDER_INTEGER.appendKind("instrument").handle(input -> {
                for (NoteBlockEvent.Play event : NoteBlockEventReceiver.getInstance().getEvents().get(instrument)) {
                    net.minecraft.world.level.Level world = input.getLeft().getTarget().getPos().getLevel(false);
                    if (world != null) {
                        BlockPos pos = input.getLeft().getTarget().getPos().getBlockPos();
                        int range = input.getRight().getValue(PROPERTY_RANGE).getRawValue();
                        if (world.dimensionType() == event.getWorld().dimensionType()
                                && pos.distSqr(event.getPos()) <= range * range) {
                            return event.getVanillaNoteId();
                        }
                    }
                }
                return -1;
            }, instrument.name().toLowerCase(Locale.ENGLISH)).withProperties(NOTE_PROPERTIES);
        }

    }

    public static final class Block {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos>
                PROP_GET = input -> input.getLeft().getTarget().getPos();

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, DimPos>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "block");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, DimPos>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "block");
        public static final AspectBuilder<ValueTypeString.ValueString, ValueTypeString, DimPos>
                BUILDER_STRING = AspectReadBuilders.BUILDER_STRING.handle(PROP_GET, "block");
        public static final AspectBuilder<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock, DimPos>
                BUILDER_BLOCK = AspectReadBuilders.BUILDER_OBJECT_BLOCK.handle(PROP_GET, "block");
        public static final AspectBuilder<ValueTypeNbt.ValueNbt, ValueTypeNbt, DimPos>
                BUILDER_NBT = AspectReadBuilders.BUILDER_NBT.handle(PROP_GET, "block");

    }

    public static final class Entity {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos>
                PROP_GET = input -> input.getLeft().getTarget().getPos();

        public static final AspectBuilder<ValueObjectTypeEntity.ValueEntity, ValueObjectTypeEntity, Pair<PartTarget, IAspectProperties>>
                BUILDER_ENTITY = AspectReadBuilders.BUILDER_ENTITY.appendKind("entity");
        public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, DimPos>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET, "entity");
        public static final AspectBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, Pair<PartTarget, IAspectProperties>>
                BUILDER_ITEMSTACK = AspectReadBuilders.BUILDER_OBJECT_ITEMSTACK.appendKind("entity");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<PartTarget, IAspectProperties>>
                BUILDER_INTEGER_ALL = AspectReadBuilders.BUILDER_INTEGER.appendKind("entity");

    }

    public static final class ExtraDimensional {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, MinecraftServer>
                PROP_GET = input -> ServerLifecycleHooks.getCurrentServer();

        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, MinecraftServer>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "extradimensional");
        public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, MinecraftServer>
                BUILDER_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET, "extradimensional");
        public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, MinecraftServer>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET, "extradimensional");

    }

    public static final class Fluid {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_TANKID =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.tankid", VALIDATOR_INTEGER_POSITIVE);
        public static final IAspectProperties PROPERTIES = new AspectProperties(ImmutableList.<IAspectPropertyTypeInstance>of(
                PROP_TANKID
        ));
        static {
            PROPERTIES.setValue(PROP_TANKID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        }

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IFluidHandler> PROP_GET = input -> {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            return BlockEntityHelpers.getCapability(dimPos, input.getLeft().getTarget().getSide(),
                    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                    .orElse(EmptyFluidHandler.INSTANCE);
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Pair<IFluidHandler, Integer>> PROP_GET_ACTIVATABLE = input -> {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            IFluidHandler fluidHandler = BlockEntityHelpers.getCapability(dimPos, input.getLeft().getTarget().getSide(),
                    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);
            if(fluidHandler != null) {
                int i = input.getRight().getValue(PROP_TANKID).getRawValue();
                if(i < fluidHandler.getTanks()) {
                    return Pair.of(fluidHandler, i);
                }
            }
            return null;
        };
        public static final IAspectValuePropagator<Pair<IFluidHandler, Integer>, FluidStack>
                PROP_GET_FLUIDSTACK = tankInfo -> tankInfo != null ? tankInfo.getLeft().getFluidInTank(tankInfo.getRight()) : FluidStack.EMPTY;

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList>
                PROP_GET_LIST_FLUIDSTACKS = input -> ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedTankFluidStacks(
                        input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                ));
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList>
                PROP_GET_LIST_CAPACITIES = input -> ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedTankCapacities(
                        input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                ));

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IFluidHandler>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "fluid");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, IFluidHandler>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "fluid");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<IFluidHandler, Integer>>
                BUILDER_INTEGER_ACTIVATABLE = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);
        public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, Pair<IFluidHandler, Integer>>
                BUILDER_DOUBLE_ACTIVATABLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);

    }

    public static final class Inventory {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_SLOTID =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.slotid", VALIDATOR_INTEGER_POSITIVE);
        public static final IAspectProperties PROPERTIES = new AspectProperties(ImmutableList.<IAspectPropertyTypeInstance>of(
                PROPERTY_SLOTID
        ));
        static {
            PROPERTIES.setValue(PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        }

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IItemHandler> PROP_GET = input -> {
            PartPos target = input.getLeft().getTarget();
            return BlockEntityHelpers.getCapability(target.getPos().getLevel(true), target.getPos().getBlockPos(), target.getSide(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ItemStack> PROP_GET_SLOT = input -> {
            PartPos target = input.getLeft().getTarget();
            IItemHandler itemHandler = BlockEntityHelpers.getCapability(target.getPos().getLevel(true), target.getPos().getBlockPos(), target.getSide(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            int slotId = input.getRight().getValue(PROPERTY_SLOTID).getRawValue();
            if(itemHandler != null && slotId >= 0 && slotId < itemHandler.getSlots()) {
                return itemHandler.getStackInSlot(slotId);
            }
            return ItemStack.EMPTY;
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList>
                PROP_GET_LIST = input -> ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedInventory(input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()));

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IItemHandler>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "inventory");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, IItemHandler>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "inventory");
        public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, IItemHandler>
                BUILDER_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET, "inventory");
        public static final AspectBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, ItemStack>
                BUILDER_ITEMSTACK = BUILDER_OBJECT_ITEMSTACK.handle(PROP_GET_SLOT, "inventory").withProperties(PROPERTIES);

    }

    public static final class Machine {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IWorker> PROP_GET_WORKER = input -> {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            return BlockEntityHelpers.getCapability(dimPos.getLevel(true), dimPos.getBlockPos(), input.getLeft().getTarget().getSide(), Capabilities.WORKER).orElse(null);
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ITemperature> PROP_GET_TEMPERATURE = input -> {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            return BlockEntityHelpers.getCapability(dimPos.getLevel(true), dimPos.getBlockPos(), input.getLeft().getTarget().getSide(), Capabilities.TEMPERATURE).orElse(null);
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IRecipeHandler> PROP_GET_RECIPE_HANDLER = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IRecipeHandler>() {
            @Override
            public IRecipeHandler getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos dimPos = input.getLeft().getTarget().getPos();
                return BlockCapabilitiesHelpers.getTileOrBlockCapability(dimPos, input.getLeft().getTarget().getSide(), Capabilities.RECIPE_HANDLER).orElse(null);
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IWorker>
                BUILDER_WORKER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_WORKER, "machine");
        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, ITemperature>
                BUILDER_TEMPERATURE_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_TEMPERATURE, "temperature");
        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IRecipeHandler>
                BUILDER_RECIPE_HANDLER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_RECIPE_HANDLER, "recipehandler");

        public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, ITemperature>
                BUILDER_TEMPERATURE_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET_TEMPERATURE, "temperature");

        public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, Pair<PartTarget, IAspectProperties>>
                BUILDER_RECIPE_HANDLER_LIST = AspectReadBuilders.BUILDER_LIST.appendKind("recipehandler");
        public static final AspectBuilder<ValueTypeOperator.ValueOperator, ValueTypeOperator, Pair<PartTarget, IAspectProperties>>
                BUILDER_RECIPE_HANDLER_OPERATOR = AspectReadBuilders.BUILDER_OPERATOR.appendKind("recipehandler");
    }

    public static final class Network {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_CHANNEL =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.channel");
        public static final IAspectProperties PROPERTIES = new AspectProperties(ImmutableList.<IAspectPropertyTypeInstance>of(
                PROPERTY_CHANNEL
        ));
        static {
            PROPERTIES.setValue(PROPERTY_CHANNEL, ValueTypeInteger.ValueInteger.of(IPositionedAddonsNetwork.WILDCARD_CHANNEL));
        }

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetwork> PROP_GET_NETWORK = input -> {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            return NetworkHelpers.getNetwork(dimPos.getLevel(true), dimPos.getBlockPos(), input.getLeft().getTarget().getSide()).orElse(null);
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, INetwork>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_NETWORK, "network");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, INetwork>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_NETWORK, "network");

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IEnergyStorage> PROP_GET_ENERGY_CHANNEL = input -> {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            INetwork network = NetworkHelpers.getNetwork(dimPos.getLevel(true), dimPos.getBlockPos(), input.getLeft().getTarget().getSide()).orElse(null);
            int channel = input.getRight().getValue(PROPERTY_CHANNEL).getRawValue();
            return network != null ? network.getCapability(EnergyNetworkConfig.CAPABILITY)
                    .map(energyNetwork -> energyNetwork.getChannelExternal(CapabilityEnergy.ENERGY, channel))
                    .orElse(null) : null;
        };

        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, IEnergyStorage>
                ENERGY_BUILDER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_ENERGY_CHANNEL, "network").withProperties(PROPERTIES);

    }

    public static final class Redstone {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_INTERVAL =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.interval", VALIDATOR_INTEGER_POSITIVE);
        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_LENGTH =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.length", VALIDATOR_INTEGER_POSITIVE);
        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_OFFSET =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.offset", VALIDATOR_INTEGER_POSITIVE);
        public static final IAspectProperties PROPERTIES_CLOCK = new AspectProperties(ImmutableList.<IAspectPropertyTypeInstance>of(
                PROPERTY_INTERVAL,
                PROPERTY_LENGTH,
                PROPERTY_OFFSET
        ));
        static {
            PROPERTIES_CLOCK.setValue(PROPERTY_INTERVAL, ValueTypeInteger.ValueInteger.of(20));
            PROPERTIES_CLOCK.setValue(PROPERTY_LENGTH, ValueTypeInteger.ValueInteger.of(1));
            PROPERTIES_CLOCK.setValue(PROPERTY_OFFSET, ValueTypeInteger.ValueInteger.of(0));
        }

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer> PROP_GET = input -> {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            int power = dimPos.getLevel(true).getSignal(dimPos.getBlockPos(), input.getLeft().getCenter().getSide());
            if (power == 0) {
                BlockState targetBlockState = dimPos.getLevel(true).getBlockState(dimPos.getBlockPos());
                power = targetBlockState.getBlock() == Blocks.REDSTONE_WIRE ? targetBlockState.getValue(RedStoneWireBlock.POWER) : 0;
            }
            return power;
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer> PROP_GET_COMPARATOR = input -> {
            DimPos dimPos = input.getLeft().getTarget().getPos();
            BlockState blockState = dimPos.getLevel(true).getBlockState(dimPos.getBlockPos());
            return blockState.hasAnalogOutputSignal()
                    ? blockState.getAnalogOutputSignal(dimPos.getLevel(true), dimPos.getBlockPos()) : 0;
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Boolean> PROP_GET_CLOCK = input -> {
            int interval = Math.max(1, input.getRight().getValue(PROPERTY_INTERVAL).getRawValue());
            int length = Math.max(1, input.getRight().getValue(PROPERTY_LENGTH).getRawValue());
            int offset = input.getRight().getValue(PROPERTY_OFFSET).getRawValue();
            return (input.getLeft().getTarget().getPos().getLevel(true).getGameTime() - offset) % interval < length;
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Integer>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "redstone");
        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Boolean>
                BUILDER_BOOLEAN_CLOCK = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_CLOCK, "redstone")
                    .withProperties(PROPERTIES_CLOCK);
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Integer>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "redstone");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Integer>
                BUILDER_INTEGER_COMPARATOR = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_COMPARATOR, "redstone");

    }

    public static final class World {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos>
                PROP_GET = input -> input.getLeft().getTarget().getPos();

        public static final IAspectValuePropagator<DimPos, net.minecraft.world.level.Level>
                PROP_GET_WORLD = dimPos -> dimPos.getLevel(true);

        public static final IAspectValuePropagator<DimPos, BlockPos>
                PROP_GET_POS = DimPos::getBlockPos;

        private static final com.google.common.base.Predicate<net.minecraft.world.entity.Entity>
                ENTITY_SELECTOR_ITEMFRAME = entity -> entity instanceof ItemFrame;

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ItemFrame> PROP_GET_ITEMFRAME = pair -> {
            DimPos dimPos = pair.getLeft().getTarget().getPos();
            Direction facing = pair.getLeft().getTarget().getSide();
            List<net.minecraft.world.entity.Entity> entities = dimPos.getLevel(true).getEntities((net.minecraft.world.entity.Entity) null,
                    new AABB(dimPos.getBlockPos(), dimPos.getBlockPos().offset(1, 1, 1)), ENTITY_SELECTOR_ITEMFRAME);
            for(net.minecraft.world.entity.Entity entity : entities) {
                if(Direction.fromYRot(((ItemFrame) entity).yRotO) == facing.getOpposite()) {
                    return ((ItemFrame) entity);
                }
            }
            return null;
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, DimPos>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, DimPos>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, DimPos>
                BUILDER_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeLong.ValueLong, ValueTypeLong, DimPos>
                BUILDER_LONG = AspectReadBuilders.BUILDER_LONG.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeString.ValueString, ValueTypeString, DimPos>
                BUILDER_STRING = AspectReadBuilders.BUILDER_STRING.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, DimPos>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET, "world");

    }

}
