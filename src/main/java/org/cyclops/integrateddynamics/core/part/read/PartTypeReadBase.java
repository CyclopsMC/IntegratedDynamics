package org.cyclops.integrateddynamics.core.part.read;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.client.gui.GuiPartReader;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.PartTypeAspects;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.List;

/**
 * An abstract {@link org.cyclops.integrateddynamics.core.part.read.IPartTypeReader}.
 * @author rubensworks
 */
public abstract class PartTypeReadBase<P extends IPartTypeReader<P, S>, S extends IPartStateReader<P>>
        extends PartTypeAspects<P, S> implements IPartTypeReader<P, S> {

    public PartTypeReadBase(String name) {
        super(name);
    }

    @Override
    public float getWidthFactor() {
        return 0.3125F;
    }

    @Override
    public Class<? super P> getPartTypeClass() {
        return IPartTypeReader.class;
    }

    @Override
    public List<IAspectRead> getReadAspects() {
        return Aspects.REGISTRY.getReadAspects(this);
    }

    @Override
    public <V extends IValue, T extends IValueType<V>> IAspectVariable<V> getVariable(PartTarget target, S partState,
                                                                                      IAspectRead<V, T> aspect) {
        if(!getAspects().contains(aspect)) {
            throw new IllegalArgumentException("Tried to get the variable for an aspect that did not exist within a " +
                    "part type.");
        }
        IAspectVariable<V> variable = partState.getVariable(aspect);
        if(variable == null) {
            variable = aspect.createNewVariable(target);
            partState.setVariable(aspect, variable);
        }
        return variable;
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
