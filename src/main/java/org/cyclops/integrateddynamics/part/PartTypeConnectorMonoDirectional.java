package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.capability.path.SidedPathElement;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import java.util.Collections;
import java.util.Set;

/**
 * A monodirectional wireless connector part that can connect to
 * at most one other monodirectional connector in a straight line.
 * @author rubensworks
 */
public class PartTypeConnectorMonoDirectional extends PartTypeConnector<PartTypeConnectorMonoDirectional, PartTypeConnectorMonoDirectional.State> {

    public PartTypeConnectorMonoDirectional(String name) {
        super(name, new PartRenderPosition(0.25F, 0.3125F, 0.5F, 0.5F));
    }

    @Override
    public int getConsumptionRate(State state) {
        return GeneralConfig.connectorMonoDirectionalBaseConsumption;
    }

    @Override
    public PartTypeConnectorMonoDirectional.State constructDefaultState() {
        return new PartTypeConnectorMonoDirectional.State();
    }

    @Override
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, State state) {
        super.onNetworkAddition(network, partNetwork, target, state);

        // Find and link two parts
        if (!state.hasTarget()) {
            int offset = findTargetOffset(target.getCenter());
            if (offset > 0) {
                state.setTarget(offset);
                state.getTargetState(target.getCenter()).setTarget(offset);

                // Re-init network at the two disconnected connectors
                DimPos originPos = target.getCenter().getPos();
                DimPos targetPos = PartTypeConnectorMonoDirectional.State.getTargetPos(target.getCenter(), state.getOffset());
                NetworkHelpers.initNetwork(originPos.getLevel(true), originPos.getBlockPos(), target.getCenter().getSide());
                NetworkHelpers.initNetwork(targetPos.getLevel(true), targetPos.getBlockPos(), target.getCenter().getSide().getOpposite());
            }
        }
    }

    @Override
    public void onPostRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, State state) {
        super.onPostRemoved(network, partNetwork, target, state);

        if (state.hasTarget()) {
            // Remove target information in both linked parts
            PartTypeConnectorMonoDirectional.State targetState = state.getTargetState(target.getCenter());
            DimPos originPos = target.getCenter().getPos();
            DimPos targetPos = null;
            if (targetState != null) {
                targetState.removeTarget();
                targetPos = PartTypeConnectorMonoDirectional.State.getTargetPos(target.getCenter(), state.getOffset());
            }
            state.removeTarget();

            // Re-init network at the two disconnected connectors
            NetworkHelpers.initNetwork(originPos.getLevel(true), originPos.getBlockPos(),  target.getCenter().getSide());
            if (targetPos != null) {
                NetworkHelpers.initNetwork(targetPos.getLevel(true), targetPos.getBlockPos(),  target.getCenter().getSide().getOpposite());
            }
        }
    }

    @Override
    public ItemStack getItemStack(ValueDeseralizationContext valueDeseralizationContext, State state, boolean saveState) {
        // Set offset to 0 to make sure it is not stored in the item
        int offset = state.getOffset();
        state.setOffset(0);

        // Serialize to item
        ItemStack itemStack = super.getItemStack(valueDeseralizationContext, state, saveState);

        // Set original offset back
        state.setOffset(offset);

        return itemStack;
    }

    /**
     * Look in the part's direction for an unbound monodirectional connector.
     * @param origin The origin position to start looking from.
     * @return The other connector's distance, or 0 if not found.
     */
    protected int findTargetOffset(PartPos origin) {
        int offset = 0;
        PartTypeConnectorMonoDirectional.State state = null;
        while (++offset < GeneralConfig.maxDirectionalConnectorOffset
                && (state = PartTypeConnectorMonoDirectional.State.getUnboundTargetState(origin, offset)) == null);
        if (state != null) {
            return offset;
        }
        return 0;
    }

    protected IgnoredBlockStatus.Status getStatus(PartTypeConnectorMonoDirectional.State state) {
        return state != null && state.hasTarget()
                ? IgnoredBlockStatus.Status.ACTIVE : IgnoredBlockStatus.Status.INACTIVE;
    }

    @Override
    public BlockState getBlockState(IPartContainer partContainer, Direction side) {
        IgnoredBlockStatus.Status status = getStatus(partContainer != null
                ? (PartTypeConnectorMonoDirectional.State) partContainer.getPartState(side) : null);
        return super.getBlockState(partContainer, side)
                .setValue(IgnoredBlock.FACING, side)
                .setValue(IgnoredBlockStatus.STATUS, status);
    }

    public static class State extends PartTypeConnector.State<PartTypeConnectorMonoDirectional> {

        private int offset = 0;

        @Override
        public Set<ISidedPathElement> getReachableElements() {
            if (getPartPos() != null) {
                Direction targetSide = getPartPos().getSide().getOpposite();
                IPathElement pathElement = BlockEntityHelpers.getCapability(State.getTargetPos(getPartPos(), offset),
                        targetSide, Capabilities.PathElement.BLOCK).orElse(null);
                if (pathElement != null) {
                    return Sets.newHashSet(SidedPathElement.of(pathElement, targetSide));
                }
            }
            return Collections.emptySet();
        }

        public void setTarget(int offset) {
            setOffset(offset);
            sendUpdate();

            DimPos dimPos = getPosition();
            if (dimPos != null && this.offset > 0) {
                BlockPos pos = dimPos.getBlockPos();
                for (int i = 1; i < this.offset; i++) {
                    pos = pos.relative(getPartPos().getSide());
                    ((ServerLevel) getPosition().getLevel(true))
                            .sendParticles(DustParticleOptions.REDSTONE,
                                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1, 0D, 0D, 0D, 0);
                }
            }
        }

        public boolean hasTarget() {
            return this.offset > 0;
        }

        public int getOffset() {
            return this.offset;
        }

        /**
         * Set the raw offset.
         * Prefer {@link #setTarget(int)}.
         * @param offset The new offset.
         */
        public void setOffset(int offset) {
            this.offset = offset;
        }

        public void removeTarget() {
            setTarget(0);
        }

        protected PartTypeConnectorMonoDirectional.State getTargetState(PartPos origin) {
            return getTargetState(origin, offset);
        }

        @Override
        public void writeToNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
            super.writeToNBT(valueDeseralizationContext, tag);
            if (offset > 0) {
                tag.putInt("connect_offset", offset);
            }
        }

        @Override
        public void readFromNBT(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag) {
            super.readFromNBT(valueDeseralizationContext, tag);
            if (tag.contains("connect_offset")) {
                this.offset = tag.getInt("connect_offset");
            }
        }

        protected static PartTypeConnectorMonoDirectional.State getUnboundTargetState(PartPos origin, int offset) {
            PartTypeConnectorMonoDirectional.State state = getTargetState(origin, offset);
            if (state != null && !state.hasTarget()) {
                return state;
            }
            return null;
        }

        protected static PartTypeConnectorMonoDirectional.State getTargetState(PartPos origin, int offset) {
            PartPos targetPos = PartPos.of(getTargetPos(origin, offset), origin.getSide().getOpposite());
            PartHelpers.PartStateHolder partStateHolder = PartHelpers.getPart(targetPos);
            if (partStateHolder != null && partStateHolder.getPart() instanceof PartTypeConnectorMonoDirectional) {
                return (State) partStateHolder.getState();
            }
            return null;
        }

        protected static DimPos getTargetPos(PartPos origin, int offset) {
            return DimPos.of(origin.getPos().getLevelKey(),
                    origin.getPos().getBlockPos().relative(origin.getSide(), offset));
        }
    }

}
