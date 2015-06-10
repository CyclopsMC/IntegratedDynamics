package org.cyclops.integrateddynamics.core.part.write;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.client.gui.GuiPartWriter;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartWriter;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Set;

/**
 * An abstract {@link org.cyclops.integrateddynamics.core.part.write.IPartTypeWriter}.
 * @author rubensworks
 */
public abstract class PartTypeWriteBase<P extends IPartTypeWriter<P, S>, S extends IPartState<P>>
        extends PartTypeBase<P, S> implements IPartTypeWriter<P, S> {

    public PartTypeWriteBase(String name) {
        super(name);
    }

    @Override
    public Set<IAspectWrite> getWriteAspects() {
        return Aspects.REGISTRY.getWriteAspects(this);
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
