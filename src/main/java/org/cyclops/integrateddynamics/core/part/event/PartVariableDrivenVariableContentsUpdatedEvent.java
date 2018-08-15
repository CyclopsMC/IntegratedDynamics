package org.cyclops.integrateddynamics.core.part.event;

import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

import javax.annotation.Nullable;

/**
 * An event that is posted in the Forge event bus when the variable contents in a variable-driven part is updated.
 * @author rubensworks
 */
public class PartVariableDrivenVariableContentsUpdatedEvent<P extends IPartType<P, S>, S extends IPartState<P>> extends PartEvent<P, S> {

    @Nullable
    private final EntityPlayer entityPlayer;
    @Nullable
    private final IVariable variable;
    @Nullable
    private final IValue value;

    public PartVariableDrivenVariableContentsUpdatedEvent(INetwork network, IPartNetwork partNetwork, PartTarget target, P partType, S partState,
                                                          @Nullable EntityPlayer entityPlayer, IVariable variable, IValue value) {
        super(network, partNetwork, target, partType, partState);
        this.entityPlayer = entityPlayer;
        this.variable = variable;
        this.value = value;
    }

    @Nullable
    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    @Nullable
    public IValue getValue() {
        return value;
    }

    @Nullable
    public IVariable getVariable() {
        return variable;
    }
}
