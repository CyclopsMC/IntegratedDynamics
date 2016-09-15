package org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeFluidStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.modcompat.refinedstorage.RefinedStorageModCompat;
import refinedstorage.api.network.INetworkMaster;
import refinedstorage.api.storage.fluid.IFluidStorage;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * A list proxy for a network's grouped fluid inventory at a certain position.
 */
public class ValueTypeListProxyPositionedNetworkMasterFluidInventory extends ValueTypeListProxyBase<ValueObjectTypeFluidStack, ValueObjectTypeFluidStack.ValueFluidStack> implements INBTProvider {

    @NBTPersist
    private DimPos pos;

    public ValueTypeListProxyPositionedNetworkMasterFluidInventory() {
        this(null);
    }

    public ValueTypeListProxyPositionedNetworkMasterFluidInventory(DimPos pos) {
        super(RefinedStorageModCompat.POSITIONED_MASTERFLUIDINVENTORY.getName(), ValueTypes.OBJECT_FLUIDSTACK);
        this.pos = pos;
    }

    protected Optional<INetworkMaster> getNetworkMaster() {
        return Optional.fromNullable(TileHelpers.getSafeTile(pos, INetworkMaster.class));
    }

    protected Optional<List<FluidStack>> getInventory() {
        return getNetworkMaster().transform(new Function<INetworkMaster, List<FluidStack>>() {
            @Nullable
            @Override
            public List<FluidStack> apply(@Nullable INetworkMaster networkMaster) {
                if (networkMaster == null) {
                    return null;
                }
                List<List<FluidStack>> fluidStacksLists = Lists.transform(networkMaster.getFluidStorage().getStorages(), new Function<IFluidStorage, List<FluidStack>>() {
                    @Nullable
                    @Override
                    public List<FluidStack> apply(@Nullable IFluidStorage fluidStorage) {
                        return fluidStorage.getStacks();
                    }
                });
                return new LazyCompositeList<>(fluidStacksLists);
            }
        });
    }

    @Override
    public int getLength() {
        return getInventory().or(Collections.<FluidStack>emptyList()).size();
    }

    @Override
    public ValueObjectTypeFluidStack.ValueFluidStack get(int index) {
        return ValueObjectTypeFluidStack.ValueFluidStack.of(getInventory().or(Collections.<FluidStack>emptyList()).get(index));
    }

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {

    }
}
