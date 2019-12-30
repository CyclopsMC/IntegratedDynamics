package org.cyclops.integrateddynamics.api.item;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

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
    public @Nullable String getLabel();

    /**
     * Get the variable.
     * @param <V> The value type.
     * @param network The object used to look for the variable.
     * @return The variable.
     */
    public <V extends IValue> IVariable<V> getVariable(IPartNetwork network);

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
    public void validate(IPartNetwork network, IValidator validator, IValueType containingValueType);

    /**
     * @return The output type of this variable facade.
     */
    public IValueType getOutputType();

    /**
     * Add information about this variable facade to the list.
     * @param list The list to add lines to.
     * @param world The world.
     */
    @OnlyIn(Dist.CLIENT)
    public void addInformation(List<ITextComponent> list, World world);

    /**
     * Handle the quads for the given baked model.
     * @param variableModelBaked The baked model.
     * @param quads The quads that can be added to.
     * @param random A random instance.
     * @param modelData Model data.
     */
    @OnlyIn(Dist.CLIENT)
    public void addModelOverlay(IVariableModelBaked variableModelBaked, List<BakedQuad> quads, Random random, IModelData modelData);

    public static interface IValidator {

        /**
         * Set the current error for the given aspect.
         * @param error The error to set, or null to clear.
         */
        public void addError(ITextComponent error);

    }

}
