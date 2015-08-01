package org.cyclops.integrateddynamics.core.item;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.client.model.VariableModelBaked;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;

import java.util.List;

/**
 * A facade for retrieving a variable.
 * @author rubensworks
 */
public interface IVariableFacade {

    /**
     * @return The unique id for this facade.
     */
    public int getId();

    /**
     * @return The optional label for this facade.
     */
    public String getLabel();

    /**
     * Get the variable.
     * @param network The object used to look for the variable.
     * @return The variable.
     */
    public <V extends IValue> IVariable<V> getVariable(Network network);

    /**
     * @return If this is a valid reference.
     */
    public boolean isValid();

    /**
     * Check if this facade is valid, otherwise notify the validator of any errors.
     * @param network The object used to look for the variable.
     * @param validator The object to notify errors to.
     * @param containingValueType The value type in which this variable facade is being used.
     */
    public void validate(Network network, Validator validator, IValueType containingValueType);

    /**
     * Add information about this variable facade to the list.
     * @param list The list to add lines to.
     * @param entityPlayer The player that will see the information.
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(List<String> list, EntityPlayer entityPlayer);

    /**
     * Handle the quads for the given baked model.
     * @param variableModelBaked The baked model.
     * @param quads The quads that can be added to.
     */
    @SideOnly(Side.CLIENT)
    public void addModelOverlay(VariableModelBaked variableModelBaked, List<BakedQuad> quads);

    public static class Validator {

        private final IPartStateWriter state;
        private final IAspectWrite aspect;

        /**
         * Make a new instance
         * @param state The part state.
         * @param aspect The aspect to set the error for.
         */
        public Validator(IPartStateWriter state, IAspectWrite aspect) {
            this.state = state;
            this.aspect = aspect;
        }

        /**
         * Set the current error for the given aspect.
         * @param error The error to set, or null to clear.
         */
        public void addError(L10NHelpers.UnlocalizedString error) {
            this.state.addError(aspect, error);
        }

    }

}
