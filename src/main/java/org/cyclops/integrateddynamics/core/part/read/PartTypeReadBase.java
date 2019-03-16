package org.cyclops.integrateddynamics.core.part.read;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.AspectUpdateType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.read.IPartStateReader;
import org.cyclops.integrateddynamics.api.part.read.IPartTypeReader;
import org.cyclops.integrateddynamics.client.gui.GuiPartReader;
import org.cyclops.integrateddynamics.core.part.PartTypeAspects;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

/**
 * An abstract {@link IPartTypeReader}.
 * @author rubensworks
 */
public abstract class PartTypeReadBase<P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>>
        extends PartTypeAspects<P, S> implements IPartTypeReader<P, S> {

    private List<IAspectRead> aspectsRead = null;
    private EnumMap<AspectUpdateType, Set<IAspectRead>> updateAspects = null;

    public PartTypeReadBase(String name) {
        this(name, new PartRenderPosition(0.1875F, 0.3125F, 0.625F, 0.625F));
    }

    public PartTypeReadBase(String name, PartRenderPosition partRenderPosition) {
        super(name, partRenderPosition);
    }

    protected Set<IAspectRead> getUpdateAspects(AspectUpdateType updateType) {
        if (updateAspects == null) {
            updateAspects = new EnumMap<>(AspectUpdateType.class);
            for (AspectUpdateType aspectUpdateType : AspectUpdateType.values()) {
                updateAspects.put(aspectUpdateType, Sets.newLinkedHashSet());
            }
            for (IAspect aspect : getAspects()) {
                if (aspect instanceof IAspectRead) {
                    IAspectRead aspectRead = (IAspectRead) aspect;
                    updateAspects.get(aspectRead.getUpdateType()).add(aspectRead);
                }
            }
        }

        return updateAspects.get(updateType);
    }

    @Override
    public boolean isSolid(S state) {
        return true;
    }

    @Override
    public Class<? super P> getPartTypeClass() {
        return IPartTypeReader.class;
    }

    @Override
    public void update(INetwork network, IPartNetwork partNetwork, PartTarget target, S state) {
        super.update(network, partNetwork, target, state);
        for(IAspect aspect : getUpdateAspects(AspectUpdateType.NETWORK_TICK)) {
            aspect.update(partNetwork, this, target, state);
        }
    }

    @Override
    public void onBlockNeighborChange(INetwork network, IPartNetwork partNetwork, PartTarget target, S state, IBlockAccess world, Block neighborBlock) {
        super.onBlockNeighborChange(network, partNetwork, target, state, world, neighborBlock);
        for(IAspect aspect : getUpdateAspects(AspectUpdateType.BLOCK_UPDATE)) {
            aspect.update(partNetwork, this, target, state);
        }
    }

    @Override
    public List<IAspectRead> getReadAspects() {
        if (aspectsRead == null) {
            aspectsRead = Aspects.REGISTRY.getReadAspects(this);
        }
        return aspectsRead;
    }

    @Override
    public <V extends IValue, T extends IValueType<V>> IAspectVariable<V> getVariable(PartTarget target, S partState,
                                                                                      IAspectRead<V, T> aspect) {
        IAspectVariable<V> variable = partState.getVariable(aspect);
        if(variable == null) {
            if(!getAspects().contains(aspect)) {
                throw new IllegalArgumentException(String.format("Tried to get the variable for the aspect %s that did not exist within the " +
                        "part type %s.", aspect.getTranslationKey(), this));
            }
            variable = aspect.createNewVariable(target);
            partState.setVariable(aspect, variable);
        }
        return variable;
    }

    @Override
    public void setTargetSideOverride(S state, @Nullable EnumFacing side) {
        EnumFacing lastSide = getTargetSideOverride(state);
        super.setTargetSideOverride(state, side);
        if (lastSide != side) {
            state.resetVariables();
        }
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPartReader.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiPartReader.class;
    }

}
