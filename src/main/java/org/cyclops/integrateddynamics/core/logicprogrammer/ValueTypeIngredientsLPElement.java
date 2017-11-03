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
import org.cyclops.commoncapabilities.api.capability.recipehandler.RecipeComponent;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonArrow;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.GuiArrowedListField;
import org.cyclops.cyclopscore.client.gui.component.input.IInputListener;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IValueTypeLogicProgrammerElement;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeIngredients;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.IngredientsRecipeLists;
import org.cyclops.integrateddynamics.core.evaluate.variable.recipe.RecipeComponentHandlers;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeIngredientsValueChangedPacket;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Element for the ingredients value type.
 * @author rubensworks
 */
public class ValueTypeIngredientsLPElement extends ValueTypeLPElementBase {

    private RecipeComponent currentType = RecipeComponent.ITEMSTACK;
    private Map<RecipeComponent, Integer> lengths = Maps.newHashMap();
    private Map<RecipeComponent, Map<Integer, IValueTypeLogicProgrammerElement>> subElements = Maps.newHashMap();
    private Map<RecipeComponent, Map<Integer, RenderPattern>> subElementGuis = Maps.newHashMap();
    private int activeElement = -1;
    @SideOnly(Side.CLIENT)
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

    protected IngredientsRecipeLists constructValues() {
        Map<RecipeComponent<?, ?>, List<List<? extends IValue>>> lists = Maps.newIdentityHashMap();
        for (RecipeComponent<?, ?> component : RecipeComponentHandlers.REGISTRY.getComponents()) {
            List<List<? extends IValue>> values = Lists.newArrayListWithExpectedSize(lengths.get(component));
            subElements.get(component).entrySet().forEach(entry -> {
                try {
                    values.add(Lists.newArrayList(entry.getValue().getValue()));
                } catch (Exception e) {
                    values.add(Lists.newArrayList(RecipeComponentHandlers.REGISTRY.getComponentHandler(component)
                            .getValueType().getDefault()));
                }
            });
            lists.put(component, values);
        }
        return new IngredientsRecipeLists(lists);
    }

    @Override
    public IValue getValue() {
        return MinecraftHelpers.isClientSide()
                ? ValueObjectTypeIngredients.ValueIngredients.of(constructValues()) : serverValue;
    }

    public int getLength() {
        return lengths.get(currentType);
    }

    public void setLength(int length) {
        lengths.put(currentType, length);
        setActiveElement(getLength() - 1);
    }

    public void setCurrentType(RecipeComponent currentType) {
        this.currentType = currentType;
        setActiveElement(subElements.get(currentType).size() - 1);
    }

    public void setActiveElement(int index) {
        activeElement = index;
        if(index >= 0 && !subElements.get(currentType).containsKey(index)) {
            subElements.get(currentType).put(index, RecipeComponentHandlers.REGISTRY.getComponentHandler(currentType)
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
        for (RecipeComponent recipeComponent : RecipeComponentHandlers.REGISTRY.getComponents()) {
            subElements.put(recipeComponent, Maps.newHashMap());
            subElementGuis.put(recipeComponent, Maps.newHashMap());
            lengths.put(recipeComponent, 0);
        }
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
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerValueTypeIngredientsValueChangedPacket(
                            ValueObjectTypeIngredients.ValueIngredients.of(constructValues())));
        }
        for (Map<Integer, IValueTypeLogicProgrammerElement> componentValues : subElements.values()) {
            for(Map.Entry<Integer, IValueTypeLogicProgrammerElement> entry : componentValues.entrySet()) {
                L10NHelpers.UnlocalizedString error = entry.getValue().validate();
                if(error != null) {
                    return new L10NHelpers.UnlocalizedString(L10NValues.VALUETYPE_ERROR_INVALIDLISTELEMENT, entry.getKey(), error);
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
    @SideOnly(Side.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return masterGui = new MasterSubGuiRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

    /**
     * Sub gui that holds the list element value type panel and the panel for browsing through the elements.
     */
    @SideOnly(Side.CLIENT)
    protected static class MasterSubGuiRenderPattern extends RenderPattern<ValueTypeIngredientsLPElement, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> {

        private final int baseX;
        private final int baseY;
        private final int maxWidth;
        private final int maxHeight;
        private final GuiLogicProgrammerBase gui;
        private final ContainerLogicProgrammerBase container;

        protected ListElementSubGui elementSubGui = null;
        protected int lastGuiLeft;
        protected int lastGuiTop;

        public MasterSubGuiRenderPattern(ValueTypeIngredientsLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                         GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
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
            IValueType valueType = element.getValueType();

            // Output type tooltip
            if(!container.hasWriteItemInSlot()) {
                if(gui.isPointInRegion(ContainerLogicProgrammerBase.OUTPUT_X, ContainerLogicProgrammerBase.OUTPUT_Y,
                        GuiLogicProgrammerBase.BOX_HEIGHT, GuiLogicProgrammerBase.BOX_HEIGHT, mouseX, mouseY)) {
                    gui.drawTooltip(getValueTypeTooltip(valueType), mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }
    }

    /**
     * Selection panel for the type.
     */
    @SideOnly(Side.CLIENT)
    protected static class SelectionSubGui extends RenderPattern<ValueTypeIngredientsLPElement, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> implements IInputListener {

        private GuiArrowedListField<RecipeComponent> valueTypeSelector = null;
        private GuiButton arrowAdd;

        public SelectionSubGui(ValueTypeIngredientsLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                               GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
            super(element, baseX, baseY, maxWidth, maxHeight, gui, container);
        }

        @Override
        public int getHeight() {
            return super.getHeight() / 4;
        }

        protected static List<RecipeComponent> getValueTypes() {
            return Lists.newArrayList(RecipeComponentHandlers.REGISTRY.getComponents());
        }

        @Override
        public void initGui(int guiLeft, int guiTop) {
            super.initGui(guiLeft, guiTop);
            valueTypeSelector = new GuiArrowedListField<RecipeComponent>(0, Minecraft.getMinecraft().fontRenderer,
                    getX() + guiLeft + getWidth() / 2 - 50, getY() + guiTop + 2, 100, 15, true, true, getValueTypes()) {
                @Override
                protected String activeElementToString(RecipeComponent element) {
                    return L10NHelpers.localize(element.getUnlocalizedName());
                }
            };
            valueTypeSelector.setListener(this);
            //onChanged();
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
                element.setLength(element.getLength() + 1);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            valueTypeSelector.drawTextBox(Minecraft.getMinecraft(), mouseX, mouseY);
        }

        @Override
        public void onChanged() {
            element.setCurrentType(valueTypeSelector.getActiveElement());
        }
    }

    /**
     * Panel for browsing through the list elements and updating them.
     */
    @SideOnly(Side.CLIENT)
    protected static class ListElementSubGui extends RenderPattern<ValueTypeIngredientsLPElement, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> {

        private GuiButtonArrow arrowLeft;
        private GuiButtonArrow arrowRight;
        private GuiButton arrowRemove;

        public ListElementSubGui(ValueTypeIngredientsLPElement element, int baseX, int baseY, int maxWidth, int maxHeight,
                                 GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
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
            gui.getContainer().setElementInventory(subElement, getX() + baseX - 24, getY() + baseY - 23);
            subElement.setValueInGui(subGui);
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
            arrowRight.enabled = element.activeElement < element.getLength() - 1;
            arrowRemove.enabled = element.getLength() > 0;
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

}
