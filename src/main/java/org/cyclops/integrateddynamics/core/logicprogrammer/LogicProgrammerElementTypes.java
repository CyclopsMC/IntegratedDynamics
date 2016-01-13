package org.cyclops.integrateddynamics.core.logicprogrammer;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementTypeRegistry;

/**
 * All types of logic programmer element types.
 * @author rubensworks
 */
public class LogicProgrammerElementTypes {

    public static final ILogicProgrammerElementTypeRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(ILogicProgrammerElementTypeRegistry.class);

    public static void load() {}

    // General types
    public static final OperatorElementType  OPERATOR  = REGISTRY.addType(new OperatorElementType());
    public static final ValueTypeElementType VALUETYPE = REGISTRY.addType(new ValueTypeElementType());

    // Specific types
    public static final ValueTypeListElementType             LIST_ELEMENT_TYPE      = REGISTRY.addType(new ValueTypeListElementType());
    public static final ValueObjectTypeBlockElementType      OBJECT_BLOCK_TYPE      = REGISTRY.addType(new ValueObjectTypeBlockElementType());
    public static final ValueObjectTypeItemStackElementType  OBJECT_ITEMSTACK_TYPE  = REGISTRY.addType(new ValueObjectTypeItemStackElementType());
    public static final ValueObjectTypeFluidStackElementType OBJECT_FLUIDSTACK_TYPE = REGISTRY.addType(new ValueObjectTypeFluidStackElementType());

    public static boolean areEqual(ILogicProgrammerElement e1, ILogicProgrammerElement e2) {
        if(e1 == null) {
            return e2 == null;
        }
        if(e2 == null) {
            return false;
        }
        return e1.getType().getName().equals(e2.getType().getName()) && e1.getType().getName(e1).equals(e2.getType().getName(e2));
    }

}
