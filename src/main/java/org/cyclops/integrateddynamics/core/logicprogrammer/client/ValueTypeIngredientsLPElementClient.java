package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonArrow;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.IInputListener;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetArrowedListField;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;
import org.cyclops.integrateddynamics.core.logicprogrammer.IRenderPatternValueTypeTooltip;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeIngredientsLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerSetElementInventory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author rubensworks
 */
public class ValueTypeIngredientsLPElementClient extends ValueTypeLPElementBaseClient<ValueTypeIngredientsLPElement> {

    private ValueTypeIngredientsLPElementClient.MasterSubGuiRenderPattern masterGui;
    private Map<IngredientComponent, Map<Integer, RenderPattern>> subElementGuis = Maps.newHashMap();

    public ValueTypeIngredientsLPElementClient(ValueTypeIngredientsLPElement element) {
        super(element);
    }

    @Override
    public void setValueInGui(ISubGuiBox subGui) {
        if (!getElement().getSubElements().get(getElement().getCurrentType()).isEmpty()) {
            getElement().setActiveElement(0);
        }
    }

    @Override
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return masterGui = new ValueTypeIngredientsLPElementClient.MasterSubGuiRenderPattern(this.getElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    public void setActiveElement(int activeElement) {
        if (masterGui != null) {
            masterGui.setActiveElement(activeElement);
            masterGui.container.onDirty();
        }
    }

    public void removeElement(int index) {
        IngredientComponent currentType = getElement().getCurrentType();
        Map<Integer, RenderPattern> oldSubElementGuis = subElementGuis.get(currentType);
        subElementGuis.put(currentType, Maps.newHashMap());
        for (Map.Entry<Integer, RenderPattern> entry : oldSubElementGuis.entrySet()) {
            int i = entry.getKey();
            if (i < index) {
                subElementGuis.get(currentType).put(i, entry.getValue());
            } else if (i > index) {
                subElementGuis.get(currentType).put(i - 1, entry.getValue());
            }
        }
    }

    public void activate() {
        for (IngredientComponent recipeComponent : IngredientComponentHandlers.REGISTRY.getComponents()) {
            subElementGuis.put(recipeComponent, Maps.newHashMap());
        }
    }

    /**
     * Sub gui that holds the list element value type panel and the panel for browsing through the elements.
     */
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
            if (elementSubGui != null) {
                subGuiHolder.removeSubGui(elementSubGui);
                ((ContainerLogicProgrammerBase) gui.getMenu()).setElementInventory(null, 0, 0);
            }
            if (index >= 0) {
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
        public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

            // Output type tooltip
            this.drawTooltipForeground(gui, guiGraphics, container, guiLeft, guiTop, mouseX, mouseY, element.getValueType());
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
    protected static class SelectionSubGui extends RenderPattern<ValueTypeIngredientsLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> implements IInputListener {

        private WidgetArrowedListField<IngredientComponent<?, ?>> valueTypeSelector = null;
        private Button arrowAdd;

        public SelectionSubGui(ValueTypeIngredientsLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                               ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public int getHeight() {
            return super.getHeight() / 4;
        }

        protected static List<IngredientComponent<?, ?>> getValueTypes() {
            // By coincidence, sorting by name (in reverse) is sufficient to achieve the order we want,
            // for the following known ingredient components:
            // - minecraft:itemstack
            // - minecraft:fluidstack
            // - minecraft:energy
            // - mekanism:chemicalstack
            return IngredientComponentHandlers.REGISTRY.getComponents().stream()
                    .sorted(Comparator.<IngredientComponent<?, ?>, Identifier>comparing(IngredientComponent::getName).reversed())
                    .toList();
        }

        @Override
        public void init(int guiLeft, int guiTop) {
            super.init(guiLeft, guiTop);
            valueTypeSelector = new WidgetArrowedListField<>(Minecraft.getInstance().font,
                    getX() + guiLeft + getWidth() / 2 - 50, getY() + guiTop + 2, 100, 15, true, Component.translatable("valuetype.integrateddynamics.value_type"), true, getValueTypes()) {
                @Override
                protected String activeElementToString(IngredientComponent element) {
                    return IModHelpers.get().getL10NHelpers().localize(element.getTranslationKey());
                }
            };
            valueTypeSelector.setListener(this);
            //onChanged();
            int x = guiLeft + getX();
            int y = guiTop + getY();
            buttonList.add(arrowAdd = new ButtonText(x + getWidth() - 13, y + getHeight() - 13, 12, 12, Component.translatable("gui.integrateddynamics.button.add"), Component.literal("+"), (b) -> {
            }, true));
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
            return super.mouseClicked(evt, isDoubleClick) || valueTypeSelector.mouseClicked(evt, isDoubleClick);
        }

        @Override
        protected void actionPerformed(Button guibutton) {
            super.actionPerformed(guibutton);
            if (guibutton == arrowAdd) {
                element.setLength(element.getLength() + 1);
            }
        }

        @Override
        public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            valueTypeSelector.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public void onChanged() {
            element.setCurrentType(valueTypeSelector.getActiveElement());
        }
    }

    /**
     * Panel for browsing through the list elements and updating them.
     */
    protected static class ListElementSubGui extends RenderPattern<ValueTypeIngredientsLPElement, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase> {

        private ButtonArrow arrowLeft;
        private ButtonArrow arrowRight;
        private Button arrowRemove;

        private RenderPattern subGui;
        private IValueTypeLogicProgrammerElement<RenderPattern, ContainerScreenLogicProgrammerBase<?>, AbstractContainerMenu, ?> subElement;

        public ListElementSubGui(ValueTypeIngredientsLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                 ContainerScreenLogicProgrammerBase<?> gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
            this.subGui = element.getClient().subElementGuis.get(element.getCurrentType()).get(element.getActiveElement());
            this.subElement = element.getSubElements().get(element.getCurrentType()).get(element.getActiveElement());
            if(subGui == null) {
                subGui = (RenderPattern) subElement.getClient().createSubGui(baseX, baseY, maxWidth,
                        maxHeight / 3 * 2, gui, container);
                element.getClient().subElementGuis.get(element.getCurrentType()).put(
                        element.getActiveElement(),
                        subGui);
            }
            int x = getX() + baseX - ValueTypeIngredientsLPElement.OFFSET_X;
            int y = getY() + baseY - ValueTypeIngredientsLPElement.OFFSET_Y;
            gui.getMenu().setElementInventory(subElement, x, y);
            subGuiHolder.addSubGui(subGui);

            // Do the same thing server-side
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerSetElementInventory(
                            IngredientComponentHandlers.REGISTRY.getComponentHandler(element.getCurrentType()).getValueType(), x, y));
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
            buttonList.add(arrowLeft = new ButtonArrow(x, y, Component.translatable("gui.cyclopscore.left"),
                    b -> element.setActiveElement(element.getActiveElement() - 1), ButtonArrow.Direction.WEST));
            buttonList.add(arrowRight = new ButtonArrow(x + getWidth() - arrowLeft.getWidth() - 1, y, Component.translatable("gui.cyclopscore.right"),
                    b -> element.setActiveElement(element.getActiveElement() + 1), ButtonArrow.Direction.EAST));
            buttonList.add(arrowRemove = new ButtonText(x + (getWidth() / 2) - (arrowLeft.getWidth() / 2), y + getHeight() - 13, 12, 12, Component.translatable("gui.integrateddynamics.button.remove"), Component.literal("-"),
                    b -> element.removeElement(element.getActiveElement()), true));
            arrowLeft.active = element.getActiveElement() > 0;
            arrowRight.active = element.getActiveElement() < element.getLength() - 1;
            arrowRemove.active = element.getLength() > 0;
            subElement.getClient().setValueInGui(subGui);
            subElement.setValueInContainer(subGui.container);
        }

        @Override
        public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            int x = guiLeft + getX() + (getWidth() / 2);
            int y = guiTop + getY() + 4;
            IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, fontRenderer, String.valueOf(element.getActiveElement()), x - 4, y + 2, 10, IModHelpers.get().getBaseHelpers().RGBAToInt(20, 20, 20, 255), false, Font.DisplayMode.NORMAL);
        }
    }
}
