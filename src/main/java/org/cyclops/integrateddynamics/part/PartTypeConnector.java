package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.common.capabilities.Capability;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.path.IPathElement;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.part.PartStateBase;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;

/**
 * A base wireless connector part.
 * @author rubensworks
 */
public abstract class PartTypeConnector<P extends PartTypeConnector<P, S>, S extends PartTypeConnector.State<P>> extends PartTypeBase<P, S> {

    public PartTypeConnector(String name, PartRenderPosition partRenderPosition) {
        super(name, partRenderPosition);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus(blockConfig);
    }

    @Override
    protected boolean hasGui() {
        return false;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return null;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return null;
    }

    @Override
    public void afterNetworkReAlive(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.afterNetworkReAlive(network, partNetwork, target, state);
        state.setPosition(target.getCenter());
    }

    @Override
    public void onNetworkAddition(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.onNetworkAddition(network, partNetwork, target, state);
        state.setPosition(target.getCenter());
    }

    public static abstract class State<P extends PartTypeConnector> extends PartStateBase<P> implements IPathElement {

        private PartPos partPos;

        protected PartPos getPartPos() {
            return partPos;
        }

        @Override
        public DimPos getPosition() {
            return this.partPos == null ? null : this.partPos.getPos();
        }

        public void setPosition(PartPos partPos) {
            this.partPos = partPos;
        }

        @Override
        public int compareTo(IPathElement o) {
            return getPosition().compareTo(o.getPosition());
        }

        @Override
        public boolean hasCapability(Capability<?> capability) {
            return capability == PathElementConfig.CAPABILITY || super.hasCapability(capability);
        }

        @Override
        public <T> T getCapability(Capability<T> capability) {
            if (capability == PathElementConfig.CAPABILITY) {
                return (T) this;
            }
            return super.getCapability(capability);
        }

    }
}
