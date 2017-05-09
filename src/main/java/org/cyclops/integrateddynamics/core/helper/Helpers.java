package org.cyclops.integrateddynamics.core.helper;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Helper methods.
 * @author rubensworks
 */
public final class Helpers {

    public static final Predicate<Entity> SELECTOR_IS_PLAYER = new Predicate<Entity>() {
        public boolean apply(@Nullable Entity p_apply_1_) {
            return p_apply_1_ instanceof EntityPlayer;
        }
    };

    /**
     * Get the fluidstack from the given itemstack.
     * @param itemStack The itemstack.
     * @return The fluidstack or null.
     */
    public static FluidStack getFluidStack(ItemStack itemStack) {
        FluidStack fluidStack = FluidUtil.getFluidContained(itemStack);
        if (fluidStack == null
                && itemStack.getItem() instanceof ItemBlock
                && ((ItemBlock) itemStack.getItem()).getBlock() instanceof IFluidBlock) {
            fluidStack = new FluidStack(((IFluidBlock) ((ItemBlock) itemStack.getItem()).getBlock()).getFluid(), Fluid.BUCKET_VOLUME);
        }
        return fluidStack;
    }

    /**
     * Get the fluidstack capacity from the given itemstack.
     * @param itemStack The itemstack.
     * @return The capacity
     */
    public static int getFluidStackCapacity(ItemStack itemStack) {
        IFluidHandler fluidHandler = FluidUtil.getFluidHandler(itemStack);
        if (fluidHandler != null) {
            for (IFluidTankProperties properties : fluidHandler.getTankProperties()) {
                return properties.getCapacity();
            }
        }
        return 0;
    }

    /**
     * Add the given element to a copy of the given list/
     * @param list The list.
     * @param newElement The element.
     * @param <T> The type.
     * @return The new joined list.
     */
    public static <T> List<T> joinList(List<T> list, T newElement) {
        ImmutableList.Builder<T> builder = ImmutableList.<T>builder().addAll(list);
        if(newElement != null) {
            builder.add(newElement);
        }
        return builder.build();
    }

    /**
     * Create a string of 'length' times '%s' seperated by ','.
     * @param length The length for the series of '%s'.
     * @return The string.
     */
    public static String createPatternOfLength(int length) {
        StringBuilder pattern = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < length; i++) {
            if (first) {
                first = false;
            } else {
                pattern.append(",");
            }
            pattern.append("%s");
        }
        return pattern.toString();
    }

    private static final List<IInterfaceRetriever> INTERFACE_RETRIEVERS = Lists.newArrayList();
    static {
        addInterfaceRetriever(new IInterfaceRetriever() {
            @Override
            public <C> C getInterface(IBlockAccess world, BlockPos pos, Class<C> clazz) {
                return TileHelpers.getSafeTile(world, pos, clazz);
            }
        });
    }

    /**
     * Check for the given interface at the given position.
     * @param world The world.
     * @param pos The position.
     * @param clazz The class to find.
     * @param <C> The class type.
     * @return The instance or null.
     */
    private static <C> C getInterface(IBlockAccess world, BlockPos pos, Class<C> clazz) {
        C instance;
        for(IInterfaceRetriever interfaceRetriever : INTERFACE_RETRIEVERS) {
            instance = interfaceRetriever.getInterface(world, pos, clazz);
            if(instance != null) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Check for the given interface at the given position.
     * @param dimPos The dimensional position.
     * @param clazz The class to find.
     * @param <C> The class type.
     * @return The instance or null.
     */
    public static <C> C getInterface(DimPos dimPos, Class<C> clazz) {
        return getInterface(dimPos.getWorld(), dimPos.getBlockPos(), clazz);
    }

    public static void addInterfaceRetriever(IInterfaceRetriever interfaceRetriever) {
        INTERFACE_RETRIEVERS.add(interfaceRetriever);
    }

    public static interface IInterfaceRetriever {

        /**
         * Attempt to get a given interface instance.
         * @param world The world.
         * @param pos The position.
         * @param clazz The class to find.
         * @param <C> The class type.
         * @return The instance or null.
         */
        public <C> C getInterface(IBlockAccess world, BlockPos pos, Class<C> clazz);

    }

}
