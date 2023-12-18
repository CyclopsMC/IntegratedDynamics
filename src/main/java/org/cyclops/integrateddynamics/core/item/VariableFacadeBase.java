package org.cyclops.integrateddynamics.core.item;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(List<Component> list, Level world) {
        list.add(Component.translatable("item.integrateddynamics.variable.id", getId() == -1 ? "..." : getId()));
    }

    @Deprecated
    public <V extends IValue> IVariable<V> getVariable(IPartNetwork network) {
        throw new UnsupportedOperationException("This method has been deprecated");
    }

    @Deprecated
    public void validate(IPartNetwork network, IValidator validator, IValueType containingValueType) {
        throw new UnsupportedOperationException("This method has been deprecated");
    }

}
