package org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable;

import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.ThaumcraftModCompat;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;

/**
 * A list proxy for an aspect container at a certain position.
 */
public class ValueTypeListProxyPositionedAspectContainer extends ValueTypeListProxyBase<ValueObjectTypeAspect, ValueObjectTypeAspect.ValueAspect> implements INBTProvider {

    @NBTPersist
    private DimPos pos;

    public ValueTypeListProxyPositionedAspectContainer() {
        this(null);
    }

    public ValueTypeListProxyPositionedAspectContainer(DimPos pos) {
        super(ValueTypeListProxyFactories.POSITIONED_INVENTORY.getName(), ThaumcraftModCompat.OBJECT_ASPECT);
        this.pos = pos;
    }

    protected IAspectContainer getContainer() {
        return TileHelpers.getSafeTile(pos.getWorld(), pos.getBlockPos(), IAspectContainer.class);
    }

    @Override
    public int getLength() {
        IAspectContainer container = getContainer();
        if(container == null) {
            return 0;
        }
        return container.getAspects().size();
    }

    @Override
    public ValueObjectTypeAspect.ValueAspect get(int index) {
        IAspectContainer container = getContainer();
        AspectList aspects = container.getAspects();
        Aspect aspect = aspects.getAspects()[index];
        int amount = aspects.getAmount(aspect);
        return ValueObjectTypeAspect.ValueAspect.of(aspect, amount);
    }

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {

    }
}
