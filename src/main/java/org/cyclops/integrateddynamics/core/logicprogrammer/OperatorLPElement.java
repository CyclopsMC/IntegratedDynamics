package org.cyclops.integrateddynamics.core.logicprogrammer;

import lombok.Data;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IOperatorVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.item.OperatorVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.util.List;

/**
 * Element for operator.
 *
 * @author rubensworks
 */
@Data
public class OperatorLPElement implements ILogicProgrammerElement<RenderPattern, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> {

    private final IOperator operator;
    private IVariableFacade[] inputVariables;
    private boolean focused = false;

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.OPERATOR;
    }

    @Override
    public String getMatchString() {
        return getOperator().getLocalizedNameFull().toLowerCase();
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
    public String getLocalizedNameFull() {
        return getOperator().getLocalizedNameFull();
    }

    @Override
    public void loadTooltip(List<String> lines) {
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
    public void onInputSlotUpdated(int slotId, ItemStack itemStack) {
        IVariableFacade variableFacade = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).handle(itemStack);
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
    public ItemStack writeElement(EntityPlayer player, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        int[] variableIds = getVariableIds(inputVariables);
        return registry.writeVariableFacadeItem(!MinecraftHelpers.isClientSide(), itemStack, Operators.REGISTRY, new OperatorVariableFacadeFactory(operator, variableIds), player, BlockLogicProgrammer.getInstance());
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
    public L10NHelpers.UnlocalizedString validate() {
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
        return itemStack.getItem() == ItemVariable.getInstance();
    }

    @Override
    public boolean slotClick(int slotId, Slot slot, int mouseButton, ClickType clickType, EntityPlayer player) {
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
    @SideOnly(Side.CLIENT)
    public RenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
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
