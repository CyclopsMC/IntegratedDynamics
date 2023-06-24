package org.cyclops.integrateddynamics.core.logicprogrammer;

import lombok.Data;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

import java.util.List;

/**
 * Element for operator.
 *
 * @author rubensworks
 */
@Data
public class OperatorLPElement implements ILogicProgrammerElement<RenderPattern, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private final IOperator operator;
    private IVariableFacade[] inputVariables;
    private boolean focused = false;

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.OPERATOR;
    }

    @Override
    public String getMatchString() {
        return getOperator().getLocalizedNameFull().getString().toLowerCase();
    }

    @Override
    public boolean matchesInput(IValueType valueType) {
        for (IValueType operatorIn : getOperator().getInputTypes()) {
            if(ValueHelpers.correspondsTo(operatorIn, valueType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matchesOutput(IValueType valueType) {
        return ValueHelpers.correspondsTo(getOperator().getOutputType(), valueType);
    }

    @Override
    public Component getName() {
        return getOperator().getLocalizedNameFull();
    }

    @Override
    public void loadTooltip(List<Component> lines) {
        getOperator().loadTooltip(lines, true);
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        if (getOperator().getRenderPattern() == null) {
            throw new IllegalStateException("Tried to render a (possibly virtual) operator with a null render pattern: "
                    + getOperator().getUniqueName());
        }
        return getOperator().getRenderPattern();
    }

    @Override
    public void onInputSlotUpdated(Player player, int slotId, ItemStack itemStack) {
        IVariableFacade variableFacade = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).handle(ValueDeseralizationContext.of(player.level()), itemStack);
        inputVariables[slotId] = variableFacade;
    }

    @Override
    public boolean canWriteElementPre() {
        for (IVariableFacade inputVariable : inputVariables) {
            if (inputVariable == null || !inputVariable.isValid()) {
                return false;
            }
        }
        return true;
    }

    protected int[] getVariableIds(IVariableFacade[] inputVariables) {
        int[] variableIds = new int[inputVariables.length];
        for (int i = 0; i < inputVariables.length; i++) {
            variableIds[i] = inputVariables[i].getId();
        }
        return variableIds;
    }

    @Override
    public ItemStack writeElement(Player player, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        int[] variableIds = getVariableIds(inputVariables);
        return registry.writeVariableFacadeItem(!player.level().isClientSide(), itemStack, Operators.REGISTRY,
                new OperatorVariableFacadeFactory(operator, variableIds), player.level(), player, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.defaultBlockState());
    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return true;
    }

    @Override
    public void activate() {
        this.inputVariables = new IVariableFacade[getRenderPattern().getSlotPositions().length];
    }

    @Override
    public void deactivate() {
        this.inputVariables = null;
    }

    @Override
    public Component validate() {
        return getOperator().validateTypes(ValueHelpers.from(inputVariables));
    }

    @Override
    public int getColor() {
        return getOperator().getOutputType().getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return getOperator().getSymbol();
    }

    @Override
    public boolean isFor(IVariableFacade variableFacade) {
        if (variableFacade instanceof IOperatorVariableFacade) {
            IOperatorVariableFacade operatorFacade = (IOperatorVariableFacade) variableFacade;
            if (operatorFacade.isValid()) {
                return getOperator() == operatorFacade.getOperator();
            }
        }
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return itemStack.getItem() == RegistryEntries.ITEM_VARIABLE;
    }

    @Override
    public boolean slotClick(int slotId, Slot slot, int mouseButton, ClickType clickType, Player player) {
        return false;
    }

    @Override
    public int getItemStackSizeLimit() {
        return 1;
    }

    @Override
    public boolean isFocused(RenderPattern subGui) {
        return focused;
    }

    @Override
    public void setFocused(RenderPattern subGui, boolean focused) {
        this.focused = focused;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                      ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new OperatorLPElementRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    protected static class OperatorVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IOperatorVariableFacade> {

        private final IOperator operator;
        private final int[] variableIds;

        public OperatorVariableFacadeFactory(IOperator operator, int[] variableIds) {
            this.operator = operator;
            this.variableIds = variableIds;
        }

        @Override
        public IOperatorVariableFacade create(boolean generateId) {
            return new OperatorVariableFacade(generateId, operator, variableIds);
        }

        @Override
        public IOperatorVariableFacade create(int id) {
            return new OperatorVariableFacade(id, operator, variableIds);
        }
    }

}
