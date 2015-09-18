package org.cyclops.integrateddynamics.core.logicprogrammer;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammer;
import org.cyclops.integrateddynamics.core.evaluate.operator.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeGuiElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.IVariableFacade;
import org.cyclops.integrateddynamics.core.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

import java.util.List;

/**
 * Element for value type.
 * @author rubensworks
 */
public class ValueTypeElement implements ILogicProgrammerElement {

    @Getter
    private ValueTypeGuiElement<GuiLogicProgrammer, ContainerLogicProgrammer> innerGuiElement;

    public ValueTypeElement(IValueType valueType) {
        innerGuiElement = new ValueTypeGuiElement<>(valueType);
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public String getMatchString() {
        return getLocalizedNameFull().toLowerCase();
    }

    @Override
    public String getLocalizedNameFull() {
        return L10NHelpers.localize(getInnerGuiElement().getValueType().getUnlocalizedName());
    }

    @Override
    public void loadTooltip(List<String> lines) {
        getInnerGuiElement().loadTooltip(lines);
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE;
    }

    @Override
    public void onInputSlotUpdated(int slotId, ItemStack itemStack) {

    }

    @Override
    public boolean canWriteElementPre() {
        return getInnerGuiElement().getInputString() != null && !getInnerGuiElement().getInputString().isEmpty();
    }

    @Override
    public ItemStack writeElement(ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(!MinecraftHelpers.isClientSide(), itemStack, ValueTypes.REGISTRY, new ValueTypeVariableFacadeFactory(innerGuiElement.getValueType(), innerGuiElement.getInputString()));
    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return this.getInnerGuiElement().getInputString() == null || this.getInnerGuiElement().getInputString().equals(getInnerGuiElement().getDefaultInputString());
    }

    @Override
    public void activate() {
        getInnerGuiElement().setInputString(new String(innerGuiElement.getDefaultInputString()));
    }

    @Override
    public void deactivate() {
        getInnerGuiElement().setInputString(null);
    }

    @Override
    public L10NHelpers.UnlocalizedString validate() {
        return getInnerGuiElement().getValueType().canDeserialize(getInnerGuiElement().getInputString());
    }

    @Override
    public int getColor() {
        return getInnerGuiElement().getValueType().getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return L10NHelpers.localize(getInnerGuiElement().getValueType().getUnlocalizedName());
    }

    @Override
    public boolean isFor(IVariableFacade variableFacade) {
        if (variableFacade instanceof ValueTypeVariableFacade) {
            ValueTypeVariableFacade valueTypeFacade = (ValueTypeVariableFacade) variableFacade;
            if (valueTypeFacade.isValid()) {
                return getInnerGuiElement().getValueType() == valueTypeFacade.getValueType();
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigRenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammer gui, ContainerLogicProgrammer container) {
        return new ValueTypeElementSubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    protected static class ValueTypeVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<ValueTypeVariableFacade> {

        private final IValueType valueType;
        private final String value;

        public ValueTypeVariableFacadeFactory(IValueType valueType, String value) {
            this.valueType = valueType;
            this.value = value;
        }

        @Override
        public ValueTypeVariableFacade create(boolean generateId) {
            return new ValueTypeVariableFacade(generateId, valueType, value);
        }

        @Override
        public ValueTypeVariableFacade create(int id) {
            return new ValueTypeVariableFacade(id, valueType, value);
        }
    }

}
