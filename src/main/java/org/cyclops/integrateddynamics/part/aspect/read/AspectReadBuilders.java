package org.cyclops.integrateddynamics.part.aspect.read;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectReadBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Collection of aspect read builders and value propagators.
 * @author rubensworks
 */
public class AspectReadBuilders {

    // --------------- Value type builders ---------------
    public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Pair<PartTarget, IAspectProperties>>
            BUILDER_BOOLEAN = AspectReadBuilder.forType(ValueTypes.BOOLEAN);
    public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<PartTarget, IAspectProperties>>
            BUILDER_INTEGER = AspectReadBuilder.forType(ValueTypes.INTEGER);
    public static final AspectReadBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, Pair<PartTarget, IAspectProperties>>
            BUILDER_DOUBLE = AspectReadBuilder.forType(ValueTypes.DOUBLE);
    public static final AspectReadBuilder<ValueTypeLong.ValueLong, ValueTypeLong, Pair<PartTarget, IAspectProperties>>
            BUILDER_LONG = AspectReadBuilder.forType(ValueTypes.LONG);
    public static final AspectReadBuilder<ValueTypeString.ValueString, ValueTypeString, Pair<PartTarget, IAspectProperties>>
            BUILDER_STRING = AspectReadBuilder.forType(ValueTypes.STRING);
    public static final AspectReadBuilder<ValueTypeList.ValueList, ValueTypeList, Pair<PartTarget, IAspectProperties>>
            BUILDER_LIST = AspectReadBuilder.forType(ValueTypes.LIST);

    public static final AspectReadBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, Pair<PartTarget, IAspectProperties>>
            BUILDER_OBJECT_ITEMSTACK = AspectReadBuilder.forType(ValueTypes.OBJECT_ITEMSTACK);
    public static final AspectReadBuilder<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock, Pair<PartTarget, IAspectProperties>>
            BUILDER_OBJECT_BLOCK = AspectReadBuilder.forType(ValueTypes.OBJECT_BLOCK);

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

    public static final class Redstone {

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

        public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Integer>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "redstone");
        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Integer>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "redstone");
        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Integer>
                BUILDER_INTEGER_COMPARATOR = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_COMPARATOR, "redstone");

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

        public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IItemHandler>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "inventory");
        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, IItemHandler>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "inventory");
        public static final AspectReadBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, ItemStack>
                BUILDER_ITEMSTACK = BUILDER_OBJECT_ITEMSTACK.handle(PROP_GET_SLOT, "inventory").withProperties(PROPERTIES);

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
        private static final Predicate<Entity> ENTITY_SELECTOR_ITEMFRAME = new Predicate<Entity>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                return entity instanceof EntityItemFrame;
            }
        };
        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, EntityItemFrame> PROP_GET_ITEMFRAME = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, EntityItemFrame>() {
            @Override
            public EntityItemFrame getOutput(Pair<PartTarget, IAspectProperties> pair) {
                DimPos dimPos = pair.getLeft().getTarget().getPos();
                EnumFacing facing = pair.getLeft().getTarget().getSide();
                List<Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                        new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), ENTITY_SELECTOR_ITEMFRAME);
                for(Entity entity : entities) {
                    if(EnumFacing.fromAngle(((EntityItemFrame) entity).rotationYaw) == facing.getOpposite()) {
                        return ((EntityItemFrame) entity);
                    }
                }
                return null;
            }
        };

        public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, DimPos>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "world");
        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, DimPos>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "world");
        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<PartTarget, IAspectProperties>>
                BUILDER_INTEGER_ALL = AspectReadBuilders.BUILDER_INTEGER.appendKind("world");
        public static final AspectReadBuilder<ValueTypeList.ValueList, ValueTypeList, DimPos>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET, "world");
        public static final AspectReadBuilder<ValueTypeLong.ValueLong, ValueTypeLong, DimPos>
                BUILDER_LONG = AspectReadBuilders.BUILDER_LONG.handle(PROP_GET, "world");
        public static final AspectReadBuilder<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock, DimPos>
                BUILDER_BLOCK = AspectReadBuilders.BUILDER_OBJECT_BLOCK.handle(PROP_GET, "world");
        public static final AspectReadBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, Pair<PartTarget, IAspectProperties>>
                BUILDER_ITEMSTACK = AspectReadBuilders.BUILDER_OBJECT_ITEMSTACK.appendKind("world");
        public static final AspectReadBuilder<ValueTypeString.ValueString, ValueTypeString, DimPos>
                BUILDER_STRING = AspectReadBuilders.BUILDER_STRING.handle(PROP_GET, "world");

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

        public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, FluidTankInfo[]>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "fluid");
        public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, FluidTankInfo>
                BUILDER_BOOLEAN_ACTIVATABLE = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);
        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, FluidTankInfo[]>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "fluid");
        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, FluidTankInfo>
                BUILDER_INTEGER_ACTIVATABLE = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);
        public static final AspectReadBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, FluidTankInfo[]>
                BUILDER_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET, "fluid");
        public static final AspectReadBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, FluidTankInfo>
                BUILDER_DOUBLE_ACTIVATABLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);
        public static final AspectReadBuilder<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock, FluidTankInfo[]>
                BUILDER_BLOCK = AspectReadBuilders.BUILDER_OBJECT_BLOCK.handle(PROP_GET, "fluid");
        public static final AspectReadBuilder<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock, FluidTankInfo>
                BUILDER_BLOCK_ACTIVATABLE = AspectReadBuilders.BUILDER_OBJECT_BLOCK.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);
        public static final AspectReadBuilder<ValueTypeString.ValueString, ValueTypeString, FluidTankInfo[]>
                BUILDER_STRING = AspectReadBuilders.BUILDER_STRING.handle(PROP_GET, "fluid");
        public static final AspectReadBuilder<ValueTypeString.ValueString, ValueTypeString, FluidTankInfo>
                BUILDER_STRING_ACTIVATABLE = AspectReadBuilders.BUILDER_STRING.handle(PROP_GET_ACTIVATABLE, "fluid").withProperties(PROPERTIES);

    }

    public static final class Minecraft {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, MinecraftServer> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, MinecraftServer>() {
            @Override
            public MinecraftServer getOutput(Pair<PartTarget, IAspectProperties> input) {
                return MinecraftServer.getServer();
            }
        };

        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, MinecraftServer>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "minecraft");
        public static final AspectReadBuilder<ValueTypeList.ValueList, ValueTypeList, MinecraftServer>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET, "minecraft");

    }

    public static final class Network {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetwork> PROP_GET_NETWORK = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetwork>() {
            @Override
            public INetwork getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos dimPos = input.getLeft().getTarget().getPos();
                Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
                if(block instanceof INetworkCarrier) {
                    return((INetworkCarrier) block).getNetwork(dimPos.getWorld(), dimPos.getBlockPos());
                }
                return null;
            }
        };

        public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, INetwork>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_NETWORK, "network");
        public static final AspectReadBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, INetwork>
                BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET_NETWORK, "network");

    }

}
