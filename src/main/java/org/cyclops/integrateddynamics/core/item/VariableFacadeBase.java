package org.cyclops.integrateddynamics.core.item;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Base implementation of {@link IVariableFacade}
 * @author rubensworks
 */
@RequiredArgsConstructor
@Data
public abstract class VariableFacadeBase implements IVariableFacade {

    private final int id;

    public VariableFacadeBase(boolean generateId) {
        this.id = generateId ? generateId() : -1;
    }

    /**
     * @return A unique new variable id.
     */
    public static int generateId() {
        return IntegratedDynamics.globalCounters.getNext("variable");
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
    public void addInformation(List<String> list, World world) {
        list.add(L10NHelpers.localize("item.items.integrateddynamics.variable.id", getId() == -1 ? "..." : getId()));
    }

}
