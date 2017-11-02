package org.cyclops.integrateddynamics.core.logicprogrammer;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeString;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.item.ItemVariable;

import java.util.List;

/**
 * Element for value type.
 * @author rubensworks
 */
public abstract class ValueTypeLPElementBase implements IValueTypeLogicProgrammerElement<ISubGuiBox, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> {

    @Getter
    private final IValueType<?> valueType;
    @Getter
    private GuiElementValueTypeString<GuiLogicProgrammerBase, ContainerLogicProgrammerBase> innerGuiElement;

    public ValueTypeLPElementBase(IValueType<?> valueType) {
        this.valueType = valueType;
        this.innerGuiElement = new GuiElementValueTypeString<>(this.valueType, getRenderPattern());
    }

    @Override
    public void loadTooltip(List<String> lines) {
        getInnerGuiElement().loadTooltip(lines);
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
    public boolean matchesInput(IValueType<?> valueType) {
        return false;
    }

    @Override
    public boolean matchesOutput(IValueType<?> valueType) {
        return ValueHelpers.correspondsTo(valueType, valueType);
    }

    @Override
    public String getLocalizedNameFull() {
        return L10NHelpers.localize(valueType.getUnlocalizedName());
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE;
    }

    @Override
    public void onInputSlotUpdated(int slotId, ItemStack itemStack) {

    }

    @Override
    public boolean isFor(IVariableFacade variableFacade) {
        if (variableFacade instanceof IValueTypeVariableFacade) {
            IValueTypeVariableFacade valueTypeFacade = (IValueTypeVariableFacade) variableFacade;
            if (valueTypeFacade.isValid()) {
                return getValueType() == valueTypeFacade.getValueType();
            }
        }
        return false;
    }

    @Override
    public boolean canWriteElementPre() {
        return true;
    }

    @Override
    public ItemStack writeElement(EntityPlayer player, ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(!MinecraftHelpers.isClientSide(), itemStack, ValueTypes.REGISTRY,
                new ValueTypeVariableFacadeFactory(getValueType(), getValue()), player, BlockLogicProgrammer.getInstance());
    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return true;
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public int getColor() {
        return valueType.getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return L10NHelpers.localize(valueType.getUnlocalizedName());
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return itemStack.getItem() == ItemVariable.getInstance();
    }

    @Override
    public int getItemStackSizeLimit() {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFocused(ISubGuiBox subGui) {
        if (subGui instanceof ValueTypeLPElementRenderPattern) {
            return ((ValueTypeLPElementRenderPattern) subGui).getSearchField().isFocused();
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setFocused(ISubGuiBox subGui, boolean focused) {
        if (subGui instanceof ValueTypeLPElementRenderPattern) {
            ((ValueTypeLPElementRenderPattern) subGui).getSearchField().setFocused(focused);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public abstract ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container);

    @Override
    @SideOnly(Side.CLIENT)
    public void setValueInGui(ISubGuiBox subGui) {

    }

    protected static class ValueTypeVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade> {

        private final IValueType valueType;
        private final IValue value;

        public ValueTypeVariableFacadeFactory(IValueType valueType, IValue value) {
            this.valueType = valueType;
            this.value = value;
        }

        public ValueTypeVariableFacadeFactory(IValue value) {
            this(value.getType(), value);
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
