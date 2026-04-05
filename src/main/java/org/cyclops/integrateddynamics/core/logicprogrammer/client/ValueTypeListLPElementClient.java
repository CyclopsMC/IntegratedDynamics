package org.cyclops.integrateddynamics.core.logicprogrammer.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonArrow;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.IInputListener;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetArrowedListField;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.IRenderPatternValueTypeTooltip;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPatternCommon;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeListLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerSetElementInventory;

import java.util.List;
import java.util.Map;

/**
 * @author rubensworks
 */
public class ValueTypeListLPElementClient extends ValueTypeLPElementBaseClient<ValueTypeListLPElement> {

    private MasterSubGuiRenderPattern masterGui;
    private Map<Integer, RenderPattern> subElementGuis;

    public ValueTypeListLPElementClient(ValueTypeListLPElement element) {
        super(element);
    }

    @Override
    public void setValueInGui(ISubGuiBox subGui) {
        if (getElement().getLength() > 0) {
            masterGui.setSelectedValueType(getElement().getListValueType());
            setActiveElement(0);
        }
    }

    @Override
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return masterGui = new ValueTypeListLPElementClient.MasterSubGuiRenderPattern(this.getElement(), baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    public void setActiveElement(int activeElement) {
        masterGui.setActiveElement(activeElement);
        masterGui.container.onDirty();
    }

    public void reset() {
        this.subElementGuis = Maps.newHashMap();
    }

    public void removeElement(int index) {
        Map<Integer, RenderPattern> oldSubElementGuis = subElementGuis;
        subElementGuis = Maps.newHashMap();
        for(Map.Entry<Integer, RenderPattern> entry : oldSubElementGuis.entrySet()) {
            int i = entry.getKey();
            if(i < index) {
                subElementGuis.put(i, entry.getValue());
            } else if(i > index) {
                subElementGuis.put(i - 1, entry.getValue());
            }
        }
    }

    /**
     * Sub gui that holds the list element value type panel and the panel for browsing through the elements.
     */
    protected static class MasterSubGuiRenderPattern extends RenderPattern<ValueTypeListLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase>
            implements IRenderPatternValueTypeTooltip {

        private final int baseX;
        private final int baseY;
        private final int maxWidth;
        private final int maxHeight;
        private final ContainerScreenLogicProgrammerBase gui;
        private final ContainerLogicProgrammerBase container;
        private final SelectionSubGui selectionGui;

        protected ListElementSubGui elementSubGui = null;
        protected int lastGuiLeft;
        protected int lastGuiTop;
        private boolean renderTooltip = true;

        public MasterSubGuiRenderPattern(ValueTypeListLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                         ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
            subGuiHolder.addSubGui(this.selectionGui = new SelectionSubGui(element, baseX, baseY - getHeight() / 4, maxWidth, maxHeight, gui, container));
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
                subGuiHolder.addSubGui(elementSubGui = new ListElementSubGui(element, baseX, baseY,
                        maxWidth, maxHeight, gui, container));
                elementSubGui.init(lastGuiLeft, lastGuiTop);
            }
        }

        public void setSelectedValueType(IValueType valueType) {
            this.selectionGui.setActiveElement(valueType);
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
     * Selection panel for the list element value type.
     */
    protected static class SelectionSubGui extends RenderPattern<ValueTypeListLPElement, ContainerScreenLogicProgrammerBase, ContainerLogicProgrammerBase> implements IInputListener {

        private WidgetArrowedListField<IValueType<?>> valueTypeSelector = null;
        private Button arrowAdd;

        public SelectionSubGui(ValueTypeListLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                               ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public int getHeight() {
            return super.getHeight() / 4;
        }

        protected static List<IValueType<?>> getValueTypes() {
            List<IValueType<?>> valueTypes = Lists.newArrayList(LogicProgrammerElementTypes.VALUETYPE.getValueTypes());
            valueTypes.remove(ValueTypes.LIST);
            valueTypes.add(ValueTypes.CATEGORY_ANY);
            return valueTypes;
        }

        @Override
        public void init(int guiLeft, int guiTop) {
            super.init(guiLeft, guiTop);
            valueTypeSelector = new WidgetArrowedListField<IValueType<?>>(Minecraft.getInstance().font,
                    getX() + guiLeft + getWidth() / 2 - 50, getY() + guiTop + 9, 100, 15, true,
                    Component.translatable("valuetype.integrateddynamics.value_type"), true, getValueTypes());
            valueTypeSelector.setListener(this);
            if (element.getActiveElement() == -1) {
                onChanged();
            }
            int x = guiLeft + getX();
            int y = guiTop + getY();
            buttonList.add(arrowAdd = new ButtonText(x + getWidth() - 13, y + 10, 12, 12,
                    Component.translatable("gui.integrateddynamics.button.add"), Component.literal("+"), b -> {}, true));
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
            return valueTypeSelector.mouseClicked(evt, isDoubleClick) || super.mouseClicked(evt, isDoubleClick);
        }

        @Override
        protected void actionPerformed(Button guibutton) {
            super.actionPerformed(guibutton);
            if(guibutton == arrowAdd) {
                element.setLength(element.getLength() + 1);
            }
        }

        @Override
        protected boolean isDrawBackground() {
            return false;
        }

        @Override
        public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

            valueTypeSelector.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

            if (element.getActiveElement() >= 0) {
                int x = guiLeft + getX() + 10;
                int y = guiTop + getY() + 4;
                IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, fontRenderer, String.valueOf(element.getActiveElement()), x - 6, y + 12, 10, IModHelpers.get().getBaseHelpers().RGBAToInt(20, 20, 20, 255), false, Font.DisplayMode.NORMAL);
            }
        }

        @Override
        public void onChanged() {
            IValueType newType = valueTypeSelector.getActiveElement();
            element.setListValueType(newType);
            if(arrowAdd != null) {
                arrowAdd.active = newType != ValueTypes.CATEGORY_ANY;
            }
        }

        public void setActiveElement(IValueType valueType) {
            valueTypeSelector.setListener(null);
            valueTypeSelector.setActiveElement(valueType);
            valueTypeSelector.setListener(this);
        }
    }

    /**
     * Panel for browsing through the list elements and updating them.
     */
    protected static class ListElementSubGui extends RenderPattern<ValueTypeListLPElement, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase> {

        private ButtonArrow arrowLeft;
        private ButtonArrow arrowRight;
        private Button arrowRemove;

        private RenderPattern subGui;
        private IValueTypeLogicProgrammerElement<RenderPattern, ContainerScreenLogicProgrammerBase<?>, AbstractContainerMenu, ?> subElement;

        public ListElementSubGui(ValueTypeListLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                 ContainerScreenLogicProgrammerBase<?> gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
            this.subGui = element.getClient().subElementGuis.get(element.getActiveElement());
            this.subElement = element.getSubElements().get(element.getActiveElement());
            if(subGui == null) {
                subGui = (RenderPattern) subElement.getClient().createSubGui(baseX, baseY, maxWidth,
                        maxHeight, gui, container);
                element.getClient().subElementGuis.put(
                        element.getActiveElement(),
                        subGui);
            }
            int x = RenderPatternCommon.calculateX(baseX, maxWidth, subElement.getRenderPattern());
            int y = RenderPatternCommon.calculateY(baseY, maxHeight, subElement.getRenderPattern());
            gui.getMenu().setElementInventory(subElement, x, y);
            subGuiHolder.addSubGui(subGui);
            if (subGui instanceof IRenderPatternValueTypeTooltip) {
                ((IRenderPatternValueTypeTooltip) subGui).setRenderTooltip(false);
            }

            // Do the same thing server-side
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerSetElementInventory(element.getListValueType(), x, y));
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
            buttonList.add(arrowRemove = new ButtonText(x + getWidth() - arrowLeft.getWidth() - 1, y + getHeight() - 13, 10, 12, Component.translatable("gui.integrateddynamics.button.remove"), Component.literal("-"),
                    b -> element.removeElement(element.getActiveElement()), true));
            arrowLeft.active = element.getActiveElement() > 0;
            arrowRight.active = element.getActiveElement() < element.getLength() - 1;
            arrowRemove.active = element.getLength() > 0;
            container.getTemporaryInputSlots().removeDirtyMarkListener(container);
            subElement.getClient().setValueInGui(subGui);
            subElement.setValueInContainer(subGui.container);
            container.getTemporaryInputSlots().addDirtyMarkListener(container);
        }
    }
}
