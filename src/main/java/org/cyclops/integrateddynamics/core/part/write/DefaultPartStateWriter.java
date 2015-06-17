package org.cyclops.integrateddynamics.core.part.write;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.DefaultPartState;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Map;

/**
 * A default implementation of the {@link org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public class DefaultPartStateWriter<P extends IPartTypeWriter>
        extends DefaultPartState<P> implements IPartStateWriter<P> {

    private boolean checkedForWriteVariable = false;
    private Pair<Integer, IAspect> currentAspectInfo = null;
    @NBTPersist
    private String activeAspectName = null;
    private SimpleInventory inventory;
    @NBTPersist
    private Map<String, L10NHelpers.UnlocalizedString> errorMessages = Maps.newHashMap();

    public DefaultPartStateWriter(int inventorySize) {
        this.inventory = new SingularInventory(inventorySize);
        this.inventory.addDirtyMarkListener(this); // No need to remove myself eventually. If I am removed, inv is also removed.
    }

    @Override
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    @Override
    public Pair<Integer, IAspect> getCurrentAspectInfo(Network network) {
        if(!checkedForWriteVariable) {
            for(int slot = 0; slot < getInventory().getSizeInventory(); slot++) {
                ItemStack itemStack = getInventory().getStackInSlot(slot);
                if(itemStack != null) {
                    this.currentAspectInfo = Aspects.REGISTRY.readAspect(itemStack);
                    // Note that this is only called server-side, so these errors are sent via NBT to the client(s).
                    if(getActiveAspect() != null) {
                        if (this.currentAspectInfo == null) {
                            setError(getActiveAspect(), new L10NHelpers.UnlocalizedString("aspect.error.invalidVariableItem"));
                        } else if (!(this.currentAspectInfo.getRight() instanceof IAspectRead
                                && network.hasPart(this.currentAspectInfo.getLeft()))) {
                            setError(getActiveAspect(), new L10NHelpers.UnlocalizedString("aspect.error.partNotInNetwork",
                                    this.currentAspectInfo.getLeft().toString()));
                        } else if (getActiveAspect().getValueType() != this.currentAspectInfo.getValue().getValueType()) {
                            setError(getActiveAspect(), new L10NHelpers.UnlocalizedString("aspect.error.invalidType",
                                    new L10NHelpers.UnlocalizedString(getActiveAspect().getValueType().getUnlocalizedName()),
                                    new L10NHelpers.UnlocalizedString(this.currentAspectInfo.getValue().getValueType().getUnlocalizedName())));
                        }
                    }
                }
            }
            this.checkedForWriteVariable = true;
        }
        return currentAspectInfo;
    }

    @Override
    public void triggerAspectInfoUpdate(P partType, PartTarget target, IAspectWrite newAspect) {
        this.checkedForWriteVariable = false;
        IAspectWrite activeAspect = getActiveAspect();
        if(activeAspect != null && activeAspect != newAspect) {
            activeAspect.onDeactivate(partType, target, this);
            setError(activeAspect, null);
        }
        this.currentAspectInfo = null;
        this.activeAspectName = newAspect == null ? null : newAspect.getUnlocalizedName();
    }

    @Override
    public IAspectWrite getActiveAspect() {
        if(this.activeAspectName == null) {
            return null;
        }
        IAspect aspect = Aspects.REGISTRY.getAspect(this.activeAspectName);
        if(!(aspect instanceof IAspectWrite)) {
            return null;
        }
        return (IAspectWrite) aspect;
    }

    @Override
    public L10NHelpers.UnlocalizedString getError(IAspectWrite aspect) {
        return errorMessages.get(aspect.getUnlocalizedName());
    }

    @Override
    public void setError(IAspectWrite aspect, L10NHelpers.UnlocalizedString error) {
        errorMessages.put(aspect.getUnlocalizedName(), error);
        onDirty();
        sendUpdate(); // We want this error messages to be sent to the client(s).
    }

    @Override
    public Class<? extends IPartState> getPartStateClass() {
        return IPartStateWriter.class;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        inventory.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inventory.readFromNBT(tag);
    }

    /**
     * An inventory that can only hold one filled slot at a time.
     */
    public static class SingularInventory extends SimpleInventory {

        /**
         * Make a new instance.
         *
         * @param size The amount of slots in the inventory.
         */
        public SingularInventory(int size) {
            super(size, "stateInventory", 1);
        }

        protected boolean canInsert() {
            for (int i = 0; i < getSizeInventory(); i++) {
                if (getStackInSlot(i) != null) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isItemValidForSlot(int i, ItemStack itemstack) {
            return canInsert() && super.isItemValidForSlot(i, itemstack);
        }

    }

}
