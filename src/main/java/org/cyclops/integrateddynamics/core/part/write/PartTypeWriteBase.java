package org.cyclops.integrateddynamics.core.part.write;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.client.gui.GuiPartWriter;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.PartTypeAspects;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.List;
import java.util.Map;

/**
 * An abstract {@link IPartTypeWriter}.
 * @author rubensworks
 */
public abstract class PartTypeWriteBase<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>>
        extends PartTypeAspects<P, S> implements IPartTypeWriter<P, S> {

    public PartTypeWriteBase(String name) {
        super(name, new RenderPosition(0.3125F, 0.25F, 0.25F));
    }

    @Override
    protected Map<Class<? extends INetworkEvent<IPartNetwork>>, IEventAction> constructNetworkEventActions() {
        Map<Class<? extends INetworkEvent<IPartNetwork>>, IEventAction> actions = super.constructNetworkEventActions();
        actions.put(VariableContentsUpdatedEvent.class, new IEventAction<P, S, VariableContentsUpdatedEvent>() {
            @Override
            public void onAction(IPartNetwork network, PartTarget target, S state, VariableContentsUpdatedEvent event) {
                onVariableContentsUpdated(event.getNetwork(), target, state);
            }
        });
        return actions;
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus(blockConfig);
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
    public void beforeNetworkKill(IPartNetwork network, PartTarget target, S state) {
        super.beforeNetworkKill(network, target, state);
        state.triggerAspectInfoUpdate((P) this, target, null);
    }

    @Override
    public void afterNetworkAlive(IPartNetwork network, PartTarget target, S state) {
        super.afterNetworkAlive(network, target, state);
        updateActivation(target, state);
    }

    @Override
    public List<IAspectWrite> getWriteAspects() {
        return Aspects.REGISTRY.getWriteAspects(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends IValue> IVariable<V> getActiveVariable(IPartNetwork network, PartTarget target, S partState) {
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

    protected void onVariableContentsUpdated(IPartNetwork network, PartTarget target, S state) {
        state.onVariableContentsUpdated((P) this, target);
    }

    @Override
    public IBlockState getBlockState(IPartContainer partContainer, double x, double y, double z, float partialTick,
                                     int destroyStage, EnumFacing side) {
        IPartStateWriter state = (IPartStateWriter) partContainer.getPartState(side);
        IgnoredBlockStatus.Status status = IgnoredBlockStatus.Status.INACTIVE;
        IAspectWrite aspectWrite = state.getActiveAspect();
        if(aspectWrite != null) {
            if(state.hasVariable() && state.isEnabled()) {
                status = IgnoredBlockStatus.Status.ACTIVE;
            } else {
                status = IgnoredBlockStatus.Status.ERROR;
            }
        }
        return getBlock().getDefaultState().withProperty(IgnoredBlock.FACING, side).
                withProperty(IgnoredBlockStatus.STATUS, status);
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
