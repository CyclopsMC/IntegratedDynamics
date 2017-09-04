package org.cyclops.integrateddynamics.part.aspect;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.commoncapabilities.api.capability.temperature.ITemperature;
import org.cyclops.commoncapabilities.api.capability.work.IWorker;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.capability.network.EnergyNetworkConfig;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Collection of all aspects.
 * @author rubensworks
 */
public class Aspects {

    public static final IAspectRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IAspectRegistry.class);

    public static void load() {}

    public static final class Read {

        public static final class Audio {

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_PIANO_NOTE =
                    AspectReadBuilders.Audio.forInstrument(NoteBlockEvent.Instrument.PIANO)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER).buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_BASSDRUM_NOTE =
                    AspectReadBuilders.Audio.forInstrument(NoteBlockEvent.Instrument.BASSDRUM)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER).buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_SNARE_NOTE =
                    AspectReadBuilders.Audio.forInstrument(NoteBlockEvent.Instrument.SNARE)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER).buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CLICKS_NOTE =
                    AspectReadBuilders.Audio.forInstrument(NoteBlockEvent.Instrument.CLICKS)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER).buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_BASSGUITAR_NOTE =
                    AspectReadBuilders.Audio.forInstrument(NoteBlockEvent.Instrument.BASSGUITAR)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER).buildRead();

        }

        public static final class Block {
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_BLOCK =
                    AspectReadBuilders.Block.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<DimPos, Boolean>() {
                        @Override
                        public Boolean getOutput(DimPos dimPos) {
                            net.minecraft.block.Block block = dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock();
                            return block != Blocks.AIR;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "block").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_DIMENSION =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Integer>() {
                        @Override
                        public Integer getOutput(net.minecraft.world.World world) {
                            return world.provider.getDimension();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "dimension").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSX =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(new IAspectValuePropagator<BlockPos, Integer>() {
                        @Override
                        public Integer getOutput(BlockPos pos) {
                            return pos.getX();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "posx").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSY =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(new IAspectValuePropagator<BlockPos, Integer>() {
                        @Override
                        public Integer getOutput(BlockPos pos) {
                            return pos.getY();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "posy").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSZ =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(new IAspectValuePropagator<BlockPos, Integer>() {
                        @Override
                        public Integer getOutput(BlockPos pos) {
                            return pos.getZ();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "posz").buildRead();
            public static final IAspectRead<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock> BLOCK =
                    AspectReadBuilders.Block.BUILDER_BLOCK.handle(new IAspectValuePropagator<DimPos, IBlockState>() {
                        @Override
                        public IBlockState getOutput(DimPos dimPos) {
                            return dimPos.getWorld().getBlockState(dimPos.getBlockPos());
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BLOCK).buildRead();
            public static final IAspectRead<ValueTypeNbt.ValueNbt, ValueTypeNbt> NBT =
                    AspectReadBuilders.Block.BUILDER_NBT.handle(new IAspectValuePropagator<DimPos, NBTTagCompound>() {
                        @Override
                        public NBTTagCompound getOutput(DimPos dimPos) {
                            TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
                            try {
                                if (tile != null) {
                                    return tile.writeToNBT(new NBTTagCompound());
                                }
                            } catch (Exception e) {
                                // Catch possible errors
                            }
                            return null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_NBT, "tile").buildRead();
        }

        public static final class Entity {
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ITEMFRAMEROTATION =
                    AspectReadBuilders.Entity.BUILDER_INTEGER_ALL
                            .handle(AspectReadBuilders.World.PROP_GET_ITEMFRAME)
                            .handle(new IAspectValuePropagator<EntityItemFrame, Integer>() {
                                @Override
                                public Integer getOutput(EntityItemFrame itemFrame) {
                                    return itemFrame!= null ? itemFrame.getRotation() : 0;
                                }
                            }).handle(AspectReadBuilders.PROP_GET_INTEGER, "itemframerotation").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ENTITIES =
                    AspectReadBuilders.Entity.BUILDER_LIST.handle(new IAspectValuePropagator<DimPos, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(DimPos dimPos) {
                            List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                                    new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), EntitySelectors.NOT_SPECTATING);
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(entities, new Function<net.minecraft.entity.Entity, ValueObjectTypeEntity.ValueEntity>() {
                                @Nullable
                                @Override
                                public ValueObjectTypeEntity.ValueEntity apply(net.minecraft.entity.Entity input) {
                                    return ValueObjectTypeEntity.ValueEntity.of(input);
                                }
                            }));
                        }
                    }).appendKind("entities").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.Entity.BUILDER_LIST.handle(new IAspectValuePropagator<DimPos, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(DimPos dimPos) {
                            List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                                    new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), Helpers.SELECTOR_IS_PLAYER);
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(entities, new Function<net.minecraft.entity.Entity, ValueObjectTypeEntity.ValueEntity>() {
                                @Nullable
                                @Override
                                public ValueObjectTypeEntity.ValueEntity apply(net.minecraft.entity.Entity input) {
                                    return ValueObjectTypeEntity.ValueEntity.of(input);
                                }
                            }));
                        }
                    }).appendKind("players").buildRead();

            public static final IAspectRead<ValueObjectTypeEntity.ValueEntity, ValueObjectTypeEntity> ENTITY =
                    AspectReadBuilders.Entity.BUILDER_ENTITY.withProperties(AspectReadBuilders.LIST_PROPERTIES).handle(new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueObjectTypeEntity.ValueEntity>() {
                        @Override
                        public ValueObjectTypeEntity.ValueEntity getOutput(Pair<PartTarget, IAspectProperties> input) {
                            int i = input.getRight().getValue(AspectReadBuilders.PROPERTY_LISTINDEX).getRawValue();
                            DimPos dimPos = input.getLeft().getTarget().getPos();
                            List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                                    new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), EntitySelectors.NOT_SPECTATING);
                            return ValueObjectTypeEntity.ValueEntity.of(i < entities.size() ? entities.get(i) : null);
                        }
                    }).buildRead();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> ITEMSTACK_ITEMFRAMECONTENTS =
                    AspectReadBuilders.Entity.BUILDER_ITEMSTACK
                            .handle(AspectReadBuilders.World.PROP_GET_ITEMFRAME)
                            .handle(new IAspectValuePropagator<EntityItemFrame, ItemStack>() {
                                @Override
                                public ItemStack getOutput(EntityItemFrame itemFrame) {
                                    return itemFrame != null ? itemFrame.getDisplayedItem() : ItemStack.EMPTY;
                                }
                            }).handle(AspectReadBuilders.PROP_GET_ITEMSTACK, "itemframecontents").buildRead();
        }

        public static final class ExtraDimensional {

            private static final Random RANDOM = new Random();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_RANDOM =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(new IAspectValuePropagator<MinecraftServer, Integer>() {
                        @Override
                        public Integer getOutput(MinecraftServer minecraft) {
                            return RANDOM.nextInt();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "random").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_PLAYERCOUNT =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(new IAspectValuePropagator<MinecraftServer, Integer>() {
                        @Override
                        public Integer getOutput(MinecraftServer minecraft) {
                            return minecraft.getCurrentPlayerCount();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "playercount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TICKTIME =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(new IAspectValuePropagator<MinecraftServer, Integer>() {
                        @Override
                        public Integer getOutput(MinecraftServer minecraft) {
                            return (int) DoubleMath.mean(minecraft.tickTimeArray);
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "ticktime").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.ExtraDimensional.BUILDER_LIST.handle(new IAspectValuePropagator<MinecraftServer, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(MinecraftServer minecraft) {
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(minecraft.getPlayerList().getPlayers(), new Function<EntityPlayerMP, ValueObjectTypeEntity.ValueEntity>() {
                                @Nullable
                                @Override
                                public ValueObjectTypeEntity.ValueEntity apply(EntityPlayerMP input) {
                                    return ValueObjectTypeEntity.ValueEntity.of(input);
                                }
                            }));
                        }
                    }).appendKind("players").buildRead();

        }

        public static final class Fluid {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_FULL =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IFluidTankProperties[], Boolean>() {
                        @Override
                        public Boolean getOutput(IFluidTankProperties[] tankInfo) {
                            boolean allFull = true;
                            for(IFluidTankProperties tank : tankInfo) {
                                if(tank.getContents() == null && tank.getCapacity() > 0 || (tank.getContents() != null && tank.getContents().amount < tank.getCapacity())) {
                                    allFull = false;
                                }
                            }
                            return allFull;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "full").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_EMPTY =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IFluidTankProperties[], Boolean>() {
                        @Override
                        public Boolean getOutput(IFluidTankProperties[] tankInfo) {
                            for(IFluidTankProperties tank : tankInfo) {
                                if(tank.getContents() != null && tank.getCapacity() > 0 || (tank.getContents() != null && tank.getContents().amount < tank.getCapacity())) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "empty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONEMPTY =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IFluidTankProperties[], Boolean>() {
                        @Override
                        public Boolean getOutput(IFluidTankProperties[] tankInfo) {
                            boolean hasFluid = false;
                            for(IFluidTankProperties tank : tankInfo) {
                                if(tank.getContents() != null && tank.getContents().amount > 0) {
                                    hasFluid = true;
                                }
                            }
                            return hasFluid;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IFluidTankProperties[], Boolean>() {
                        @Override
                        public Boolean getOutput(IFluidTankProperties[] tankInfo) {
                            return tankInfo.length > 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_AMOUNT =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER_ACTIVATABLE.handle(AspectReadBuilders.Fluid.PROP_GET_FLUIDSTACK).handle(new IAspectValuePropagator<FluidStack, Integer>() {
                        @Override
                        public Integer getOutput(FluidStack fluidStack) {
                            return fluidStack != null ? fluidStack.amount : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "amount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_AMOUNTTOTAL =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(new IAspectValuePropagator<IFluidTankProperties[], Integer>() {
                        @Override
                        public Integer getOutput(IFluidTankProperties[] tankInfo) {
                            int amount = 0;
                            for(IFluidTankProperties tank : tankInfo) {
                                if(tank.getContents() != null) {
                                    amount += tank.getContents().amount;
                                }
                            }
                            return amount;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "totalamount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CAPACITY =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER_ACTIVATABLE.handle(new IAspectValuePropagator<IFluidTankProperties, Integer>() {
                        @Override
                        public Integer getOutput(IFluidTankProperties tankInfo) {
                            return tankInfo != null ? tankInfo.getCapacity() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "capacity").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CAPACITYTOTAL =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(new IAspectValuePropagator<IFluidTankProperties[], Integer>() {
                        @Override
                        public Integer getOutput(IFluidTankProperties[] tankInfo) {
                            int capacity = 0;
                            for(IFluidTankProperties tank : tankInfo) {
                                capacity += tank.getCapacity();
                            }
                            return capacity;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "totalamount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TANKS =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(new IAspectValuePropagator<IFluidTankProperties[], Integer>() {
                        @Override
                        public Integer getOutput(IFluidTankProperties[] tankInfo) {
                            return tankInfo.length;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "tanks").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_FILLRATIO =
                    AspectReadBuilders.Fluid.BUILDER_DOUBLE_ACTIVATABLE.handle(new IAspectValuePropagator<IFluidTankProperties, Double>() {
                        @Override
                        public Double getOutput(IFluidTankProperties tankInfo) {
                            if(tankInfo == null) {
                                return 0D;
                            }
                            double amount = tankInfo.getContents() == null ? 0D : tankInfo.getContents().amount;
                            return amount / (double) tankInfo.getCapacity();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_TANKFLUIDS =
                    AspectReadBuilders.BUILDER_LIST.appendKind("fluid").handle(AspectReadBuilders.Fluid.PROP_GET_LIST_FLUIDSTACKS, "fluidstacks").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_TANKCAPACITIES =
                    AspectReadBuilders.BUILDER_LIST.appendKind("fluid").handle(AspectReadBuilders.Fluid.PROP_GET_LIST_CAPACITIES, "capacities").buildRead();

            public static final IAspectRead<ValueObjectTypeFluidStack.ValueFluidStack, ValueObjectTypeFluidStack> FLUIDSTACK =
                    AspectReadBuilders.BUILDER_OBJECT_FLUIDSTACK
                            .handle(AspectReadBuilders.Fluid.PROP_GET_ACTIVATABLE, "fluid").withProperties(AspectReadBuilders.Fluid.PROPERTIES)
                            .handle(AspectReadBuilders.Fluid.PROP_GET_FLUIDSTACK).handle(AspectReadBuilders.PROP_GET_FLUIDSTACK).buildRead();

        }

        public static final class Inventory {
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_FULL =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IItemHandler, Boolean>() {
                        @Override
                        public Boolean getOutput(IItemHandler inventory) {
                            if(inventory != null) {
                                for (int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if (itemStack.isEmpty()) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "full").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_EMPTY =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IItemHandler, Boolean>() {
                        @Override
                        public Boolean getOutput(IItemHandler inventory) {
                            if(inventory != null) {
                                for(int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if(!itemStack.isEmpty()) {
                                        return false;
                                    }
                                }
                            }
                            return true;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "empty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONEMPTY =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IItemHandler, Boolean>() {
                        @Override
                        public Boolean getOutput(IItemHandler inventory) {
                            if(inventory != null) {
                                for(int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if(!itemStack.isEmpty()) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IItemHandler, Boolean>() {
                        @Override
                        public Boolean getOutput(IItemHandler inventory) {
                            return inventory != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_COUNT =
                    AspectReadBuilders.Inventory.BUILDER_INTEGER.handle(new IAspectValuePropagator<IItemHandler, Integer>() {
                        @Override
                        public Integer getOutput(IItemHandler inventory) {
                            int count = 0;
                            if(inventory != null) {
                                for (int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if (!itemStack.isEmpty()) {
                                        count += itemStack.getCount();
                                    }
                                }
                            }
                            return count;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "count").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_SLOTS =
                    AspectReadBuilders.Inventory.BUILDER_INTEGER.handle(new IAspectValuePropagator<IItemHandler, Integer>() {
                        @Override
                        public Integer getOutput(IItemHandler inventory) {
                            return inventory.getSlots();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "slots").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_SLOTSFILLED =
                    AspectReadBuilders.Inventory.BUILDER_INTEGER.handle(new IAspectValuePropagator<IItemHandler, Integer>() {
                        @Override
                        public Integer getOutput(IItemHandler inventory) {
                            int count = 0;
                            if(inventory != null) {
                                for (int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if (!itemStack.isEmpty()) {
                                        count++;
                                    }
                                }
                            }
                            return count;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "slotsfilled").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_FILLRATIO =
                    AspectReadBuilders.Inventory.BUILDER_DOUBLE.handle(new IAspectValuePropagator<IItemHandler, Double>() {
                        @Override
                        public Double getOutput(IItemHandler inventory) {
                            int count = 0;
                            if(inventory != null) {
                                for (int i = 0; i < inventory.getSlots(); i++) {
                                    ItemStack itemStack = inventory.getStackInSlot(i);
                                    if (!itemStack.isEmpty()) {
                                        count++;
                                    }
                                }
                            }
                            return ((double) count) / (double) inventory.getSlots();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ITEMSTACKS =
                    AspectReadBuilders.BUILDER_LIST.appendKind("inventory").handle(AspectReadBuilders.Inventory.PROP_GET_LIST, "itemstacks").buildRead();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> OBJECT_ITEM_STACK_SLOT =
                    AspectReadBuilders.Inventory.BUILDER_ITEMSTACK.handle(AspectReadBuilders.PROP_GET_ITEMSTACK).buildRead();

        }

        public static final class Machine {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISWORKER =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(new IAspectValuePropagator<IWorker, Boolean>() {
                        @Override
                        public Boolean getOutput(IWorker worker) {
                            return worker != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isworker").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HASWORK =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(new IAspectValuePropagator<IWorker, Boolean>() {
                        @Override
                        public Boolean getOutput(IWorker worker) {
                            return worker != null && worker.hasWork();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "haswork").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANWORK =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(new IAspectValuePropagator<IWorker, Boolean>() {
                        @Override
                        public Boolean getOutput(IWorker worker) {
                            return worker != null && worker.canWork();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "canwork").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISWORKING =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(new IAspectValuePropagator<IWorker, Boolean>() {
                        @Override
                        public Boolean getOutput(IWorker worker) {
                            return worker != null && worker.canWork() && worker.hasWork();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isworking").buildRead();

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISTEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_BOOLEAN.handle(new IAspectValuePropagator<ITemperature, Boolean>() {
                        @Override
                        public Boolean getOutput(ITemperature temperature) {
                            return temperature != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "istemperature").buildRead();
            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_TEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_DOUBLE.handle(new IAspectValuePropagator<ITemperature, Double>() {
                        @Override
                        public Double getOutput(ITemperature temperature) {
                            return temperature != null ? temperature.getTemperature() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "temperature").buildRead();
            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_MAXTEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_DOUBLE.handle(new IAspectValuePropagator<ITemperature, Double>() {
                        @Override
                        public Double getOutput(ITemperature temperature) {
                            return temperature != null ? temperature.getMaximumTemperature() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "maxtemperature").buildRead();
            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_MINTEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_DOUBLE.handle(new IAspectValuePropagator<ITemperature, Double>() {
                        @Override
                        public Double getOutput(ITemperature temperature) {
                            return temperature != null ? temperature.getMinimumTemperature() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "mintemperature").buildRead();
            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_DEFAULTTEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_DOUBLE.handle(new IAspectValuePropagator<ITemperature, Double>() {
                        @Override
                        public Double getOutput(ITemperature temperature) {
                            return temperature != null ? temperature.getDefaultTemperature() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "defaulttemperature").buildRead();

            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IEnergyStorage> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IEnergyStorage>() {
                @Override
                public IEnergyStorage getOutput(Pair<PartTarget, IAspectProperties> input) {
                    return EnergyHelpers.getEnergyStorage(input.getLeft().getTarget());
                }
            };

            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IEnergyStorage>
                    BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "fe");
            public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, IEnergyStorage>
                    BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "fe");
            public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, IEnergyStorage>
                    BUILDER_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET, "fe");

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyStorage, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyStorage data) {
                            return data != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYRECEIVER =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyStorage, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyStorage data) {
                            return data != null && data.canReceive();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isreceiver").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYPROVIDER =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyStorage, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyStorage data) {
                            return data != null && data.canExtract();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isprovider").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANEXTRACTENERGY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyStorage, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyStorage data) {
                            return data != null && data.extractEnergy(1, true) == 1;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "canextract").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANINSERTENERGY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyStorage, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyStorage data) {
                            return data != null && data.receiveEnergy(1, true) == 1;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "caninsert").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYFULL =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyStorage, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyStorage data) {
                            return data != null && data.getEnergyStored() == data.getMaxEnergyStored();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isfull").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYEMPTY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyStorage, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyStorage data) {
                            return data != null && data.getEnergyStored() == 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYNONEMPTY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyStorage, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyStorage data) {
                            return data != null && data.getEnergyStored() != 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isnonempty").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGYSTORED =
                    BUILDER_INTEGER.handle(new IAspectValuePropagator<IEnergyStorage, Integer>() {
                        @Override
                        public Integer getOutput(IEnergyStorage data) {
                            return data != null ? data.getEnergyStored() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "amount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGYCAPACITY =
                    BUILDER_INTEGER.handle(new IAspectValuePropagator<IEnergyStorage, Integer>() {
                        @Override
                        public Integer getOutput(IEnergyStorage data) {
                            return data != null ? data.getMaxEnergyStored() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "capacity").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_ENERGYFILLRATIO =
                    BUILDER_DOUBLE.handle(new IAspectValuePropagator<IEnergyStorage, Double>() {
                        @Override
                        public Double getOutput(IEnergyStorage data) {
                            if(data != null) {
                                double capacity = (double) data.getMaxEnergyStored();
                                if(capacity == 0.0D) {
                                    return 0.0D;
                                }
                                return ((double) data.getEnergyStored()) / capacity;
                            }
                            return 0.0D;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

        }

        public static final class Network {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Network.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<INetwork, Boolean>() {
                        @Override
                        public Boolean getOutput(INetwork network) {
                            return network != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ELEMENT_COUNT =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(new IAspectValuePropagator<INetwork, Integer>() {
                        @Override
                        public Integer getOutput(INetwork network) {
                            return network != null ? network.getElements().size() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "elementcount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_BATTERY_COUNT =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(new IAspectValuePropagator<INetwork, Integer>() {
                        @Override
                        public Integer getOutput(INetwork network) {
                            return network != null && network.hasCapability(EnergyNetworkConfig.CAPABILITY) ? network.getCapability(EnergyNetworkConfig.CAPABILITY).getPositions().size() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("batterycount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_STORED =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(new IAspectValuePropagator<INetwork, Integer>() {
                        @Override
                        public Integer getOutput(INetwork network) {
                            return network != null && network.hasCapability(EnergyNetworkConfig.CAPABILITY) ? network.getCapability(EnergyNetworkConfig.CAPABILITY).getEnergyStored() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("stored").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_MAX =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(new IAspectValuePropagator<INetwork, Integer>() {
                        @Override
                        public Integer getOutput(INetwork network) {
                            return network != null && network.hasCapability(EnergyNetworkConfig.CAPABILITY) ? network.getCapability(EnergyNetworkConfig.CAPABILITY).getMaxEnergyStored() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("max").buildRead();

        }

        public static final class Redstone {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_LOW =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Integer, Boolean>() {
                        @Override
                        public Boolean getOutput(Integer input) {
                            return input == 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "low").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONLOW =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Integer, Boolean>() {
                        @Override
                        public Boolean getOutput(Integer input) {
                            return input > 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonlow").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HIGH =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Integer, Boolean>() {
                        @Override
                        public Boolean getOutput(Integer input) {
                            return input == 15;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "high").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CLOCK =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN_CLOCK.handle(AspectReadBuilders.PROP_GET_BOOLEAN, "clock").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_VALUE =
                    AspectReadBuilders.Redstone.BUILDER_INTEGER.handle(AspectReadBuilders.PROP_GET_INTEGER, "value").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_COMPARATOR =
                    AspectReadBuilders.Redstone.BUILDER_INTEGER_COMPARATOR.handle(AspectReadBuilders.PROP_GET_INTEGER, "comparator").buildRead();

        }

        public static final class World {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_CLEAR =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return !world.isRaining();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("clear").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_RAINING =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return world.isRaining();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("raining").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_THUNDER =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return world.isThundering();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("thunder").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISDAY =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return MinecraftHelpers.isDay(world);
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isday").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISNIGHT =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Boolean>() {
                        @Override
                        public Boolean getOutput(net.minecraft.world.World world) {
                            return !MinecraftHelpers.isDay(world);
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isnight").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_RAINCOUNTDOWN =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Integer>() {
                        @Override
                        public Integer getOutput(net.minecraft.world.World world) {
                            return world.getWorldInfo().getRainTime();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "raincountdown").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TICKTIME =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Integer>() {
                        @Override
                        public Integer getOutput(net.minecraft.world.World world) {
                            return (int) DoubleMath.mean(FMLCommonHandler.instance().getMinecraftServerInstance().worldTickTimes.get(world.provider.getDimension()));
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "ticktime").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_DAYTIME =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Integer>() {
                        @Override
                        public Integer getOutput(net.minecraft.world.World world) {
                            return (int) world.getWorldTime() % MinecraftHelpers.MINECRAFT_DAY;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "daytime").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_LIGHTLEVEL =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(new IAspectValuePropagator<DimPos, Integer>() {
                        @Override
                        public Integer getOutput(DimPos dimPos) {
                            return dimPos.getWorld().getLight(dimPos.getBlockPos());
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "lightlevel").buildRead();

            public static final IAspectRead<ValueTypeLong.ValueLong, ValueTypeLong> LONG_TIME =
                    AspectReadBuilders.World.BUILDER_LONG.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Long>() {
                        @Override
                        public Long getOutput(net.minecraft.world.World world) {
                            return world.getWorldTime();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_LONG, "time").buildRead();
            public static final IAspectRead<ValueTypeLong.ValueLong, ValueTypeLong> LONG_TOTALTIME =
                    AspectReadBuilders.World.BUILDER_LONG.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, Long>() {
                        @Override
                        public Long getOutput(net.minecraft.world.World world) {
                            return world.getTotalWorldTime();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_LONG, "totaltime").buildRead();

            public static final IAspectRead<ValueTypeString.ValueString, ValueTypeString> STRING_NAME =
                    AspectReadBuilders.World.BUILDER_STRING.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(new IAspectValuePropagator<net.minecraft.world.World, String>() {
                        @Override
                        public String getOutput(net.minecraft.world.World world) {
                            return world.getWorldInfo().getWorldName();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_STRING, "worldname").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.World.BUILDER_LIST.handle(new IAspectValuePropagator<DimPos, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(DimPos dimPos) {
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(dimPos.getWorld().playerEntities, new Function<EntityPlayer, ValueObjectTypeEntity.ValueEntity>() {
                                @Nullable
                                @Override
                                public ValueObjectTypeEntity.ValueEntity apply(EntityPlayer input) {
                                    return ValueObjectTypeEntity.ValueEntity.of(input);
                                }
                            }));
                        }
                    }).appendKind("players").buildRead();

        }

    }

    public static final class Write {

        public static final class Audio {

            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_PIANO_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.PIANO), "piano")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_BASSDRUM_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.BASSDRUM), "bassdrum")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_SNARE_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.SNARE), "snare")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CLICKS_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.CLICKS), "clicks")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_BASSGUITAR_NOTE =
                    AspectWriteBuilders.Audio.BUILDER_INTEGER_INSTRUMENT
                            .handle(AspectWriteBuilders.Audio.propWithInstrument(NoteBlockEvent.Instrument.BASSGUITAR), "bassguitar")
                            .handle(AspectWriteBuilders.Audio.PROP_SET).buildWrite();

            public static final IAspectWrite<ValueTypeString.ValueString, ValueTypeString> STRING_SOUND =
                    AspectWriteBuilders.Audio.BUILDER_STRING.withProperties(AspectWriteBuilders.Audio.PROPERTIES_SOUND)
                            .handle(new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, String>, Void>() {
                                @Override
                                public Void getOutput(Triple<PartTarget, IAspectProperties, String> input) throws EvaluationException {
                                    IAspectProperties properties = input.getMiddle();
                                    BlockPos pos = input.getLeft().getTarget().getPos().getBlockPos();
                                    if(!StringUtils.isNullOrEmpty(input.getRight())) {
                                        float f = (float) properties.getValue(AspectWriteBuilders.Audio.PROP_FREQUENCY).getRawValue();
                                        float volume = (float) properties.getValue(AspectWriteBuilders.Audio.PROP_VOLUME).getRawValue();
                                        SoundEvent soundEvent = SoundEvent.REGISTRY.getObject(new ResourceLocation(input.getRight()));

                                        if (soundEvent != null) {
                                            World world = input.getLeft().getTarget().getPos().getWorld();
                                            world.playSound(null,
                                                    (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                                                    soundEvent, SoundCategory.RECORDS, volume, f);
                                        }
                                    }
                                    return null;
                                }
                            }, "sound").buildWrite();

        }

        public static final class Effect {

            public static IAspectWrite<ValueTypeDouble.ValueDouble, ValueTypeDouble> createForParticle(final EnumParticleTypes particle) {
                return AspectWriteBuilders.Effect.BUILDER_DOUBLE_PARTICLE.appendKind("particle").appendKind(particle.getParticleName().toLowerCase(Locale.ROOT))
                        .handle(new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Double>, Void>() {
                            @Override
                            public Void getOutput(Triple<PartTarget, IAspectProperties, Double> input) throws EvaluationException {
                                double velocity = input.getRight();
                                if (velocity < 0) {
                                    return null;
                                }

                                IAspectProperties properties = input.getMiddle();
                                PartPos pos = input.getLeft().getTarget();

                                boolean force = properties.getValue(AspectWriteBuilders.Effect.PROP_FORCE).getRawValue();
                                double x = pos.getPos().getBlockPos().getX() + properties.getValue(AspectWriteBuilders.Effect.PROP_OFFSET_X).getRawValue();
                                double y = pos.getPos().getBlockPos().getY() + properties.getValue(AspectWriteBuilders.Effect.PROP_OFFSET_Y).getRawValue();
                                double z = pos.getPos().getBlockPos().getZ() + properties.getValue(AspectWriteBuilders.Effect.PROP_OFFSET_Z).getRawValue();
                                int numberOfParticles = properties.getValue(AspectWriteBuilders.Effect.PROP_PARTICLES).getRawValue();

                                double xDir = properties.getValue(AspectWriteBuilders.Effect.PROP_SPREAD_X).getRawValue();
                                double yDir = properties.getValue(AspectWriteBuilders.Effect.PROP_SPREAD_Y).getRawValue();
                                double zDir = properties.getValue(AspectWriteBuilders.Effect.PROP_SPREAD_Z).getRawValue();

                                int[] aint = new int[particle.getArgumentCount()];
                                for (int i = 0; i < aint.length; i++) {
                                    aint[i] = 0;
                                }
                                ((WorldServer) pos.getPos().getWorld()).spawnParticle(
                                        particle, force, x, y, z, numberOfParticles,
                                        xDir, yDir, zDir, velocity, aint);
                                return null;
                            }
                        }).buildWrite();
            }

        }

        public static final class Redstone {

            public static final IAspectWrite<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN =
                    AspectWriteBuilders.Redstone.BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, Boolean>, Triple<PartTarget, IAspectProperties, Integer>>() {
                        @Override
                        public Triple<PartTarget, IAspectProperties, Integer> getOutput(Triple<PartTarget, IAspectProperties, Boolean> input) throws EvaluationException {
                            return Triple.of(input.getLeft(), input.getMiddle(), input.getRight() ? 15 : 0);
                        }
                    }).handle(AspectWriteBuilders.Redstone.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER =
                    AspectWriteBuilders.Redstone.BUILDER_INTEGER.handle(AspectWriteBuilders.Redstone.PROP_SET).buildWrite();

        }

    }

}
