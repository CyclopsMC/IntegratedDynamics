package org.cyclops.integrateddynamics.part.aspect.read;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.commoncapabilities.api.capability.work.IWorker;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.NoteBlockEventReceiver;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

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

    public static final AspectBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, Pair<PartTarget, IAspectProperties>>
            BUILDER_OBJECT_ITEMSTACK = AspectBuilder.forReadType(ValueTypes.OBJECT_ITEMSTACK);
    public static final AspectBuilder<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock, Pair<PartTarget, IAspectProperties>>
            BUILDER_OBJECT_BLOCK = AspectBuilder.forReadType(ValueTypes.OBJECT_BLOCK);
    public static final AspectBuilder<ValueObjectTypeFluidStack.ValueFluidStack, ValueObjectTypeFluidStack, Pair<PartTarget, IAspectProperties>>
            BUILDER_OBJECT_FLUIDSTACK = AspectBuilder.forReadType(ValueTypes.OBJECT_FLUIDSTACK);

    // --------------- Value type propagators ---------------
    public static final IAspectValuePropagator<Boolean, ValueTypeBoolean.ValueBoolean> PROP_GET_BOOLEAN = new IAspectValuePropagator<Boolean, ValueTypeBoolean.ValueBoolean>() {
        @Override
        public ValueTypeBoolean.ValueBoolean getOutput(Boolean input) {
            return ValueTypeBoolean.ValueBoolean.of(input);
        }
    };
    public static final IAspectValuePropagator<Integer, ValueTypeInteger.ValueInteger> PROP_GET_INTEGER = new IAspectValuePropagator<Integer, ValueTypeInteger.ValueInteger>() {
        @Override
        public ValueTypeInteger.ValueInteger getOutput(Integer input) {
            return ValueTypeInteger.ValueInteger.of(input);
        }
    };
    public static final IAspectValuePropagator<Double, ValueTypeDouble.ValueDouble> PROP_GET_DOUBLE = new IAspectValuePropagator<Double, ValueTypeDouble.ValueDouble>() {
        @Override
        public ValueTypeDouble.ValueDouble getOutput(Double input) {
            return ValueTypeDouble.ValueDouble.of(input);
        }
    };
    public static final IAspectValuePropagator<Long, ValueTypeLong.ValueLong> PROP_GET_LONG = new IAspectValuePropagator<Long, ValueTypeLong.ValueLong>() {
        @Override
        public ValueTypeLong.ValueLong getOutput(Long input) {
            return ValueTypeLong.ValueLong.of(input);
        }
    };
    public static final IAspectValuePropagator<ItemStack, ValueObjectTypeItemStack.ValueItemStack> PROP_GET_ITEMSTACK = new IAspectValuePropagator<ItemStack, ValueObjectTypeItemStack.ValueItemStack>() {
        @Override
        public ValueObjectTypeItemStack.ValueItemStack getOutput(ItemStack input) {
            return ValueObjectTypeItemStack.ValueItemStack.of(input);
        }
    };
    public static final IAspectValuePropagator<String, ValueTypeString.ValueString> PROP_GET_STRING = new IAspectValuePropagator<String, ValueTypeString.ValueString>() {
        @Override
        public ValueTypeString.ValueString getOutput(String input) {
            return ValueTypeString.ValueString.of(input);
        }
    };
    public static final IAspectValuePropagator<IBlockState, ValueObjectTypeBlock.ValueBlock> PROP_GET_BLOCK = new IAspectValuePropagator<IBlockState, ValueObjectTypeBlock.ValueBlock>() {
        @Override
        public ValueObjectTypeBlock.ValueBlock getOutput(IBlockState input) {
            return ValueObjectTypeBlock.ValueBlock.of(input);
        }
    };
    public static final IAspectValuePropagator<FluidStack, ValueObjectTypeFluidStack.ValueFluidStack> PROP_GET_FLUIDSTACK = new IAspectValuePropagator<FluidStack, ValueObjectTypeFluidStack.ValueFluidStack>() {
        @Override
        public ValueObjectTypeFluidStack.ValueFluidStack getOutput(FluidStack input) {
            return ValueObjectTypeFluidStack.ValueFluidStack.of(input);
        }
    };

    // --------------- Generic properties ---------------
    public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_LISTINDEX =
            new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.listindex.name");
    public static final IAspectProperties LIST_PROPERTIES = new AspectProperties(Sets.<IAspectPropertyTypeInstance>newHashSet(
            PROPERTY_LISTINDEX
    ));
    static {
        LIST_PROPERTIES.setValue(PROPERTY_LISTINDEX, ValueTypeInteger.ValueInteger.of(0));
    }

    public static final class Audio {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_RANGE =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.range.name");
        public static final IAspectProperties NOTE_PROPERTIES = new AspectProperties(Sets.<IAspectPropertyTypeInstance>newHashSet(
                PROPERTY_RANGE
        ));
        static {
            NOTE_PROPERTIES.setValue(PROPERTY_RANGE, ValueTypeInteger.ValueInteger.of(64));
        }

        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<PartTarget, IAspectProperties>>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.appendKind("audio");

        public static AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Integer> forInstrument(final NoteBlockEvent.Instrument instrument) {
            return BUILDER_INTEGER.appendKind("instrument").handle(new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer>() {
                @Override
                public Integer getOutput(Pair<PartTarget, IAspectProperties> input) throws EvaluationException {
                    for (NoteBlockEvent.Play event : NoteBlockEventReceiver.getInstance().getEvents().get(instrument)) {
                        net.minecraft.world.World world = input.getLeft().getTarget().getPos().getWorld();
                        BlockPos pos = input.getLeft().getTarget().getPos().getBlockPos();
                        int range = input.getRight().getValue(PROPERTY_RANGE).getRawValue();
                        if (world.provider.getDimensionId() == event.world.provider.getDimensionId()
                                && pos.distanceSq(event.pos) <= range * range) {
                            return event.getVanillaNoteId();
                        }
                    }
                    return -1;
                }
            }, instrument.name().toLowerCase(Locale.ENGLISH)).withProperties(NOTE_PROPERTIES);
        }

    }

    public static final class Block {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos>() {
            @Override
            public DimPos getOutput(Pair<PartTarget, IAspectProperties> input) {
                return input.getLeft().getTarget().getPos();
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, DimPos>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "block");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, DimPos>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "block");
        public static final AspectBuilder<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock, DimPos>
                BUILDER_BLOCK = AspectReadBuilders.BUILDER_OBJECT_BLOCK.handle(PROP_GET, "block");

    }

    public static final class Entity {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos>() {
            @Override
            public DimPos getOutput(Pair<PartTarget, IAspectProperties> input) {
                return input.getLeft().getTarget().getPos();
            }
        };

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

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, MinecraftServer> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, MinecraftServer>() {
            @Override
            public MinecraftServer getOutput(Pair<PartTarget, IAspectProperties> input) {
                return MinecraftServer.getServer();
            }
        };

        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, MinecraftServer>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "extradimensional");
        public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, MinecraftServer>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET, "extradimensional");

    }

    public static final class Fluid {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROP_TANKID =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.tankid.name");
        public static final IAspectProperties PROPERTIES = new AspectProperties(Sets.<IAspectPropertyTypeInstance>newHashSet(
                PROP_TANKID
        ));
        static {
            PROPERTIES.setValue(PROP_TANKID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        }

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, FluidTankInfo[]> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, FluidTankInfo[]>() {
            @Override
            public FluidTankInfo[] getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos dimPos = input.getLeft().getTarget().getPos();
                IFluidHandler fluidHandler = TileHelpers.getSafeTile(dimPos, IFluidHandler.class);
                if(fluidHandler != null) {
                    return fluidHandler.getTankInfo(input.getLeft().getTarget().getSide());
                }
                return new FluidTankInfo[0];
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, FluidTankInfo> PROP_GET_ACTIVATABLE = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, FluidTankInfo>() {
            @Override
            public FluidTankInfo getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos dimPos = input.getLeft().getTarget().getPos();
                IFluidHandler fluidHandler = TileHelpers.getSafeTile(dimPos, IFluidHandler.class);
                if(fluidHandler != null) {
                    FluidTankInfo[] tankInfo = fluidHandler.getTankInfo(input.getLeft().getTarget().getSide());
                    int i = input.getRight().getValue(PROP_TANKID).getRawValue();
                    if(tankInfo != null && i < tankInfo.length) {
                        return tankInfo[i];
                    }
                }
                return null;
            }
        };
        public static final IAspectValuePropagator<FluidTankInfo, FluidStack> PROP_GET_FLUIDSTACK = new IAspectValuePropagator<FluidTankInfo, FluidStack>() {
            @Override
            public FluidStack getOutput(FluidTankInfo tankInfo) {
                return tankInfo != null ? tankInfo.fluid : null;
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList> PROP_GET_LIST_FLUIDSTACKS = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList>() {
            @Override
            public ValueTypeList.ValueList getOutput(Pair<PartTarget, IAspectProperties> input) {
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedTankFluidStacks(
                        input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                ));
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList> PROP_GET_LIST_CAPACITIES = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList>() {
            @Override
            public ValueTypeList.ValueList getOutput(Pair<PartTarget, IAspectProperties> input) {
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedTankCapacities(
                        input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                ));
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, FluidTankInfo[]>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "fluid");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, FluidTankInfo[]>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "fluid");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, FluidTankInfo>
                BUILDER_INTEGER_ACTIVATABLE = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);
        public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, FluidTankInfo>
                BUILDER_DOUBLE_ACTIVATABLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);

    }

    public static final class Inventory {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_SLOTID =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.slotid.name");
        public static final IAspectProperties PROPERTIES = new AspectProperties(Sets.<IAspectPropertyTypeInstance>newHashSet(
                PROPERTY_SLOTID
        ));
        static {
            PROPERTIES.setValue(PROPERTY_SLOTID, ValueTypeInteger.ValueInteger.of(0)); // Not required in this case, but we do this here just as an example on how to set default values.
        }

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IItemHandler> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IItemHandler>() {
            @Override
            public IItemHandler getOutput(Pair<PartTarget, IAspectProperties> input) {
                PartPos target = input.getLeft().getTarget();
                return TileHelpers.getCapability(target.getPos().getWorld(), target.getPos().getBlockPos(), target.getSide(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ItemStack> PROP_GET_SLOT = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ItemStack>() {
            @Override
            public ItemStack getOutput(Pair<PartTarget, IAspectProperties> input) {
                PartPos target = input.getLeft().getTarget();
                IItemHandler itemHandler = TileHelpers.getCapability(target.getPos().getWorld(), target.getPos().getBlockPos(), target.getSide(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                int slotId = input.getRight().getValue(PROPERTY_SLOTID).getRawValue();
                if(itemHandler != null && slotId >= 0 && slotId < itemHandler.getSlots()) {
                    return itemHandler.getStackInSlot(slotId);
                }
                return null;
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList> PROP_GET_LIST = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList>() {
            @Override
            public ValueTypeList.ValueList getOutput(Pair<PartTarget, IAspectProperties> input) {
                return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedInventory(input.getLeft().getTarget().getPos()));
            }
        };

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

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IWorker> PROP_GET_WORKER = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IWorker>() {
            @Override
            public IWorker getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos dimPos = input.getLeft().getTarget().getPos();
                return TileHelpers.getCapability(dimPos.getWorld(), dimPos.getBlockPos(), input.getLeft().getTarget().getSide(), Capabilities.WORKER);
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IWorker>
                BUILDER_WORKER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_WORKER, "machine");

    }

    public static final class Network {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetwork> PROP_GET_NETWORK = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetwork>() {
            @Override
            public INetwork getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos dimPos = input.getLeft().getTarget().getPos();
                net.minecraft.block.Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
                if(block instanceof INetworkCarrier) {
                    return((INetworkCarrier) block).getNetwork(dimPos.getWorld(), dimPos.getBlockPos());
                }
                return null;
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, INetwork>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_NETWORK, "network");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, INetwork>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_NETWORK, "network");

    }

    public static final class Redstone {

        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_INTERVAL =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.interval.name");
        public static final IAspectPropertyTypeInstance<ValueTypeInteger, ValueTypeInteger.ValueInteger> PROPERTY_LENGTH =
                new AspectPropertyTypeInstance<>(ValueTypes.INTEGER, "aspect.aspecttypes.integrateddynamics.integer.length.name");
        public static final IAspectProperties PROPERTIES_CLOCK = new AspectProperties(Sets.<IAspectPropertyTypeInstance>newHashSet(
                PROPERTY_INTERVAL,
                PROPERTY_LENGTH
        ));
        static {
            PROPERTIES_CLOCK.setValue(PROPERTY_INTERVAL, ValueTypeInteger.ValueInteger.of(20));
            PROPERTIES_CLOCK.setValue(PROPERTY_LENGTH, ValueTypeInteger.ValueInteger.of(1));
        }

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer>() {
            @Override
            public Integer getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos dimPos = input.getLeft().getTarget().getPos();
                return dimPos.getWorld().getRedstonePower(dimPos.getBlockPos(), input.getLeft().getCenter().getSide());
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer> PROP_GET_COMPARATOR = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Integer>() {
            @Override
            public Integer getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos dimPos = input.getLeft().getTarget().getPos();
                return dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock().getComparatorInputOverride(dimPos.getWorld(), dimPos.getBlockPos());
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Boolean> PROP_GET_CLOCK = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Boolean>() {
            @Override
            public Boolean getOutput(Pair<PartTarget, IAspectProperties> input) {
                int interval = Math.max(1, input.getRight().getValue(PROPERTY_INTERVAL).getRawValue());
                int length = Math.max(1, input.getRight().getValue(PROPERTY_LENGTH).getRawValue());
                /*if(length * 2 > interval) {
                    throw new EvaluationException(String.format("A true and false pulse of length %s do not " +
                            "fit into an interval of %s.", length, interval));
                }*/
                return (input.getLeft().getTarget().getPos().getWorld().getTotalWorldTime() / length) % (interval / length) == 0;
            }
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

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, DimPos>() {
            @Override
            public DimPos getOutput(Pair<PartTarget, IAspectProperties> input) {
                return input.getLeft().getTarget().getPos();
            }
        };
        public static final IAspectValuePropagator<DimPos, net.minecraft.world.World> PROP_GET_WORLD = new IAspectValuePropagator<DimPos, net.minecraft.world.World>() {
            @Override
            public net.minecraft.world.World getOutput(DimPos input) {
                return input.getWorld();
            }
        };
        public static final IAspectValuePropagator<DimPos, BlockPos> PROP_GET_POS = new IAspectValuePropagator<DimPos, BlockPos>() {
            @Override
            public BlockPos getOutput(DimPos input) {
                return input.getBlockPos();
            }
        };
        private static final Predicate<net.minecraft.entity.Entity> ENTITY_SELECTOR_ITEMFRAME = new Predicate<net.minecraft.entity.Entity>() {
            @Override
            public boolean apply(@Nullable net.minecraft.entity.Entity entity) {
                return entity instanceof EntityItemFrame;
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, EntityItemFrame> PROP_GET_ITEMFRAME = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, EntityItemFrame>() {
            @Override
            public EntityItemFrame getOutput(Pair<PartTarget, IAspectProperties> pair) {
                DimPos dimPos = pair.getLeft().getTarget().getPos();
                EnumFacing facing = pair.getLeft().getTarget().getSide();
                List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                        new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), ENTITY_SELECTOR_ITEMFRAME);
                for(net.minecraft.entity.Entity entity : entities) {
                    if(EnumFacing.fromAngle(((EntityItemFrame) entity).rotationYaw) == facing.getOpposite()) {
                        return ((EntityItemFrame) entity);
                    }
                }
                return null;
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, DimPos>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, DimPos>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeLong.ValueLong, ValueTypeLong, DimPos>
                BUILDER_LONG = AspectReadBuilders.BUILDER_LONG.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeString.ValueString, ValueTypeString, DimPos>
                BUILDER_STRING = AspectReadBuilders.BUILDER_STRING.handle(PROP_GET, "world");
        public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, DimPos>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET, "world");

    }

}
