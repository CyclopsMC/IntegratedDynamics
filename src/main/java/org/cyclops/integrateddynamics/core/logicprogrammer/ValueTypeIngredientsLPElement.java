package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.commoncapabilities.api.ingredient.IMixedIngredients;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.commoncapabilities.api.ingredient.MixedIngredients;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonArrow;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.IInputListener;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetArrowedListField;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandler;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerSetElementInventory;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeIngredientsValueChangedPacket;

import java.util.List;
import java.util.Map;

/**
 * Element for the ingredients value type.
 * @author rubensworks
 */
public class ValueTypeIngredientsLPElement extends ValueTypeLPElementBase {

    private IngredientComponent currentType = IngredientComponent.ITEMSTACK;
    private Map<IngredientComponent, Integer> lengths = Maps.newHashMap();
    private Map<IngredientComponent, Map<Integer, IValueTypeLogicProgrammerElement>> subElements = Maps.newHashMap();
    private Map<IngredientComponent, Map<Integer, RenderPattern>> subElementGuis = Maps.newHashMap();
    private int activeElement = -1;
    @OnlyIn(Dist.CLIENT)
    private MasterSubGuiRenderPattern masterGui;

    private ValueObjectTypeIngredients.ValueIngredients serverValue = null;

    public ValueTypeIngredientsLPElement() {
        super(ValueTypes.OBJECT_INGREDIENTS);
    }

    public void setServerValue(ValueObjectTypeIngredients.ValueIngredients serverValue) {
        this.serverValue = serverValue;
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.VALUETYPE;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE_CANVAS;
    }

    @Override
    public boolean canWriteElementPre() {
        return true;
    }

    protected IMixedIngredients constructValues() {
        Map<IngredientComponent<?, ?>, List<?>> lists = Maps.newIdentityHashMap();
        for (IngredientComponent<?, ?> component : IngredientComponentHandlers.REGISTRY.getComponents()) {
            List values = Lists.newArrayListWithExpectedSize(lengths.get(component));
            subElements.get(component).entrySet().forEach(entry -> {
                IIngredientComponentHandler componentHandler = IngredientComponentHandlers.REGISTRY.getComponentHandler(component);
                try {
                    values.add(componentHandler.toInstance(entry.getValue().getValue()));
                } catch (Exception e) {
                    values.add(component.getMatcher().getEmptyInstance());
                }
            });
            if (!values.isEmpty()) {
                lists.put(component, values);
            }
        }
        return new MixedIngredients(lists);
    }

    @Override
    public IValue getValue() {
        return MinecraftHelpers.isClientSideThread()
                ? ValueObjectTypeIngredients.ValueIngredients.of(constructValues()) : serverValue;
    }

    public int getLength() {
        return lengths.get(currentType);
    }

    public void setLength(int length) {
        lengths.put(currentType, length);
        setActiveElement(getLength() - 1);
    }

    public void setCurrentType(IngredientComponent currentType) {
        this.currentType = currentType;
        setActiveElement(subElements.get(currentType).size() - 1);
    }

    public void setActiveElement(int index) {
        activeElement = index;
        if(index >= 0 && !subElements.get(currentType).containsKey(index)) {
            subElements.get(currentType).put(index, IngredientComponentHandlers.REGISTRY.getComponentHandler(currentType)
                    .getValueType().createLogicProgrammerElement());
        }
        masterGui.setActiveElement(activeElement);
        masterGui.container.onDirty();
    }

    public void removeElement(int index) {
        Map<Integer, IValueTypeLogicProgrammerElement> oldSubElements = subElements.get(currentType);
        Map<Integer, RenderPattern> oldSubElementGuis = subElementGuis.get(currentType);
        subElements.put(currentType, Maps.newHashMap());
        subElementGuis.put(currentType, Maps.newHashMap());
        for(Map.Entry<Integer, IValueTypeLogicProgrammerElement> entry : oldSubElements.entrySet()) {
            int i = entry.getKey();
            if(i < index) {
                subElements.get(currentType).put(i, entry.getValue());
                subElementGuis.get(currentType).put(i, oldSubElementGuis.get(i));
            } else if(i > index) {
                subElements.get(currentType).put(i - 1, entry.getValue());
                subElementGuis.get(currentType).put(i - 1, oldSubElementGuis.get(i));
            }
        }
        setLength(getLength() - 1);
    }

    @Override
    public void activate() {
        for (IngredientComponent recipeComponent : IngredientComponentHandlers.REGISTRY.getComponents()) {
            subElements.put(recipeComponent, Maps.newHashMap());
            subElementGuis.put(recipeComponent, Maps.newHashMap());
            lengths.put(recipeComponent, 0);
        }
    }

    @Override
    public void deactivate() {

    }

    @Override
    public ITextComponent validate() {
        if(!MinecraftHelpers.isClientSideThread()) {
            return serverValue == null ? new StringTextComponent("") : null;
        }
        if(MinecraftHelpers.isClientSideThread()) {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeIngredientsValueChangedPacket(
                            ValueObjectTypeIngredients.ValueIngredients.of(constructValues())));
        }
        for (Map<Integer, IValueTypeLogicProgrammerElement> componentValues : subElements.values()) {
            for(Map.Entry<Integer, IValueTypeLogicProgrammerElement> entry : componentValues.entrySet()) {
                ITextComponent error = entry.getValue().validate();
                if(error != null) {
                    return new TranslationTextComponent(L10NValues.VALUETYPE_ERROR_INVALIDLISTELEMENT, entry.getKey(), error);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
        return (slotId == 0 && super.isItemValidForSlot(slotId, itemStack)) ||
                (activeElement >= 0 && subElements.get(currentType).containsKey(activeElement)
                        && subElements.get(currentType).get(activeElement).isItemValidForSlot(slotId, itemStack));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return masterGui = new MasterSubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    /**
     * Sub gui that holds the list element value type panel and the panel for browsing through the elements.
     */
    @OnlyIn(Dist.CLIENT)
    protected static class MasterSubGuiRenderPattern extends RenderPattern<ValueTypeIngredientsLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
            implements IRenderPatternValueTypeTooltip {

        private final int baseX;
        private final int baseY;
        private final int maxWidth;
        private final int maxHeight;
        private final ContainerScreenLogicProgrammerBase gui;
        private final ContainerLogicProgrammerBase container;

        protected ListElementSubGui elementSubGui = null;
        protected int lastGuiLeft;
        protected int lastGuiTop;
        private boolean renderTooltip = true;

        public MasterSubGuiRenderPattern(ValueTypeIngredientsLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                         ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
            subGuiHolder.addSubGui(new SelectionSubGui(element, baseX, baseY, maxWidth, maxHeight, gui, container));
            this.baseX = baseX;
            this.baseY = baseY;
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.gui = gui;
            this.container = container;
        }

        public void setActiveElement(int index) {
            if(elementSubGui != null) {
                subGuiHolder.removeSubGui(elementSubGui);
            }
            if(index >= 0) {
                subGuiHolder.addSubGui(elementSubGui = new ListElementSubGui(element, baseX, baseY + (getHeight() / 4),
                        maxWidth, maxHeight, gui, container));
                elementSubGui.init(lastGuiLeft, lastGuiTop);
            }
        }

        @Override
        public void init(int guiLeft, int guiTop) {
            super.init(guiLeft, guiTop);
            lastGuiLeft = guiLeft;
            lastGuiTop = guiTop;
        }

        @Override
        public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

            // Output type tooltip
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

    /**
     * Selection panel for the type.
     */
    @OnlyIn(Dist.CLIENT)
    protected static class SelectionSubGui extends RenderPattern<ValueTypeIngredientsLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> implements IInputListener {

        private WidgetArrowedListField<IngredientComponent> valueTypeSelector = null;
        private Button arrowAdd;

        public SelectionSubGui(ValueTypeIngredientsLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                               ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public int getHeight() {
            return super.getHeight() / 4;
        }

        protected static List<IngredientComponent> getValueTypes() {
            return Lists.newArrayList(IngredientComponentHandlers.REGISTRY.getComponents());
        }

        @Override
        public void init(int guiLeft, int guiTop) {
            super.init(guiLeft, guiTop);
            valueTypeSelector = new WidgetArrowedListField<IngredientComponent>(Minecraft.getInstance().font,
                    getX() + guiLeft + getWidth() / 2 - 50, getY() + guiTop + 2, 100, 15, true, new TranslationTextComponent("valuetype.integrateddynamics.value_type"), true, getValueTypes()) {
                @Override
                protected String activeElementToString(IngredientComponent element) {
                    return L10NHelpers.localize(element.getTranslationKey());
                }
            };
            valueTypeSelector.setListener(this);
            //onChanged();
            int x = guiLeft + getX();
            int y = guiTop + getY();
            buttonList.add(arrowAdd = new ButtonText(x + getWidth() - 13, y + getHeight() - 13, 12, 12, new TranslationTextComponent("gui.integrateddynamics.button.add"), new StringTextComponent("+"), (b) -> {}, true));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            return super.mouseClicked(mouseX, mouseY, mouseButton) || valueTypeSelector.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        protected void actionPerformed(Button guibutton) {
            super.actionPerformed(guibutton);
            if(guibutton == arrowAdd) {
                element.setLength(element.getLength() + 1);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            valueTypeSelector.renderButton(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void onChanged() {
            element.setCurrentType(valueTypeSelector.getActiveElement());
        }
    }

    /**
     * Panel for browsing through the list elements and updating them.
     */
    @OnlyIn(Dist.CLIENT)
    protected static class ListElementSubGui extends RenderPattern<ValueTypeIngredientsLPElement, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase> {

        private ButtonArrow arrowLeft;
        private ButtonArrow arrowRight;
        private Button arrowRemove;

        public ListElementSubGui(ValueTypeIngredientsLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                 ContainerScreenLogicProgrammerBase<?> gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
            RenderPattern subGui = element.subElementGuis.get(element.currentType).get(element.activeElement);
            IValueTypeLogicProgrammerElement subElement = element.subElements.get(element.currentType).get(element.activeElement);
            if(subGui == null) {
                subGui = (RenderPattern) subElement.createSubGui(baseX, baseY, maxWidth,
                        maxHeight / 3 * 2, gui, container);
                element.subElementGuis.get(element.currentType).put(
                        element.activeElement,
                        subGui);
            }
            int x = getX() + baseX - 24;
            int y = getY() + baseY - 23;
            gui.getMenu().setElementInventory(subElement, x, y);
            subElement.setValueInGui(subGui);
            subGuiHolder.addSubGui(subGui);

            // Do the same thing server-side
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerSetElementInventory(
                            IngredientComponentHandlers.REGISTRY.getComponentHandler(element.currentType).getValueType(), x, y));
        }

        @Override
        public int getHeight() {
            return (super.getHeight() / 4) * 3;
        }

        @Override
        public void init(int guiLeft, int guiTop) {
            super.init(guiLeft, guiTop);
            int x = guiLeft + getX();
            int y = guiTop + getY();
            buttonList.add(arrowLeft = new ButtonArrow(x, y, new TranslationTextComponent("gui.cyclopscore.left"),
                    b -> element.setActiveElement(element.activeElement - 1), ButtonArrow.Direction.WEST));
            buttonList.add(arrowRight = new ButtonArrow(x + getWidth() - arrowLeft.getWidth() - 1, y, new TranslationTextComponent("gui.cyclopscore.right"),
                    b -> element.setActiveElement(element.activeElement + 1), ButtonArrow.Direction.EAST));
            buttonList.add(arrowRemove = new ButtonText(x + (getWidth() / 2) - (arrowLeft.getWidth() / 2), y + getHeight() - 13, 12, 12, new TranslationTextComponent("gui.integrateddynamics.button.remove"), new StringTextComponent("-"),
                    b -> element.removeElement(element.activeElement), true));
            arrowLeft.active = element.activeElement > 0;
            arrowRight.active = element.activeElement < element.getLength() - 1;
            arrowRemove.active = element.getLength() > 0;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(matrixStack, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            int x = guiLeft + getX() + (getWidth() / 2);
            int y = guiTop + getY() + 4;
            RenderHelpers.drawScaledCenteredString(matrixStack, fontRenderer, String.valueOf(element.activeElement), x - 4, y + 2, 10, Helpers.RGBToInt(20, 20, 20));
        }
    }

}
