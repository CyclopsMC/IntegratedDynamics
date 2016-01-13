package org.cyclops.integrateddynamics.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.core.tileentity.TileActiveVariableBase;
import org.cyclops.integrateddynamics.network.MaterializerNetworkElement;

/**
 * A tile entity for the variable materializer.
 * @author rubensworks
 */
public class TileMaterializer extends TileActiveVariableBase<MaterializerNetworkElement> implements IDirtyMarkListener, IVariableFacade.IValidator, INetworkEventListener<IPartNetwork, MaterializerNetworkElement> {

    public static final int SLOT_READ = 0;
    public static final int SLOT_WRITE_IN = 1;
    public static final int SLOT_WRITE_OUT = 2;

    public TileMaterializer() {
        super(3, "materializer");

        addSlotsToSide(EnumFacing.UP, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.DOWN, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.SOUTH, Sets.newHashSet(SLOT_READ));
        addSlotsToSide(EnumFacing.WEST, Sets.newHashSet(SLOT_WRITE_OUT));
        addSlotsToSide(EnumFacing.EAST, Sets.newHashSet(SLOT_WRITE_IN));
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
        return getNetwork() != null && getVariable(getNetwork()) != null && getErrors().isEmpty();
    }

    @Override
    public void onDirty() {
        super.onDirty();
        if(!worldObj.isRemote) {
            if (getStackInSlot(SLOT_WRITE_IN) != null && canWrite() && getStackInSlot(SLOT_WRITE_OUT) == null) {
                // Write proxy reference
                ItemStack outputStack = writeMaterialized(!getWorld().isRemote, getStackInSlot(SLOT_WRITE_IN));
                if(outputStack != null) {
                    setInventorySlotContents(SLOT_WRITE_OUT, outputStack);
                    removeStackFromSlot(SLOT_WRITE_IN);
                }
            }
        }
    }

    public ItemStack writeMaterialized(boolean generateId, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        IVariable variable = getVariable(getNetwork());
        try {
            final IValueType valueType = variable.getType();
            final IValue value = variable.getType().materialize(variable.getValue());
            return registry.writeVariableFacadeItem(generateId, itemStack, ValueTypes.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade>() {
                @Override
                public IValueTypeVariableFacade create(boolean generateId) {
                    return new ValueTypeVariableFacade(generateId, valueType, value);
                }

                @Override
                public IValueTypeVariableFacade create(int id) {
                    return new ValueTypeVariableFacade(id, valueType, value);
                }
            });
        } catch (EvaluationException e) {
            addError(new L10NHelpers.UnlocalizedString(e.getMessage()));
        }
        return null;
    }
}
