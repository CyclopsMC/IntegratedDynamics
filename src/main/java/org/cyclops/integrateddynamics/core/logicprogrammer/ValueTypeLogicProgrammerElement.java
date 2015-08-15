package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
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
public class ValueTypeLogicProgrammerElement extends ValueTypeGuiElement<GuiLogicProgrammer, ContainerLogicProgrammer> implements ILogicProgrammerElement {

    public ValueTypeLogicProgrammerElement(IValueType valueType) {
        super(valueType);
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
        return L10NHelpers.localize(getValueType().getUnlocalizedName());
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
        return getInputString() != null && !getInputString().isEmpty();
    }

    @Override
    public ItemStack writeElement(ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        return registry.writeVariableFacadeItem(!MinecraftHelpers.isClientSide(), itemStack, ValueTypes.REGISTRY, new ValueTypeVariableFacadeFactory(getValueType(), getInputString()));
    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return this.getInputString() == null || this.getInputString().equals(getDefaultInputString());
    }

    @Override
    public void activate() {
        setInputString(new String(getDefaultInputString()));
    }

    @Override
    public void deactivate() {
        setInputString(null);
    }

    @Override
    public L10NHelpers.UnlocalizedString validate() {
        return getValueType().canDeserialize(getInputString());
    }

    @Override
    public int getColor() {
        return getValueType().getDisplayColor();
    }

    @Override
    public String getSymbol() {
        return L10NHelpers.localize(getValueType().getUnlocalizedName());
    }

    @Override
    public boolean isFor(IVariableFacade variableFacade) {
        if (variableFacade instanceof ValueTypeVariableFacade) {
            ValueTypeVariableFacade valueTypeFacade = (ValueTypeVariableFacade) variableFacade;
            if (valueTypeFacade.isValid()) {
                return getValueType() == valueTypeFacade.getValueType();
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiRenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammer gui, ContainerLogicProgrammer container) {
        return new SubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
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

    @SideOnly(Side.CLIENT)
    protected class SubGuiRenderPattern extends ValueTypeGuiElement<GuiLogicProgrammer, ContainerLogicProgrammer>.SubGuiRenderPattern {

        public SubGuiRenderPattern(ValueTypeLogicProgrammerElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                   GuiLogicProgrammer gui, ContainerLogicProgrammer container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
            IValueType valueType = element.getValueType();

            // Output type tooltip
            if(!container.hasWriteItemInSlot()) {
                if(gui.isPointInRegion(ContainerLogicProgrammer.OUTPUT_X, ContainerLogicProgrammer.OUTPUT_Y,
                        GuiLogicProgrammer.BOX_HEIGHT, GuiLogicProgrammer.BOX_HEIGHT, mouseX, mouseY)) {
                    List<String> lines = Lists.newLinkedList();
                    lines.add(valueType.getDisplayColorFormat() + L10NHelpers.localize(valueType.getUnlocalizedName()));
                    gui.drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

    }

}
