package org.cyclops.integrateddynamics.core.evaluate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IExpressionVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionTrees;
import org.cyclops.integrateddynamics.core.item.ExpressionVariableFacade;

/**
 * Handler for expression variable facades.
 * @author rubensworks
 */
public class ExpressionVariableFacadeHandler implements IVariableFacadeHandler<IExpressionVariableFacade> {

    public static final String KEY_EXPRESSION = "expression";

    private static final IExpressionVariableFacade INVALID_FACADE = new ExpressionVariableFacade(false, null);
    private static ExpressionVariableFacadeHandler _instance;

    private ExpressionVariableFacadeHandler() {

    }

    public static ExpressionVariableFacadeHandler getInstance() {
        if(_instance == null) _instance = new ExpressionVariableFacadeHandler();
        return _instance;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "expression");
    }

    @Override
    public IExpressionVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        if(!tag.contains(KEY_EXPRESSION, Tag.TAG_COMPOUND)) {
            return INVALID_FACADE;
        }
        try {
            return new ExpressionVariableFacade(id, ExpressionTrees.deserialize(valueDeseralizationContext, tag.getCompound(KEY_EXPRESSION)));
        } catch (EvaluationException e) {
            return INVALID_FACADE;
        }
    }

    @Override
    public void setVariableFacade(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, IExpressionVariableFacade variableFacade) {
        if (variableFacade.getExpression() != null) {
            tag.put(KEY_EXPRESSION, ExpressionTrees.serialize(valueDeseralizationContext, variableFacade.getExpression()));
        }
    }

    @Override
    public boolean isInstance(IVariableFacade variableFacade) {
        return variableFacade instanceof IExpressionVariableFacade;
    }

    @Override
    public boolean isInstance(IVariable<?> variable) {
        return variable instanceof IVariable;
    }
}
