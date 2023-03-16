package org.cyclops.integrateddynamics.core.part.write;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.network.event.NetworkElementAddEvent;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.PartTypeAspects;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.core.part.event.PartWriterAspectEvent;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        IEventAction<P, S, INetworkEvent> updateEventListener = (network, target, state, event) -> NetworkHelpers
                .getPartNetwork(network).ifPresent(partNetwork -> onVariableContentsUpdated(partNetwork, target, state));
        actions.put(VariableContentsUpdatedEvent.class, updateEventListener);
        actions.put(NetworkElementAddEvent.Post.class, updateEventListener);
        return actions;
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus();
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.update(network, partNetwork, target, state);
        IAspect aspect = getActiveAspect(target, state);
        if (aspect != null) {
            aspect.update(network, partNetwork, this, target, state);
        }
    }

    @Override
    public boolean setTargetOffset(S state, PartPos center, Vec3i offset) {
        IAspectWrite activeAspect = state.getActiveAspect();
        if(activeAspect != null) {
            activeAspect.onDeactivate(this, getTarget(center, state), state);
        }
        boolean ret = super.setTargetOffset(state, center, offset);
        if(activeAspect != null) {
            activeAspect.onActivate(this, getTarget(center, state), state);
        }
        return ret;
    }

    @Override
    public void addDrops(PartTarget target, S state, List<ItemStack> itemStacks, boolean dropMainElement, boolean saveState) {
        for(int i = 0; i < state.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = state.getInventory().getItem(i);
            if(!itemStack.isEmpty()) {
                itemStacks.add(itemStack);
            }
        }
        state.getInventory().clearContent();
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
    public <V extends IValue> IVariable<V> getActiveVariable(INetwork network, IPartNetwork partNetwork, PartTarget target, S partState) {
        return partState.getVariable(network, partNetwork);
    }

    @Override
    public IAspectWrite getActiveAspect(PartTarget target, S partState) {
        return partState.getActiveAspect();
    }

    @Override
    public void updateActivation(PartTarget target, S partState, @Nullable Player player) {
        // Check inside the inventory for a variable item and determine everything with that.
        int activeIndex = -1;
        for(int i = 0 ; i < partState.getInventory().getContainerSize(); i++) {
            if(!partState.getInventory().getItem(i).isEmpty()) {
                activeIndex = i;
                break;
            }
        }
        IAspectWrite aspect = activeIndex == -1 ? null : getWriteAspects().get(activeIndex);
        partState.triggerAspectInfoUpdate((P) this, target, aspect);

        INetwork network = NetworkHelpers.getNetwork(target.getCenter()).orElse(null);
        if (network != null && aspect != null) {
            IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network).orElse(null);
            if (partNetwork != null) {
                MinecraftForge.EVENT_BUS.post(new PartWriterAspectEvent<>(network, partNetwork, target, (P) this,
                        partState, player, aspect, partState.getInventory().getItem(activeIndex)));
            }
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
    public BlockState getBlockState(IPartContainer partContainer,
                                    Direction side) {
        IgnoredBlockStatus.Status status = getStatus(partContainer != null
                ? (IPartStateWriter) partContainer.getPartState(side) : null);
        return getBlock().defaultBlockState().setValue(IgnoredBlock.FACING, side)
                .setValue(IgnoredBlockStatus.STATUS, status);
    }

    @Override
    public void loadTooltip(S state, List<Component> lines) {
        super.loadTooltip(state, lines);
        IAspectWrite aspectWrite = state.getActiveAspect();
        if (aspectWrite != null) {
            if (state.hasVariable() && state.isEnabled()) {
                lines.add(Component.translatable(
                        L10NValues.PART_TOOLTIP_WRITER_ACTIVEASPECT,
                        Component.translatable(aspectWrite.getTranslationKey()),
                        Component.translatable(aspectWrite.getValueType().getTranslationKey())
                                .withStyle(aspectWrite.getValueType().getDisplayColorFormat())));
            } else {
                lines.add(Component.translatable(L10NValues.PART_TOOLTIP_ERRORS)
                        .withStyle(ChatFormatting.RED));
                for (MutableComponent error : state.getErrors(aspectWrite)) {
                    lines.add(error.withStyle(ChatFormatting.RED));
                }
            }
        } else {
            lines.add(Component.translatable(L10NValues.PART_TOOLTIP_INACTIVE));
        }
    }

    @Override
    public Optional<MenuProvider> getContainerProvider(PartPos pos) {
        return Optional.of(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable(getTranslationKey());
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                S partState = (S) data.getLeft().getPartState(data.getRight().getCenter().getSide());
                return new ContainerPartWriter<>(id, playerInventory, partState.getInventory(),
                        data.getRight(), Optional.of(data.getLeft()), (PartTypeWriteBase) data.getMiddle());
            }
        });
    }

    @Override
    public void writeExtraGuiData(FriendlyByteBuf packetBuffer, PartPos pos, ServerPlayer player) {
        // Write inventory size
        packetBuffer.writeInt(getWriteAspects().size());
        // Write part position
        PacketCodec.write(packetBuffer, pos);

        super.writeExtraGuiData(packetBuffer, pos, player);
    }

    @Override
    public boolean shouldTriggerBlockRenderUpdate(@Nullable S oldPartState, @Nullable S newPartState) {
        return super.shouldTriggerBlockRenderUpdate(oldPartState, newPartState)
                || getStatus(oldPartState) != getStatus(newPartState);
    }

}
