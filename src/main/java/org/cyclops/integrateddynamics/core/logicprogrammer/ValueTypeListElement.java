package org.cyclops.integrateddynamics.core.logicprogrammer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonArrow;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.GuiArrowedListField;
import org.cyclops.cyclopscore.client.gui.component.input.IInputListener;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerPortable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeSubGuiRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeListValueChangedPacket;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Element for the list value type.
 * @author rubensworks
 */
public class ValueTypeListElement extends ValueTypeElement {

    private IValueType listValueType;
    private Map<Integer, ValueTypeElement> subElements;
    private Map<Integer, ValueTypeSubGuiRenderPattern> subElementGuis;
    private int length = 0;
    private int activeElement = -1;
    private MasterSubGuiRenderPattern masterGui;

    private ValueTypeList.ValueList serverValue = null;

    public ValueTypeListElement() {
        super(ValueTypes.LIST);
    }

    public void setServerValue(ValueTypeList.ValueList serverValue) {
        this.serverValue = serverValue;
    }

    @Override
    public ILogicProgrammerElementType getType() {
        return LogicProgrammerElementTypes.LIST_ELEMENT_TYPE;
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE_CANVAS;
    }

    @Override
    public boolean canWriteElementPre() {
        return MinecraftHelpers.isClientSide() ? listValueType != null : serverValue != null;
    }

    protected List<IValue> constructValues() {
        List<IValue> valueList = Lists.newArrayListWithExpectedSize(this.length);
        for (Map.Entry<Integer, ValueTypeElement> value : this.subElements.entrySet()) {
            if(value.getValue().validate() == null) {
                valueList.add(value.getKey(), value.getValue().getValue());
            } else {
                valueList.add(value.getKey(), listValueType.getDefault());
            }
        }
        return valueList;
    }

    @Override
    public ItemStack writeElement(ItemStack itemStack) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        ValueTypeVariableFacadeFactory factory;
        if(MinecraftHelpers.isClientSide()) {
            factory = new ValueTypeVariableFacadeFactory(listValueType, constructValues());
        } else {
            factory = new ValueTypeVariableFacadeFactory(serverValue);
        }
        return registry.writeVariableFacadeItem(!MinecraftHelpers.isClientSide(), itemStack, ValueTypes.REGISTRY, factory);
    }

    public void setListValueType(IValueType listValueType) {
        this.listValueType = listValueType;
        this.subElements = Maps.newHashMap();
        this.subElementGuis = Maps.newHashMap();
        setLength(0);
    }

    public void setLength(int length) {
        this.length = length;
        setActiveElement(length - 1);
    }

    public void setActiveElement(int index) {
        activeElement = index;
        if(index >= 0 && !subElements.containsKey(index)) {
            subElements.put(index, LogicProgrammerElementTypes.VALUETYPE.getByValueType(listValueType));
        }
        masterGui.setActiveElement(activeElement);
        masterGui.container.onDirty();
    }

    public void removeElement(int index) {
        Map<Integer, ValueTypeElement> oldSubElements = subElements;
        Map<Integer, ValueTypeSubGuiRenderPattern> oldSubElementGuis = subElementGuis;
        subElements = Maps.newHashMap();
        subElementGuis = Maps.newHashMap();
        for(Map.Entry<Integer, ValueTypeElement> entry : oldSubElements.entrySet()) {
            int i = entry.getKey();
            if(i < index) {
                subElements.put(i, entry.getValue());
                subElementGuis.put(i, oldSubElementGuis.get(i));
            } else if(i > index) {
                subElements.put(i - 1, entry.getValue());
                subElementGuis.put(i - 1, oldSubElementGuis.get(i));
            }
        }
        setLength(length - 1);
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public L10NHelpers.UnlocalizedString validate() {
        if(!MinecraftHelpers.isClientSide()) {
            return serverValue == null ? new L10NHelpers.UnlocalizedString() : null;
        }
        if(MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerValueTypeListValueChangedPacket(listValueType == null ? ValueTypes.LIST.getDefault() : ValueTypeList.ValueList.ofList(listValueType, constructValues())));
        }
        if(this.listValueType == null) {
            return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDINPUTITEM);
        }
        for(Map.Entry<Integer, ValueTypeElement> entry : subElements.entrySet()) {
            L10NHelpers.UnlocalizedString error = entry.getValue().validate();
            if(error != null) {
                return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDLISTELEMENT, entry.getKey(), error);
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigRenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammerPortable gui, ContainerLogicProgrammerBase container) {
        return masterGui = new MasterSubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    /**
     * Sub gui that holds the list element value type panel and the panel for browsing through the elements.
     */
    @SideOnly(Side.CLIENT)
    protected static class MasterSubGuiRenderPattern extends SubGuiConfigRenderPattern<ValueTypeListElement, GuiLogicProgrammerPortable, ContainerLogicProgrammerBase> {

        private final int baseX;
        private final int baseY;
        private final int maxWidth;
        private final int maxHeight;
        private final GuiLogicProgrammerPortable gui;
        private final ContainerLogicProgrammerBase container;

        protected ListElementSubGui elementSubGui = null;
        protected int lastGuiLeft;
        protected int lastGuiTop;

        public MasterSubGuiRenderPattern(ValueTypeListElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                         GuiLogicProgrammerPortable gui, ContainerLogicProgrammerBase container) {
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
                subGuiHolder.addSubGui(elementSubGui = new ListElementSubGui(element, baseX, baseY + (getHeight() / 4), maxWidth, maxHeight, gui, container));
                elementSubGui.initGui(lastGuiLeft, lastGuiTop);
            }
        }

        @Override
        public void initGui(int guiLeft, int guiTop) {
            super.initGui(guiLeft, guiTop);
            lastGuiLeft = guiLeft;
            lastGuiTop = guiTop;
        }

        @Override
        public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
            IValueType valueType = element.getInnerGuiElement().getValueType();

            // Output type tooltip
            if(!container.hasWriteItemInSlot()) {
                if(gui.isPointInRegion(ContainerLogicProgrammerBase.OUTPUT_X, ContainerLogicProgrammerBase.OUTPUT_Y,
                        GuiLogicProgrammerPortable.BOX_HEIGHT, GuiLogicProgrammerPortable.BOX_HEIGHT, mouseX, mouseY)) {
                    gui.drawTooltip(getValueTypeTooltip(valueType), mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }
    }

    /**
     * Selection panel for the list element value type.
     */
    @SideOnly(Side.CLIENT)
    protected static class SelectionSubGui extends SubGuiConfigRenderPattern<ValueTypeListElement, GuiLogicProgrammerPortable, ContainerLogicProgrammerBase> implements IInputListener {

        private GuiArrowedListField<IValueType> valueTypeSelector = null;
        private GuiButton arrowAdd;

        public SelectionSubGui(ValueTypeListElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                               GuiLogicProgrammerPortable gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public int getHeight() {
            return super.getHeight() / 4;
        }

        protected static List<IValueType> getValueTypes() {
            return LogicProgrammerElementTypes.VALUETYPE.getValueTypes();
        }

        @Override
        public void initGui(int guiLeft, int guiTop) {
            super.initGui(guiLeft, guiTop);
            valueTypeSelector = new GuiArrowedListField<>(0, Minecraft.getMinecraft().fontRendererObj, getX() + guiLeft + getWidth() / 2 - 50, getY() + guiTop + 2, 100, 15, true, true, getValueTypes());
            valueTypeSelector.setListener(this);
            onChanged();
            int x = guiLeft + getX();
            int y = guiTop + getY();
            buttonList.add(arrowAdd = new GuiButtonText(1, x + getWidth() - 13, y + getHeight() - 13, 12, 12, "+", true));
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            valueTypeSelector.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        protected void actionPerformed(GuiButton guibutton) {
            super.actionPerformed(guibutton);
            if(guibutton == arrowAdd) {
                element.setLength(element.length + 1);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            valueTypeSelector.drawTextBox(Minecraft.getMinecraft(), mouseX - guiLeft, mouseY - guiTop);
        }

        @Override
        public void onChanged() {
            element.setListValueType(valueTypeSelector.getActiveElement());
        }
    }

    /**
     * Panel for browsing through the list elements and updating them.
     */
    @SideOnly(Side.CLIENT)
    protected static class ListElementSubGui extends SubGuiConfigRenderPattern<ValueTypeListElement, GuiLogicProgrammerPortable, ContainerLogicProgrammerBase> {

        private GuiButtonArrow arrowLeft;
        private GuiButtonArrow arrowRight;
        private GuiButton arrowRemove;

        public ListElementSubGui(ValueTypeListElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                               GuiLogicProgrammerPortable gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
            ValueTypeSubGuiRenderPattern subGui = element.subElementGuis.get(element.activeElement);
            if(subGui == null) {
                subGui = (ValueTypeSubGuiRenderPattern) element.subElements.get(element.activeElement).createSubGui(baseX, baseY, maxWidth, maxHeight / 3 * 2, gui, container);
                element.subElementGuis.put(
                        element.activeElement,
                        subGui);
            }
            subGuiHolder.addSubGui(subGui);
        }

        @Override
        public int getHeight() {
            return (super.getHeight() / 4) * 3;
        }

        @Override
        public void initGui(int guiLeft, int guiTop) {
            super.initGui(guiLeft, guiTop);
            int x = guiLeft + getX();
            int y = guiTop + getY();
            buttonList.add(arrowLeft = new GuiButtonArrow(1, x, y, GuiButtonArrow.Direction.WEST));
            buttonList.add(arrowRight = new GuiButtonArrow(1, x + getWidth() - arrowLeft.width - 1, y, GuiButtonArrow.Direction.EAST));
            buttonList.add(arrowRemove = new GuiButtonText(2, x + (getWidth() / 2) - (arrowLeft.width / 2), y + getHeight() - 13, 12, 12, "-", true));
            arrowLeft.enabled = element.activeElement > 0;
            arrowRight.enabled = element.activeElement < element.length - 1;
            arrowRemove.enabled = element.length > 0;
        }

        @Override
        protected void actionPerformed(GuiButton guibutton) {
            super.actionPerformed(guibutton);
            if(guibutton == arrowLeft) {
                element.setActiveElement(element.activeElement - 1);
            } else if(guibutton == arrowRight) {
                element.setActiveElement(element.activeElement + 1);
            } else if(guibutton == arrowRemove) {
                element.removeElement(element.activeElement);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            int x = guiLeft + getX() + (getWidth() / 2);
            int y = guiTop + getY() + 4;
            RenderHelpers.drawScaledCenteredString(fontRenderer, String.valueOf(element.activeElement), x - 4, y + 2, 10, Helpers.RGBToInt(20, 20, 20));
        }
    }

    protected static class ValueTypeVariableFacadeFactory implements IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade> {

        private final ValueTypeList.ValueList values;

        public ValueTypeVariableFacadeFactory(IValueType valueType, List<IValue> values) {
            this(ValueTypeList.ValueList.ofList(valueType, values));
        }

        public ValueTypeVariableFacadeFactory(ValueTypeList.ValueList values) {
            this.values = values;
        }

        @Override
        public IValueTypeVariableFacade create(boolean generateId) {
            return new ValueTypeVariableFacade(generateId, ValueTypes.LIST, values);
        }

        @Override
        public IValueTypeVariableFacade create(int id) {
            return new ValueTypeVariableFacade(id, ValueTypes.LIST, values);
        }
    }

}
