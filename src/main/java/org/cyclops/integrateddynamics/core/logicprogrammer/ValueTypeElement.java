package org.cyclops.integrateddynamics.core.logicprogrammer;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeGuiElement;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeSubGuiRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.util.List;

/**
 * Element for value type.
 * @author rubensworks
 */
public class ValueTypeElement implements ILogicProgrammerElement<SubGuiConfigRenderPattern, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> {

    @Getter
    private ValueTypeGuiElement<GuiLogicProgrammerBase, ContainerLogicProgrammerBase> innerGuiElement;

    public ValueTypeElement(IValueType valueType) {
        innerGuiElement = new ValueTypeGuiElement<>(valueType, getRenderPattern());
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
    public boolean matchesInput(IValueType valueType) {
        return false;
    }

    @Override
    public boolean matchesOutput(IValueType valueType) {
        return ValueHelpers.correspondsTo(getInnerGuiElement().getValueType(), valueType);
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
        return getInnerGuiElement().getInputString() != null;
    }

    @Override
    public ItemStack writeElement(EntityPlayer player, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(!MinecraftHelpers.isClientSide(), itemStack, ValueTypes.REGISTRY, new ValueTypeVariableFacadeFactory(innerGuiElement.getValueType(), innerGuiElement.getInputString()), player, BlockLogicProgrammer.getInstance());
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
        if (variableFacade instanceof IValueTypeVariableFacade) {
            IValueTypeVariableFacade valueTypeFacade = (IValueTypeVariableFacade) variableFacade;
            if (valueTypeFacade.isValid()) {
                return getInnerGuiElement().getValueType() == valueTypeFacade.getValueType();
            }
        }
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return itemStack.getItem() == ItemVariable.getInstance();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFocused(SubGuiConfigRenderPattern subGui) {
        if (subGui instanceof ValueTypeSubGuiRenderPattern) {
            return ((ValueTypeSubGuiRenderPattern) subGui).getSearchField().isFocused();
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setFocused(SubGuiConfigRenderPattern subGui, boolean focused) {
        if (subGui instanceof ValueTypeSubGuiRenderPattern) {
            ((ValueTypeSubGuiRenderPattern) subGui).getSearchField().setFocused(focused);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigRenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new ValueTypeElementSubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    /**
     * Set the value.
     * @param value The value.
     * @param activeElementSubGui The sub gui that is displaying the value
     */
    public void setValue(IValue value, ValueTypeSubGuiRenderPattern activeElementSubGui) {
        getInnerGuiElement().setInputString(getInnerGuiElement().getValueType().serialize(value), activeElementSubGui);
    }

    /**
     * @return The current value.
     */
    public IValue getValue() {
        return getInnerGuiElement().getValueType().deserialize(getInnerGuiElement().getInputString());
    }

    protected static class ValueTypeVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade> {

        private final IValueType valueType;
        private final String value;

        public ValueTypeVariableFacadeFactory(IValueType valueType, String value) {
            this.valueType = valueType;
            this.value = value;
        }

        @Override
        public IValueTypeVariableFacade create(boolean generateId) {
            return new ValueTypeVariableFacade(generateId, valueType, value);
        }

        @Override
        public IValueTypeVariableFacade create(int id) {
            return new ValueTypeVariableFacade(id, valueType, value);
        }
    }

}
