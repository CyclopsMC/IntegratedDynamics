package org.cyclops.integrateddynamics.core.item;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.client.model.VariableModelBaked;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.network.IVariableFacade;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.write.IPartStateWriter;

import java.util.List;

/**
 * The variable facade handler registry.
 * @author rubensworks
 */
public class VariableFacadeHandlerRegistry implements IVariableFacadeHandlerRegistry {

    private static VariableFacadeHandlerRegistry INSTANCE = new VariableFacadeHandlerRegistry();
    private static DummyVariableFacade DUMMY_FACADE = new DummyVariableFacade();

    private final List<IVariableFacadeHandler> handlers = Lists.newLinkedList();

    private VariableFacadeHandlerRegistry() {

    }

    /**
     * @return The unique instance.
     */
    public static VariableFacadeHandlerRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerHandler(IVariableFacadeHandler variableFacadeHandler) {
        handlers.add(variableFacadeHandler);
    }

    @Override
    public IVariableFacade handle(ItemStack itemStack) {
        for(IVariableFacadeHandler handler : handlers) {
            if(handler.canHandle(itemStack)) {
                return handler.getVariableFacade(itemStack);
            }
        }
        return DUMMY_FACADE;
    }

    /**
     * Variable facade used for items that have no (valid) information on them.
     */
    public static class DummyVariableFacade implements IVariableFacade {

        @Override
        public <V extends IValue> IVariable<V> getVariable(Network network) {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public void validate(Network network, IPartStateWriter validator) {
            validator.setError(validator.getActiveAspect(), new L10NHelpers.UnlocalizedString("aspect.error.invalidVariableItem"));
        }

        @Override
        public void addInformation(List<String> list, EntityPlayer entityPlayer) {

        }

        @Override
        public void addModelOverlay(VariableModelBaked variableModelBaked, List<BakedQuad> quads) {

        }
    }

}
