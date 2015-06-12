package org.cyclops.integrateddynamics.core.part.write;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.DefaultPartState;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A default implementation of the {@link org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter} with auto-persistence
 * of fields annotated with {@link org.cyclops.cyclopscore.persist.nbt.NBTPersist}.
 * @author rubensworks
 */
public class DefaultPartStateWriter<P extends IPartTypeWriter>
        extends DefaultPartState<P> implements IPartStateWriter<P> {

    private boolean checkedForWriteVariable = false;
    private Pair<Integer, IAspect> currentAspectInfo = null;
    private IAspectWrite activeAspect = null;
    private SimpleInventory inventory;

    public DefaultPartStateWriter(int inventorySize) {
        this.inventory = new SimpleInventory(inventorySize, "stateInventory", 1);
        this.inventory.addDirtyMarkListener(this);
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
                }
            }
            this.checkedForWriteVariable = true;
        }
        return currentAspectInfo;
    }

    @Override
    public void triggerAspectInfoUpdate(P partType, PartTarget target, IAspectWrite newAspect) {
        this.checkedForWriteVariable = false;
        if(activeAspect != null && activeAspect != newAspect) {
            activeAspect.onDeactivate(partType, target, this);
        }
        this.currentAspectInfo = null;
        this.activeAspect = newAspect;
    }

    @Override
    public IAspectWrite getActiveAspect() {
        return this.activeAspect;
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

}
