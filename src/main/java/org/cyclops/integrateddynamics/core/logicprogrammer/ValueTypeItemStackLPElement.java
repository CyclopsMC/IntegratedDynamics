package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Element for a value type that can be derived from an {@link ItemStack}.
 * @author rubensworks
 */
public class ValueTypeItemStackLPElement<V extends IValue> extends ValueTypeLPElementBase {

    private final IItemStackToValue<V> itemStackToValue;
    private ItemStack itemStack = ItemStack.EMPTY;

    public ValueTypeItemStackLPElement(IValueType valueType, IItemStackToValue<V> itemStackToValue) {
        super(valueType);
        this.itemStackToValue = itemStackToValue;
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.SINGLE_SLOT;
    }

    @Override
    public void onInputSlotUpdated(int slotId, ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean canWriteElementPre() {
        return this.itemStackToValue.isNullable() || !this.itemStack.isEmpty();
    }

    @Override
    public void activate() {
        this.itemStack = ItemStack.EMPTY;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public L10NHelpers.UnlocalizedString validate() {
        if(!this.itemStackToValue.isNullable() && this.itemStack.isEmpty()) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDINPUTITEM);
        }
        return itemStackToValue.validate(itemStack);
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return true;
    }

    @Override
    public IValue getValue() {
        return this.itemStackToValue.getValue(this.itemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new SubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setValueInGui(ISubGuiBox subGui) {
        ((ValueTypeItemStackLPElement.SubGuiRenderPattern) subGui).container.getTemporaryInputSlots().setInventorySlotContents(0, this.itemStack);
    }

    @SideOnly(Side.CLIENT)
    protected static class SubGuiRenderPattern extends RenderPattern<ValueTypeItemStackLPElement, GuiLogicProgrammerBase, ContainerLogicProgrammerBase>
            implements IRenderPatternValueTypeTooltip {

        private boolean renderTooltip = true;

        public SubGuiRenderPattern(ValueTypeItemStackLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                   GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
            this.drawTooltipForeground(gui, container, guiLeft, guiTop, mouseX, mouseY, element.getValueType());
        }

        @Override
        public boolean isRenderTooltip() {
            return this.renderTooltip;
        }

        @Override
        public void setRenderTooltip(boolean renderTooltip) {
            this.renderTooltip = renderTooltip;
        }

    }

    public static interface IItemStackToValue<V extends IValue> {

        public boolean isNullable();
        public L10NHelpers.UnlocalizedString validate(ItemStack itemStack);
        public V getValue(ItemStack itemStack);

    }

}
