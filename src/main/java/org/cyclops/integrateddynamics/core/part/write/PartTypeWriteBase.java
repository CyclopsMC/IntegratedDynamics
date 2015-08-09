package org.cyclops.integrateddynamics.core.part.write;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.client.gui.GuiPartWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.List;

/**
 * An abstract {@link org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter}.
 * @author rubensworks
 */
public abstract class PartTypeWriteBase<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>>
        extends PartTypeBase<P, S> implements IPartTypeWriter<P, S> {

    public PartTypeWriteBase(String name) {
        super(name);
    }

    @Override
    public Class<? super P> getPartTypeClass() {
        return IPartTypeWriter.class;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks) {
        for(int i = 0; i < state.getInventory().getSizeInventory(); i++) {
            ItemStack itemStack = state.getInventory().getStackInSlot(i);
            if(itemStack != null) {
                itemStacks.add(itemStack);
            }
        }
        state.getInventory().clear();
        state.triggerAspectInfoUpdate((P) this, target, null);
        super.addDrops(target, state, itemStacks);
    }

    @Override
    public void beforeNetworkKill(Network network, PartTarget target, S state) {
        super.beforeNetworkKill(network, target, state);
        state.triggerAspectInfoUpdate((P) this, target, null);
    }

    @Override
    public void afterNetworkAlive(Network network, PartTarget target, S state) {
        super.afterNetworkAlive(network, target, state);
        updateActivation(target, state);
    }

    @Override
    public List<IAspectWrite> getWriteAspects() {
        return Aspects.REGISTRY.getWriteAspects(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends IValue> IVariable<V> getActiveVariable(Network network, PartTarget target, S partState) {
        return partState.getVariable(network);
    }

    @Override
    public IAspectWrite getActiveAspect(PartTarget target, S partState) {
        return partState.getActiveAspect();
    }

    @Override
    public void updateActivation(PartTarget target, S partState) {
        // Check inside the inventory for a variable item and determine everything with that.
        int activeIndex = -1;
        for(int i = 0 ; i < partState.getInventory().getSizeInventory(); i++) {
            if(partState.getInventory().getStackInSlot(i) != null) {
                activeIndex = i;
                break;
            }
        }
        IAspectWrite aspect = activeIndex == -1 ? null : getWriteAspects().get(activeIndex);
        partState.triggerAspectInfoUpdate((P) this, target, aspect);
    }

    @Override
    public void refresh(Network network, PartTarget target, S state) {
        state.refresh((P) this, target);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPartWriter.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiPartWriter.class;
    }

}
