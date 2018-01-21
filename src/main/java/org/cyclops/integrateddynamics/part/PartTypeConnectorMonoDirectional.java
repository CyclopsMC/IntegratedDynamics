package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.api.path.ISidedPathElement;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
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
        return 32;
    }

    @Override
    public PartTypeConnectorMonoDirectional.State constructDefaultState() {
        return new PartTypeConnectorMonoDirectional.State();
    }

    @Override
    public Class<? super PartTypeConnectorMonoDirectional> getPartTypeClass() {
        return PartTypeConnectorMonoDirectional.class;
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
                NetworkHelpers.initNetwork(originPos.getWorld(), originPos.getBlockPos(), target.getCenter().getSide());
                NetworkHelpers.initNetwork(targetPos.getWorld(), targetPos.getBlockPos(), target.getCenter().getSide().getOpposite());
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
            NetworkHelpers.initNetwork(originPos.getWorld(), originPos.getBlockPos(),  target.getCenter().getSide());
            if (targetPos != null) {
                NetworkHelpers.initNetwork(targetPos.getWorld(), targetPos.getBlockPos(),  target.getCenter().getSide().getOpposite());
            }
        }
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
    public IBlockState getBlockState(IPartContainer partContainer, EnumFacing side) {
        IgnoredBlockStatus.Status status = getStatus(partContainer != null
                ? (PartTypeConnectorMonoDirectional.State) partContainer.getPartState(side) : null);
        return super.getBlockState(partContainer, side).withProperty(IgnoredBlock.FACING, side).
                withProperty(IgnoredBlockStatus.STATUS, status);
    }

    public static class State extends PartTypeConnector.State<PartTypeConnectorMonoDirectional> {

        private int offset = 0;

        @Override
        public Set<ISidedPathElement> getReachableElements() {
            if (getPartPos() != null) {
                EnumFacing targetSide = getPartPos().getSide().getOpposite();
                IPathElement pathElement = TileHelpers.getCapability(State.getTargetPos(getPartPos(), offset),
                        targetSide, PathElementConfig.CAPABILITY);
                if (pathElement != null) {
                    return Sets.newHashSet(SidedPathElement.of(pathElement, targetSide));
                }
            }
            return Collections.emptySet();
        }

        public void setTarget(int offset) {
            this.offset = offset;
            sendUpdate();

            DimPos dimPos = getPosition();
            if (dimPos != null && this.offset > 0) {
                BlockPos pos = dimPos.getBlockPos();
                for (int i = 1; i < this.offset; i++) {
                    pos = pos.offset(getPartPos().getSide());
                    ((WorldServer) getPosition().getWorld())
                            .spawnParticle(EnumParticleTypes.REDSTONE, true,
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

        public void removeTarget() {
            setTarget(0);
        }

        protected PartTypeConnectorMonoDirectional.State getTargetState(PartPos origin) {
            return getTargetState(origin, offset);
        }

        @Override
        public void writeToNBT(NBTTagCompound tag) {
            super.writeToNBT(tag);
            if (offset > 0) {
                tag.setInteger("connect_offset", offset);
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound tag) {
            super.readFromNBT(tag);
            if (tag.hasKey("connect_offset")) {
                this.offset = tag.getInteger("connect_offset");
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
            return DimPos.of(origin.getPos().getWorld(),
                    origin.getPos().getBlockPos().offset(origin.getSide(), offset));
        }
    }

}
