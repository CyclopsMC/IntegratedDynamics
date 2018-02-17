package org.cyclops.integrateddynamics.core.part.write;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.client.gui.GuiPartWriter;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.PartTypeAspects;
import org.cyclops.integrateddynamics.core.part.event.PartWriterAspectEvent;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * An abstract {@link IPartTypeWriter}.
 * @author rubensworks
 */
public abstract class PartTypeWriteBase<P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>>
        extends PartTypeAspects<P, S> implements IPartTypeWriter<P, S> {

    private List<IAspectWrite> aspectsWrite = null;

    public PartTypeWriteBase(String name) {
        this(name, new PartRenderPosition(0.3125F, 0.3125F, 0.625F, 0.625F, 0.25F, 0.25F));
    }

    public PartTypeWriteBase(String name, PartRenderPosition partRenderPosition) {
        super(name, partRenderPosition);
    }

    @Override
    protected Map<Class<? extends INetworkEvent>, IEventAction> constructNetworkEventActions() {
        Map<Class<? extends INetworkEvent>, IEventAction> actions = super.constructNetworkEventActions();
        actions.put(VariableContentsUpdatedEvent.class, new IEventAction<P, S, VariableContentsUpdatedEvent>() {
            @Override
            public void onAction(INetwork network, PartTarget target, S state, VariableContentsUpdatedEvent event) {
                IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
                onVariableContentsUpdated(partNetwork, target, state);
            }
        });
        actions.put(NetworkElementAddEvent.Post.class, new IEventAction<P, S, NetworkElementAddEvent.Post>() {
            @Override
            public void onAction(INetwork network, PartTarget target, S state, NetworkElementAddEvent.Post event) {
                IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
                onVariableContentsUpdated(partNetwork, target, state);
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
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.update(network, partNetwork, target, state);
        IAspect aspect = getActiveAspect(target, state);
        if (aspect != null) {
            aspect.update(partNetwork, this, target, state);
        }
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        for(int i = 0; i < state.getInventory().getSizeInventory(); i++) {
            ItemStack itemStack = state.getInventory().getStackInSlot(i);
            if(!itemStack.isEmpty()) {
                itemStacks.add(itemStack);
            }
        }
        state.getInventory().clear();
        state.triggerAspectInfoUpdate((P) this, target, null);
        super.addDrops(target, state, itemStacks, dropMainElement, saveState);
    }

    @Override
    public void beforeNetworkKill(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.beforeNetworkKill(network, partNetwork, target, state);
        state.triggerAspectInfoUpdate((P) this, target, null);
    }

    @Override
    public void afterNetworkAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.afterNetworkAlive(network, partNetwork, target, state);
        updateActivation(target, state, null);
    }

    @Override
    public List<IAspectWrite> getWriteAspects() {
        if (aspectsWrite == null) {
            aspectsWrite = Aspects.REGISTRY.getWriteAspects(this);
        }
        return aspectsWrite;
    }

    @Override
    public boolean hasActiveVariable(IPartNetwork network, PartTarget target, S partState) {
        return partState.hasVariable();
    }

    @Override
    public <V extends IValue> IVariable<V> getActiveVariable(IPartNetwork network, PartTarget target, S partState) {
        return partState.getVariable(network);
    }

    @Override
    public IAspectWrite getActiveAspect(PartTarget target, S partState) {
        return partState.getActiveAspect();
    }

    @Override
    public void updateActivation(PartTarget target, S partState, @Nullable EntityPlayer player) {
        // Check inside the inventory for a variable item and determine everything with that.
        int activeIndex = -1;
        for(int i = 0 ; i < partState.getInventory().getSizeInventory(); i++) {
            if(!partState.getInventory().getStackInSlot(i).isEmpty()) {
                activeIndex = i;
                break;
            }
        }
        IAspectWrite aspect = activeIndex == -1 ? null : getWriteAspects().get(activeIndex);
        partState.triggerAspectInfoUpdate((P) this, target, aspect);

        INetwork network = NetworkHelpers.getNetwork(target.getCenter());
        if (aspect != null) {
            IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
            MinecraftForge.EVENT_BUS.post(new PartWriterAspectEvent<>(network, partNetwork, target, (P) this, partState, player,
                    aspect, partState.getInventory().getStackInSlot(activeIndex)));
        }
        if (network != null) {
            network.getEventBus().post(new VariableContentsUpdatedEvent(network));
        }
    }

    protected void onVariableContentsUpdated(IPartNetwork network, PartTarget target, S state) {
        state.onVariableContentsUpdated((P) this, target);
    }

    protected IgnoredBlockStatus.Status getStatus(IPartStateWriter state) {
        IgnoredBlockStatus.Status status = IgnoredBlockStatus.Status.INACTIVE;
        if (state != null && !state.getInventory().isEmpty()) {
            if (state.hasVariable() && state.isEnabled()) {
                status = IgnoredBlockStatus.Status.ACTIVE;
            } else {
                status = IgnoredBlockStatus.Status.ERROR;
            }
        }
        return status;
    }

    @Override
    public IBlockState getBlockState(IPartContainer partContainer,
                                     EnumFacing side) {
        IgnoredBlockStatus.Status status = getStatus(partContainer != null
                ? (IPartStateWriter) partContainer.getPartState(side) : null);
        return getBlock().getDefaultState().withProperty(IgnoredBlock.FACING, side).
                withProperty(IgnoredBlockStatus.STATUS, status);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPartWriter.class;
    }

    @Override
    public void loadTooltip(S state, List<String> lines) {
        super.loadTooltip(state, lines);
        IAspectWrite aspectWrite = state.getActiveAspect();
        if (aspectWrite != null) {
            if (state.hasVariable() && state.isEnabled()) {
                lines.add(L10NHelpers.localize(
                        L10NValues.PART_TOOLTIP_WRITER_ACTIVEASPECT,
                        L10NHelpers.localize(aspectWrite.getUnlocalizedName()),
                        aspectWrite.getValueType().getDisplayColorFormat()
                                + L10NHelpers.localize(aspectWrite.getValueType().getUnlocalizedName())
                                + TextFormatting.RESET));
            } else {
                lines.add(TextFormatting.RED + L10NHelpers.localize(L10NValues.PART_TOOLTIP_ERRORS));
                for (L10NHelpers.UnlocalizedString error : state.getErrors(aspectWrite)) {
                    lines.add(TextFormatting.RED + error.localize());
                }
            }
        } else {
            lines.add(L10NHelpers.localize(L10NValues.PART_TOOLTIP_INACTIVE));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiPartWriter.class;
    }

    @Override
    public boolean shouldTriggerBlockRenderUpdate(@Nullable S oldPartState, @Nullable S newPartState) {
        return super.shouldTriggerBlockRenderUpdate(oldPartState, newPartState)
                || getStatus(oldPartState) != getStatus(newPartState);
    }

}
