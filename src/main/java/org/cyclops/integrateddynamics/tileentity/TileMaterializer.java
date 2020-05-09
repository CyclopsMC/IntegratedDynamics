package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.core.tileentity.TileActiveVariableBase;
import org.cyclops.integrateddynamics.network.MaterializerNetworkElement;

/**
 * A part entity for the variable materializer.
 * @author rubensworks
 */
public class TileMaterializer extends TileActiveVariableBase<MaterializerNetworkElement> {

    public static final int SLOT_READ = 0;
    public static final int SLOT_WRITE_IN = 1;
    public static final int SLOT_WRITE_OUT = 2;

    @Setter
    private EntityPlayer lastPlayer = null;

    public TileMaterializer() {
        super(3, "materializer");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(SLOT_WRITE_OUT));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(SLOT_WRITE_IN));

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new MaterializerNetworkElement(DimPos.of(world, blockPos));
            }
        });
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return slot != SLOT_WRITE_OUT && super.canInsertItem(slot, itemStack, side);
    }

    @Override
    public int getSlotRead() {
        return SLOT_READ;
    }

    protected boolean canWrite() {
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(getNetwork());
        return partNetwork != null && getVariable(partNetwork) != null && getEvaluator().getErrors().isEmpty();
    }

    @Override
    public void onDirty() {
        super.onDirty();
        if(!world.isRemote) {
            if (!getStackInSlot(SLOT_WRITE_IN).isEmpty() && canWrite() && getStackInSlot(SLOT_WRITE_OUT).isEmpty()) {
                // Write proxy reference
                ItemStack outputStack = writeMaterialized(!getWorld().isRemote, getStackInSlot(SLOT_WRITE_IN));
                if(!outputStack.isEmpty()) {
                    setInventorySlotContents(SLOT_WRITE_OUT, outputStack);
                    removeStackFromSlot(SLOT_WRITE_IN);
                }
            }
        }
    }

    public ItemStack writeMaterialized(boolean generateId, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        IVariable variable = getVariable(NetworkHelpers.getPartNetwork(getNetwork()));
        try {
            final IValue value = variable.getType().materialize(variable.getValue());
            final IValueType valueType = value.getType();
            return registry.writeVariableFacadeItem(generateId, itemStack, ValueTypes.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade>() {
                @Override
                public IValueTypeVariableFacade create(boolean generateId) {
                    return new ValueTypeVariableFacade(generateId, valueType, value);
                }

                @Override
                public IValueTypeVariableFacade create(int id) {
                    return new ValueTypeVariableFacade(id, valueType, value);
                }
            }, lastPlayer, getBlock());
        } catch (EvaluationException e) {
            getEvaluator().addError(new L10NHelpers.UnlocalizedString(e.getMessage()));
        }
        return ItemStack.EMPTY;
    }
}
