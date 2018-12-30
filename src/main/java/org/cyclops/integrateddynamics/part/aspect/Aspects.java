package org.cyclops.integrateddynamics.part.aspect;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.google.common.math.Stats;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.network.IEnergyConsumingNetworkElement;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.AspectUpdateType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.capability.network.EnergyNetworkConfig;
import org.cyclops.integrateddynamics.capability.valueinterface.ValueInterfaceConfig;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.operator.PositionedOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.PositionedOperatorRecipeHandlerInputs;
import org.cyclops.integrateddynamics.core.evaluate.operator.PositionedOperatorRecipeHandlerOutput;
import org.cyclops.integrateddynamics.core.evaluate.operator.PositionedOperatorRecipeHandlerRecipeByInput;
import org.cyclops.integrateddynamics.core.evaluate.operator.PositionedOperatorRecipeHandlerRecipeByOutput;
import org.cyclops.integrateddynamics.core.evaluate.operator.PositionedOperatorRecipeHandlerRecipesByInput;
import org.cyclops.integrateddynamics.core.evaluate.operator.PositionedOperatorRecipeHandlerRecipesByOutput;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.EnergyHelpers;
import org.cyclops.integrateddynamics.core.helper.Helpers;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
                    AspectReadBuilders.Block.BUILDER_BOOLEAN.handle(
                        dimPos -> dimPos.getWorld().getBlockState(dimPos.getBlockPos()).getBlock() != Blocks.AIR
                    ).withUpdateType(AspectUpdateType.BLOCK_UPDATE)
                            .handle(AspectReadBuilders.PROP_GET_BOOLEAN, "block").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_DIMENSION =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        world -> world.provider.getDimension()
                    ).withUpdateType(AspectUpdateType.NEVER)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER, "dimension").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSX =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(
                        BlockPos::getX
                    ).withUpdateType(AspectUpdateType.NEVER)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER, "posx").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSY =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(
                        BlockPos::getY
                    ).withUpdateType(AspectUpdateType.NEVER)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER, "posy").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_POSZ =
                    AspectReadBuilders.Block.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_POS).handle(
                        BlockPos::getZ
                    ).withUpdateType(AspectUpdateType.NEVER)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER, "posz").buildRead();
            public static final IAspectRead<ValueObjectTypeBlock.ValueBlock, ValueObjectTypeBlock> BLOCK =
                    AspectReadBuilders.Block.BUILDER_BLOCK
                            .handle(
                        dimPos -> dimPos.getWorld().getBlockState(dimPos.getBlockPos())
                    ).withUpdateType(AspectUpdateType.BLOCK_UPDATE)
                            .handle(AspectReadBuilders.PROP_GET_BLOCK).buildRead();
            public static final IAspectRead<ValueTypeNbt.ValueNbt, ValueTypeNbt> NBT =
                    AspectReadBuilders.Block.BUILDER_NBT.handle(dimPos -> {
                        TileEntity tile = dimPos.getWorld().getTileEntity(dimPos.getBlockPos());
                        try {
                            if (tile != null) {
                                return tile.writeToNBT(new NBTTagCompound());
                            }
                        } catch (Exception e) {
                            // Catch possible errors
                        }
                        return null;
                    }).handle(AspectReadBuilders.PROP_GET_NBT, "tile").buildRead();
        }

        public static final class Entity {
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ITEMFRAMEROTATION =
                    AspectReadBuilders.Entity.BUILDER_INTEGER_ALL
                            .handle(AspectReadBuilders.World.PROP_GET_ITEMFRAME)
                            .handle(itemFrame -> itemFrame != null ? itemFrame.getRotation() : 0)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER, "itemframerotation").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ENTITIES =
                    AspectReadBuilders.Entity.BUILDER_LIST.handle(dimPos -> {
                        List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                                new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), EntitySelectors.NOT_SPECTATING);
                        return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(entities,
                            ValueObjectTypeEntity.ValueEntity::of
                        ));
                    }).appendKind("entities").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.Entity.BUILDER_LIST.handle(dimPos -> {
                        List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                                new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), Helpers.SELECTOR_IS_PLAYER);
                        return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(entities,
                            ValueObjectTypeEntity.ValueEntity::of
                        ));
                    }).appendKind("players").buildRead();

            public static final IAspectRead<ValueObjectTypeEntity.ValueEntity, ValueObjectTypeEntity> ENTITY =
                    AspectReadBuilders.Entity.BUILDER_ENTITY.withProperties(AspectReadBuilders.LIST_PROPERTIES).handle(input -> {
                        int i = input.getRight().getValue(AspectReadBuilders.PROPERTY_LISTINDEX).getRawValue();
                        DimPos dimPos = input.getLeft().getTarget().getPos();
                        List<net.minecraft.entity.Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                                new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), EntitySelectors.NOT_SPECTATING);
                        return ValueObjectTypeEntity.ValueEntity.of(i < entities.size() ? entities.get(i) : null);
                    }).buildRead();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> ITEMSTACK_ITEMFRAMECONTENTS =
                    AspectReadBuilders.Entity.BUILDER_ITEMSTACK
                            .handle(AspectReadBuilders.World.PROP_GET_ITEMFRAME)
                            .handle(itemFrame -> itemFrame != null ? itemFrame.getDisplayedItem() : ItemStack.EMPTY)
                            .handle(AspectReadBuilders.PROP_GET_ITEMSTACK, "itemframecontents").buildRead();
        }

        public static final class ExtraDimensional {

            private static final Random RANDOM = new Random();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_RANDOM =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(
                        minecraft -> RANDOM.nextInt()
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "random").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_PLAYERCOUNT =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(
                        MinecraftServer::getCurrentPlayerCount
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "playercount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TICKTIME =
                    AspectReadBuilders.ExtraDimensional.BUILDER_INTEGER.handle(
                        minecraft -> (int) DoubleMath.mean(minecraft.tickTimeArray)
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "ticktime").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_TPS =
                    AspectReadBuilders.ExtraDimensional.BUILDER_DOUBLE.handle(
                            minecraft -> Math.min(20, Stats.meanOf(minecraft.tickTimeArray) / 1000)
                    ).handle(AspectReadBuilders.PROP_GET_DOUBLE, "tps").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.ExtraDimensional.BUILDER_LIST.handle(
                        minecraft -> ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(minecraft.getPlayerList().getPlayers(),
                                ValueObjectTypeEntity.ValueEntity::of))
                    ).appendKind("players").buildRead();

        }

        public static final class Fluid {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_FULL =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(tankInfo -> {
                        boolean allFull = true;
                        for(IFluidTankProperties tank : tankInfo) {
                            if(tank.getContents() == null && tank.getCapacity() > 0 || (tank.getContents() != null && tank.getContents().amount < tank.getCapacity())) {
                                allFull = false;
                            }
                        }
                        return allFull;
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "full").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_EMPTY =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(tankInfo -> {
                        for(IFluidTankProperties tank : tankInfo) {
                            if(tank.getContents() != null && tank.getCapacity() > 0 || (tank.getContents() != null && tank.getContents().amount < tank.getCapacity())) {
                                return false;
                            }
                        }
                        return true;
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "empty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONEMPTY =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(tankInfo -> {
                        boolean hasFluid = false;
                        for(IFluidTankProperties tank : tankInfo) {
                            if(tank.getContents() != null && tank.getContents().amount > 0) {
                                hasFluid = true;
                            }
                        }
                        return hasFluid;
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Fluid.BUILDER_BOOLEAN.handle(
                        tankInfo -> tankInfo.length > 0
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_AMOUNT =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER_ACTIVATABLE.handle(AspectReadBuilders.Fluid.PROP_GET_FLUIDSTACK).handle(
                        fluidStack -> fluidStack != null ? fluidStack.amount : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "amount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_AMOUNTTOTAL =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(tankInfo -> {
                        int amount = 0;
                        for(IFluidTankProperties tank : tankInfo) {
                            if(tank.getContents() != null) {
                                amount += tank.getContents().amount;
                            }
                        }
                        return amount;
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "totalamount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CAPACITY =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER_ACTIVATABLE.handle(
                        tankInfo -> tankInfo != null ? tankInfo.getCapacity() : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "capacity").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CAPACITYTOTAL =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(tankInfo -> {
                        int capacity = 0;
                        for(IFluidTankProperties tank : tankInfo) {
                            capacity += tank.getCapacity();
                        }
                        return capacity;
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "totalcapacity").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TANKS =
                    AspectReadBuilders.Fluid.BUILDER_INTEGER.handle(
                        tankInfo -> tankInfo.length
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "tanks").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_FILLRATIO =
                    AspectReadBuilders.Fluid.BUILDER_DOUBLE_ACTIVATABLE.handle(tankInfo -> {
                        if(tankInfo == null) {
                            return 0D;
                        }
                        double amount = tankInfo.getContents() == null ? 0D : tankInfo.getContents().amount;
                        return amount / (double) tankInfo.getCapacity();
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_TANKFLUIDS =
                    AspectReadBuilders.BUILDER_LIST.appendKind("fluid").handle(AspectReadBuilders.Fluid.PROP_GET_LIST_FLUIDSTACKS, "fluidstacks").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_TANKCAPACITIES =
                    AspectReadBuilders.BUILDER_LIST.appendKind("fluid").handle(AspectReadBuilders.Fluid.PROP_GET_LIST_CAPACITIES, "capacities").buildRead();

            public static final IAspectRead<ValueObjectTypeFluidStack.ValueFluidStack, ValueObjectTypeFluidStack> FLUIDSTACK =
                    AspectReadBuilders.BUILDER_OBJECT_FLUIDSTACK
                            .handle(AspectReadBuilders.Fluid.PROP_GET_ACTIVATABLE, "fluid").withProperties(AspectReadBuilders.Fluid.PROPERTIES)
                            .handle(AspectReadBuilders.Fluid.PROP_GET_FLUIDSTACK).handle(AspectReadBuilders.PROP_GET_FLUIDSTACK).buildRead();

            public static final IAspectRead<ValueObjectTypeFluidStack.ValueFluidStack, ValueObjectTypeFluidStack> BLOCK =
                    AspectReadBuilders.BUILDER_OBJECT_FLUIDSTACK
                            .handle(AspectReadBuilders.Block.PROP_GET, "block")
                            .handle(dimPos -> {
                                IBlockState blockState = dimPos.getWorld().getBlockState(dimPos.getBlockPos());
                                net.minecraft.block.Block block = blockState.getBlock();
                                if (block instanceof IFluidBlock) {
                                    return ((IFluidBlock) block).drain(dimPos.getWorld(), dimPos.getBlockPos(), false);
                                }
                                if (block instanceof BlockLiquid) {
                                    return new BlockLiquidWrapper((BlockLiquid) block, dimPos.getWorld(), dimPos.getBlockPos()).drain(Integer.MAX_VALUE, false);
                                }
                                return null;
                            })
                            .handle(AspectReadBuilders.PROP_GET_FLUIDSTACK).buildRead();

        }

        public static final class Inventory {
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_FULL =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(inventory -> {
                        if(inventory != null) {
                            for (int i = 0; i < inventory.getSlots(); i++) {
                                ItemStack itemStack = inventory.getStackInSlot(i);
                                if (itemStack.isEmpty()) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "full").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_EMPTY =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(inventory -> {
                        if(inventory != null) {
                            for(int i = 0; i < inventory.getSlots(); i++) {
                                ItemStack itemStack = inventory.getStackInSlot(i);
                                if(!itemStack.isEmpty()) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "empty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONEMPTY =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(inventory -> {
                        if(inventory != null) {
                            for(int i = 0; i < inventory.getSlots(); i++) {
                                ItemStack itemStack = inventory.getStackInSlot(i);
                                if(!itemStack.isEmpty()) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Inventory.BUILDER_BOOLEAN.handle(
                        Objects::nonNull
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_COUNT =
                    AspectReadBuilders.Inventory.BUILDER_INTEGER.handle(inventory -> {
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
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "count").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_SLOTS =
                    AspectReadBuilders.Inventory.BUILDER_INTEGER.handle(
                        inventory -> inventory != null ? inventory.getSlots() : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "slots").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_SLOTSFILLED =
                    AspectReadBuilders.Inventory.BUILDER_INTEGER.handle(inventory -> {
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
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "slotsfilled").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_FILLRATIO =
                    AspectReadBuilders.Inventory.BUILDER_DOUBLE.handle(inventory -> {
                        int count = 0;
                        if(inventory != null) {
                            for (int i = 0; i < inventory.getSlots(); i++) {
                                ItemStack itemStack = inventory.getStackInSlot(i);
                                if (!itemStack.isEmpty()) {
                                    count++;
                                }
                            }
                        }
                        return ((double) count) / (double) (inventory != null ? inventory.getSlots() : 1);
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ITEMSTACKS =
                    AspectReadBuilders.BUILDER_LIST.appendKind("inventory")
                            .handle(AspectReadBuilders.Inventory.PROP_GET_LIST, "itemstacks").buildRead();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> OBJECT_ITEM_STACK_SLOT =
                    AspectReadBuilders.Inventory.BUILDER_ITEMSTACK.handle(AspectReadBuilders.PROP_GET_ITEMSTACK).buildRead();

        }

        public static final class Machine {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISWORKER =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(
                        Objects::nonNull
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isworker").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HASWORK =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(
                        worker -> worker != null && worker.hasWork()
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "haswork").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANWORK =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(
                        worker -> worker != null && worker.canWork()
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "canwork").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISWORKING =
                    AspectReadBuilders.Machine.BUILDER_WORKER_BOOLEAN.handle(
                        worker -> worker != null && worker.canWork() && worker.hasWork()
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isworking").buildRead();

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISTEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_BOOLEAN.handle(
                        Objects::nonNull
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "istemperature").buildRead();
            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_TEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_DOUBLE.handle(
                        temperature -> temperature != null ? temperature.getTemperature() : 0
                    ).handle(AspectReadBuilders.PROP_GET_DOUBLE, "temperature").buildRead();
            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_MAXTEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_DOUBLE.handle(
                        temperature -> temperature != null ? temperature.getMaximumTemperature() : 0
                    ).handle(AspectReadBuilders.PROP_GET_DOUBLE, "maxtemperature").buildRead();
            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_MINTEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_DOUBLE.handle(
                        temperature -> temperature != null ? temperature.getMinimumTemperature() : 0
                    ).handle(AspectReadBuilders.PROP_GET_DOUBLE, "mintemperature").buildRead();
            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_DEFAULTTEMPERATURE =
                    AspectReadBuilders.Machine.BUILDER_TEMPERATURE_DOUBLE.handle(
                        temperature -> temperature != null ? temperature.getDefaultTemperature() : 0
                    ).handle(AspectReadBuilders.PROP_GET_DOUBLE, "defaulttemperature").buildRead();

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISRECIPEHANDLER =
                    AspectReadBuilders.Machine.BUILDER_RECIPE_HANDLER_BOOLEAN
                            .handle(Objects::nonNull)
                            .handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();
            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_GETRECIPES =
                    AspectReadBuilders.Machine.BUILDER_RECIPE_HANDLER_LIST.handle(
                            input -> ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedRecipes(
                                    input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()))).appendKind("recipes").buildRead();
            public static final IAspectRead<ValueTypeOperator.ValueOperator, ValueTypeOperator> OPERATOR_GETRECIPEOUTPUT =
                    AspectReadBuilders.Machine.BUILDER_RECIPE_HANDLER_OPERATOR.handle(
                            input -> ValueTypeOperator.ValueOperator.of(new PositionedOperatorRecipeHandlerOutput<>(
                                    input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                            ))).appendKind("recipeoutputbyinput").buildRead();
            static {
                Operators.REGISTRY.registerSerializer(new PositionedOperator.Serializer(
                        PositionedOperatorRecipeHandlerOutput.class, "positionedRecipeHandlerOutput"));
            }
            public static final IAspectRead<ValueTypeOperator.ValueOperator, ValueTypeOperator> OPERATOR_GETRECIPEINPUTS =
                    AspectReadBuilders.Machine.BUILDER_RECIPE_HANDLER_OPERATOR.handle(input ->
                            ValueTypeOperator.ValueOperator.of(new PositionedOperatorRecipeHandlerInputs<>(
                                    input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                            ))).appendKind("recipeinputsbyoutput").buildRead();
            static {
                Operators.REGISTRY.registerSerializer(new PositionedOperator.Serializer(
                        PositionedOperatorRecipeHandlerInputs.class, "positionedRecipeHandlerInputs"));
            }
            public static final IAspectRead<ValueTypeOperator.ValueOperator, ValueTypeOperator> OPERATOR_GETRECIPESBYINPUT =
                    AspectReadBuilders.Machine.BUILDER_RECIPE_HANDLER_OPERATOR.handle(
                            input -> ValueTypeOperator.ValueOperator.of(new PositionedOperatorRecipeHandlerRecipesByInput<>(
                                    input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                            ))).appendKind("recipesbyinput").buildRead();
            static {
                Operators.REGISTRY.registerSerializer(new PositionedOperator.Serializer(
                        PositionedOperatorRecipeHandlerRecipesByInput.class, "positionedRecipeHandlerRecipesByInput"));
            }
            public static final IAspectRead<ValueTypeOperator.ValueOperator, ValueTypeOperator> OPERATOR_GETRECIPESBYOUTPUT =
                    AspectReadBuilders.Machine.BUILDER_RECIPE_HANDLER_OPERATOR.handle(
                            input -> ValueTypeOperator.ValueOperator.of(new PositionedOperatorRecipeHandlerRecipesByOutput<>(
                                    input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                            ))).appendKind("recipesbyoutput").buildRead();
            static {
                Operators.REGISTRY.registerSerializer(new PositionedOperator.Serializer(
                        PositionedOperatorRecipeHandlerRecipesByOutput.class, "positionedRecipeHandlerRecipesByOutput"));
            }
            public static final IAspectRead<ValueTypeOperator.ValueOperator, ValueTypeOperator> OPERATOR_GETRECIPEBYINPUT =
                    AspectReadBuilders.Machine.BUILDER_RECIPE_HANDLER_OPERATOR.handle(
                            input -> ValueTypeOperator.ValueOperator.of(new PositionedOperatorRecipeHandlerRecipeByInput<>(
                                    input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                            ))).appendKind("recipebyinput").buildRead();
            static {
                Operators.REGISTRY.registerSerializer(new PositionedOperator.Serializer(
                        PositionedOperatorRecipeHandlerRecipeByInput.class, "positionedRecipeHandlerRecipeByInput"));
            }
            public static final IAspectRead<ValueTypeOperator.ValueOperator, ValueTypeOperator> OPERATOR_GETRECIPEBYOUTPUT =
                    AspectReadBuilders.Machine.BUILDER_RECIPE_HANDLER_OPERATOR.handle(
                            input -> ValueTypeOperator.ValueOperator.of(new PositionedOperatorRecipeHandlerRecipeByOutput<>(
                                    input.getLeft().getTarget().getPos(), input.getLeft().getTarget().getSide()
                            ))).appendKind("recipebyoutput").buildRead();
            static {
                Operators.REGISTRY.registerSerializer(new PositionedOperator.Serializer(
                        PositionedOperatorRecipeHandlerRecipeByOutput.class, "positionedRecipeHandlerRecipeByOutput"));
            }

            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IEnergyStorage>
                    PROP_GET = input -> EnergyHelpers.getEnergyStorage(input.getLeft().getTarget());

            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IEnergyStorage>
                    BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "fe");
            public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, IEnergyStorage>
                    BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "fe");
            public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, IEnergyStorage>
                    BUILDER_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET, "fe");

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGY =
                    BUILDER_BOOLEAN.handle(
                        Objects::nonNull
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYRECEIVER =
                    BUILDER_BOOLEAN.handle(
                        data -> data != null && data.canReceive()
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isreceiver").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYPROVIDER =
                    BUILDER_BOOLEAN.handle(
                        data -> data != null && data.canExtract()
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isprovider").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANEXTRACTENERGY =
                    BUILDER_BOOLEAN.handle(
                        data -> data != null && data.extractEnergy(1, true) == 1
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "canextract").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANINSERTENERGY =
                    BUILDER_BOOLEAN.handle(
                        data -> data != null && data.receiveEnergy(1, true) == 1
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "caninsert").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYFULL =
                    BUILDER_BOOLEAN.handle(
                        data -> data != null && data.getEnergyStored() == data.getMaxEnergyStored()
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isfull").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYEMPTY =
                    BUILDER_BOOLEAN.handle(
                        data -> data != null && data.getEnergyStored() == 0
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISENERGYNONEMPTY =
                    BUILDER_BOOLEAN.handle(
                        data -> data != null && data.getEnergyStored() != 0
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isnonempty").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGYSTORED =
                    BUILDER_INTEGER.handle(
                        data -> data != null ? data.getEnergyStored() : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "amount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGYCAPACITY =
                    BUILDER_INTEGER.handle(
                        data -> data != null ? data.getMaxEnergyStored() : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "capacity").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_ENERGYFILLRATIO =
                    BUILDER_DOUBLE.handle(data -> {
                        if(data != null) {
                            double capacity = (double) data.getMaxEnergyStored();
                            if(capacity == 0.0D) {
                                return 0.0D;
                            }
                            return ((double) data.getEnergyStored()) / capacity;
                        }
                        return 0.0D;
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

        }

        public static final class Network {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    AspectReadBuilders.Network.BUILDER_BOOLEAN.handle(
                        Objects::nonNull
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ELEMENT_COUNT =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(
                        network -> network != null ? network.getElements().size() : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "elementcount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_BATTERY_COUNT =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(
                        network -> network != null && network.hasCapability(EnergyNetworkConfig.CAPABILITY) ? network.getCapability(EnergyNetworkConfig.CAPABILITY).getPositions().size() : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("batterycount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_STORED =
                    AspectReadBuilders.Network.ENERGY_BUILDER.handle(
                        storage -> storage != null ? storage.getEnergyStored() : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("stored").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_MAX =
                    AspectReadBuilders.Network.ENERGY_BUILDER.handle(
                        storage -> storage != null ? storage.getMaxEnergyStored() : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("max").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_ENERGY_CONSUMPTION_RATE =
                    AspectReadBuilders.Network.BUILDER_INTEGER.handle(
                            network -> network != null && GeneralConfig.energyConsumptionMultiplier > 0
                                    ? network.getElements().stream()
                                    .mapToInt((e) -> e instanceof IEnergyConsumingNetworkElement
                                            ? ((IEnergyConsumingNetworkElement) e).getConsumptionRate() : 0).sum()
                                    * GeneralConfig.energyConsumptionMultiplier : 0
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "energy").appendKind("consumptionrate").buildRead();
            public static final IAspectRead<IValue, ValueTypeCategoryAny> ANY_VALUE =
                    AspectReadBuilders.BUILDER_ANY.appendKind("network").handle(
                            data -> {
                                PartPos target = data.getLeft().getTarget();
                                IValueInterface valueInterface = TileHelpers.getCapability(
                                        target.getPos(), target.getSide(), ValueInterfaceConfig.CAPABILITY);
                                if (valueInterface != null) {
                                    return valueInterface.getValue().orElseThrow(() ->
                                            new EvaluationException("No valid value interface value was found."));
                                }
                                throw new EvaluationException("No valid value interface was found.");
                            }
                    ).appendKind("value").buildRead();

        }

        public static final class Redstone {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_LOW =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(
                        input -> input == 0
                    ).withUpdateType(AspectUpdateType.BLOCK_UPDATE)
                            .handle(AspectReadBuilders.PROP_GET_BOOLEAN, "low").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_NONLOW =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(
                        input -> input > 0
                    ).withUpdateType(AspectUpdateType.BLOCK_UPDATE)
                            .handle(AspectReadBuilders.PROP_GET_BOOLEAN, "nonlow").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HIGH =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN.handle(
                        input -> input == 15
                    ).withUpdateType(AspectUpdateType.BLOCK_UPDATE)
                            .handle(AspectReadBuilders.PROP_GET_BOOLEAN, "high").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CLOCK =
                    AspectReadBuilders.Redstone.BUILDER_BOOLEAN_CLOCK.handle(AspectReadBuilders.PROP_GET_BOOLEAN, "clock").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_VALUE =
                    AspectReadBuilders.Redstone.BUILDER_INTEGER.withUpdateType(AspectUpdateType.BLOCK_UPDATE)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER, "value").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_COMPARATOR =
                    AspectReadBuilders.Redstone.BUILDER_INTEGER_COMPARATOR.withUpdateType(AspectUpdateType.BLOCK_UPDATE)
                            .handle(AspectReadBuilders.PROP_GET_INTEGER, "comparator").buildRead();

        }

        public static final class World {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_CLEAR =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        world -> !world.isRaining()
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("clear").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_RAINING =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        net.minecraft.world.World::isRaining
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("raining").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_WEATHER_THUNDER =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        net.minecraft.world.World::isThundering
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "weather").appendKind("thunder").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISDAY =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        MinecraftHelpers::isDay
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isday").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISNIGHT =
                    AspectReadBuilders.World.BUILDER_BOOLEAN.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        world -> !MinecraftHelpers.isDay(world)
                    ).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isnight").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_RAINCOUNTDOWN =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        world -> world.getWorldInfo().getRainTime()
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "raincountdown").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_TICKTIME =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        world -> (int) DoubleMath.mean(FMLCommonHandler.instance().getMinecraftServerInstance().worldTickTimes.get(world.provider.getDimension()))
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "ticktime").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_DAYTIME =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        world -> (int) (world.getWorldTime() % MinecraftHelpers.MINECRAFT_DAY)
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "daytime").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_LIGHTLEVEL =
                    AspectReadBuilders.World.BUILDER_INTEGER.handle(
                        dimPos -> dimPos.getWorld().getLight(dimPos.getBlockPos())
                    ).handle(AspectReadBuilders.PROP_GET_INTEGER, "lightlevel").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_TPS =
                    AspectReadBuilders.World.BUILDER_DOUBLE.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                            world -> Math.min(20, Stats.meanOf(FMLCommonHandler.instance().getMinecraftServerInstance().worldTickTimes.get(world.provider.getDimension())) / 1000)
                    ).handle(AspectReadBuilders.PROP_GET_DOUBLE, "tps").buildRead();

            public static final IAspectRead<ValueTypeLong.ValueLong, ValueTypeLong> LONG_TIME =
                    AspectReadBuilders.World.BUILDER_LONG.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        net.minecraft.world.World::getWorldTime
                    ).handle(AspectReadBuilders.PROP_GET_LONG, "time").buildRead();
            public static final IAspectRead<ValueTypeLong.ValueLong, ValueTypeLong> LONG_TOTALTIME =
                    AspectReadBuilders.World.BUILDER_LONG.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        net.minecraft.world.World::getTotalWorldTime
                    ).handle(AspectReadBuilders.PROP_GET_LONG, "totaltime").buildRead();

            public static final IAspectRead<ValueTypeString.ValueString, ValueTypeString> STRING_NAME =
                    AspectReadBuilders.World.BUILDER_STRING.handle(AspectReadBuilders.World.PROP_GET_WORLD).handle(
                        world -> world.getWorldInfo().getWorldName()
                    ).handle(AspectReadBuilders.PROP_GET_STRING, "worldname").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_PLAYERS =
                    AspectReadBuilders.World.BUILDER_LIST.handle(dimPos ->
                            ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(dimPos.getWorld().playerEntities,
                                ValueObjectTypeEntity.ValueEntity::of))
                    ).appendKind("players").buildRead();

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
                            .handle(input -> {
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
                            }, "sound").buildWrite();

        }

        public static final class Effect {

            public static IAspectWrite<ValueTypeDouble.ValueDouble, ValueTypeDouble> createForParticle(final EnumParticleTypes particle) {
                return AspectWriteBuilders.Effect.BUILDER_DOUBLE_PARTICLE.appendKind("particle").appendKind(particle.getParticleName().toLowerCase(Locale.ROOT))
                        .handle(input -> {
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
                        }).buildWrite();
            }

        }

        public static final class Redstone {

            public static final IAspectWrite<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN =
                    AspectWriteBuilders.Redstone.BUILDER_BOOLEAN.handle(
                        input -> Triple.of(input.getLeft(), input.getMiddle(), input.getRight() ? 15 : 0)
                    ).handle(AspectWriteBuilders.Redstone.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER =
                    AspectWriteBuilders.Redstone.BUILDER_INTEGER.handle(AspectWriteBuilders.Redstone.PROP_SET).buildWrite();
            public static final IAspectWrite<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_PULSE =
                    AspectWriteBuilders.Redstone.BUILDER_BOOLEAN
                            .withProperties(AspectWriteBuilders.Redstone.PROPERTIES_REDSTONE_PULSE)
                            .appendKind("pulse")
                            .handle(input -> Triple.of(input.getLeft(), input.getMiddle(), input.getRight() ? 15 : 0)
                    ).handle(AspectWriteBuilders.Redstone.PROP_SET_PULSE).buildWrite();
            public static final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_PULSE =
                    AspectWriteBuilders.Redstone.BUILDER_INTEGER
                            .withProperties(AspectWriteBuilders.Redstone.PROPERTIES_REDSTONE_PULSE)
                            .appendKind("pulse")
                            .handle(AspectWriteBuilders.Redstone.PROP_SET_PULSE).buildWrite();

        }

    }

}
