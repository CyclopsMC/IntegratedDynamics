package org.cyclops.integrateddynamics.core.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

import java.util.List;

/**
 * Base implementation of {@link IVariableFacade}
 * @author rubensworks
 */
public abstract class VariableFacadeBase implements IVariableFacade {

    private final int id;

    public VariableFacadeBase(boolean generateId) {
        this.id = generateId ? IntegratedDynamics.globalCounters.getNext("variable") : -1;
    }

    public VariableFacadeBase(int id) {
        this.id = id;
    }

    @Override
    public final int getId() {
        return this.id;
    }

    @Override
    public String getLabel() {
        return LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(getId());
    }

    protected String getReferenceDisplay(int variableId) {
        String label = LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(variableId);
        if(label == null) {
            return String.valueOf(variableId);
        } else {
            return String.format("%s:%s", label, variableId);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(List<String> list, EntityPlayer entityPlayer) {
        list.add(L10NHelpers.localize("item.items.integrateddynamics.variable.id", getId() == -1 ? "..." : getId()));
    }

}
