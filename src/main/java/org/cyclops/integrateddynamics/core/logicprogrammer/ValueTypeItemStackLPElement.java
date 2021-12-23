package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
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
    public ITextComponent validate() {
        if(!this.itemStackToValue.isNullable() && this.itemStack.isEmpty()) {
            return new TranslationTextComponent(L10NValues.VALUETYPE_ERROR_INVALIDINPUTITEM);
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
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new SubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setValueInGui(ISubGuiBox subGui) {
        ((ValueTypeItemStackLPElement.SubGuiRenderPattern) subGui).container.getTemporaryInputSlots().setItem(0, this.itemStack);
    }

    @OnlyIn(Dist.CLIENT)
    protected static class SubGuiRenderPattern extends RenderPattern<ValueTypeItemStackLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
            implements IRenderPatternValueTypeTooltip {

        private boolean renderTooltip = true;

        public SubGuiRenderPattern(ValueTypeItemStackLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
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
        public ITextComponent validate(ItemStack itemStack);
        public V getValue(ItemStack itemStack);

    }

}
