package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;

import java.util.List;

/**
 * Operator element type.
 * @author rubensworks
 */
public class OperatorLPElementType implements ILogicProgrammerElementType<OperatorLPElement> {

    @Override
    public OperatorLPElement getByName(ResourceLocation name) {
        return new OperatorLPElement(Operators.REGISTRY.getOperator(name));
    }

    @Override
    public ResourceLocation getName(OperatorLPElement element) {
        return element.getOperator().getUniqueName();
    }

    @Override
    public ResourceLocation getUniqueName() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "operator");
    }

    @Override
    public List<OperatorLPElement> createElements() {
        List<OperatorLPElement> elements = Lists.newArrayList();
        for(IOperator operator : Operators.REGISTRY.getOperators()) {
            elements.add(new OperatorLPElement(operator));
        }
        return elements;
    }
}
