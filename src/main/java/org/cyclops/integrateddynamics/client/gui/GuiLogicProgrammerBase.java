package org.cyclops.integrateddynamics.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonText;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammerConfig;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeString;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.item.ItemLabeller;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerActivateElementPacket;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerLabelPacket;
import org.cyclops.integrateddynamics.proxy.ClientProxy;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Base gui for the logic programmer.
 * @author rubensworks
 */
public class GuiLogicProgrammerBase extends ScrollingGuiContainer {

    public static final int BOX_HEIGHT = 18;
    private static final Rectangle ITEM_POSITION = new Rectangle(19, 18, 56, BOX_HEIGHT - 1);

    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();
    private final boolean hasLabeller;
    protected RenderPattern operatorConfigPattern = null;
    protected SubGuiOperatorInfo operatorInfoPattern = null;
    protected boolean firstInit = true;
    protected int relativeStep = -1;

    public GuiLogicProgrammerBase(InventoryPlayer inventoryPlayer, ContainerLogicProgrammerBase container) {
        super(container);
        container.setGui(this);

        this.hasLabeller = inventoryPlayer.hasItemStack(new ItemStack(ItemLabeller.getInstance()));
    }

    @Override
    public ContainerLogicProgrammerBase getContainer() {
        return (ContainerLogicProgrammerBase) super.getContainer();
    }

    @Override
    public void initGui() {
        super.initGui();
        subGuiHolder.initGui(this.guiLeft, this.guiTop);
        if (firstInit) {
            setSearchFieldFocussed(true);
            firstInit = false;
        }
    }

    protected int getScrollX() {
        return 5;
    }

    protected int getScrollY() {
        return 18;
    }

    protected int getScrollHeight() {
        return 178;
    }

    @Override
    protected int getBaseXSize() {
        return 256;
    }

    @Override
    protected int getBaseYSize() {
        return 240;
    }

    @Override
    protected int getSearchX() {
        return 6;
    }

    protected int getSearchWidth() {
        return 70;
    }

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getModGui().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + BlockLogicProgrammerConfig._instance.getNamedId() + ".png";
    }

    protected float colorSmoothener(float color, boolean hover) {
        return 1F - ((1F - color) / (hover ? 2F : 4F));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        subGuiHolder.drawGuiContainerBackgroundLayer(this.guiLeft, this.guiTop, mc.renderEngine, fontRenderer, partialTicks, mouseX, mouseY);

        // Draw container name
        fontRenderer.drawString(L10NHelpers.localize(L10NValues.GUI_LOGICPROGRAMMER_FILTER),
                this.guiLeft + offsetX + 5, this.guiTop + offsetY + 208, Helpers.RGBToInt(80, 80, 80));

        // Draw operators
        ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) getScrollingInventoryContainer();
        int boxHeight = BOX_HEIGHT;
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);

                GlStateManager.disableAlpha();
                Triple<Float, Float, Float> rgb = Helpers.intToRGB(element.getColor());
                boolean hover = LogicProgrammerElementTypes.areEqual(container.getActiveElement(), element)
                        || isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY));
                GlStateManager.color(colorSmoothener(rgb.getLeft(), hover), colorSmoothener(rgb.getMiddle(), hover),
                        colorSmoothener(rgb.getRight(), hover), 1);

                // Background
                mc.renderEngine.bindTexture(texture);
                drawTexturedModalRect(guiLeft + offsetX + ITEM_POSITION.x,
                        guiTop + offsetY + ITEM_POSITION.y + boxHeight * i, 19, 18, ITEM_POSITION.width, ITEM_POSITION.height);

                GlStateManager.enableAlpha();
                // Arrow
                if(hover) {
                    drawTexturedModalRect(guiLeft + offsetX + ITEM_POSITION.x,
                            guiTop + offsetY + ITEM_POSITION.y + boxHeight * i, 0, 240, 3, 16);
                }
                GlStateManager.disableAlpha();
                GlStateManager.color(1, 1, 1);

                // Operator info
                String aspectName = L10NHelpers.localize(element.getSymbol());
                RenderHelpers.drawScaledCenteredString(fontRenderer, aspectName,
                        this.guiLeft + offsetX + (hover ? 22 : 21),
                        this.guiTop + offsetY + 26 + boxHeight * i,
                        53, Helpers.RGBToInt(40, 40, 40));
            }
        }
    }

    protected Rectangle getElementPosition(ContainerLogicProgrammerBase container, int i, boolean absolute) {
        return new Rectangle(ITEM_POSITION.x + offsetX + (absolute ? this.guiLeft : 0),
                ITEM_POSITION.y + BOX_HEIGHT * i + offsetY + (absolute ? this.guiTop : 0),
                ITEM_POSITION.width, ITEM_POSITION.height
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(this.guiLeft, this.guiTop, mc.renderEngine, fontRenderer, mouseX, mouseY);
        // Draw operator tooltips
        ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) getScrollingInventoryContainer();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);
                if(isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    List<String> lines = Lists.newLinkedList();
                    element.loadTooltip(lines);
                    drawTooltip(lines, mouseX - this.guiLeft, mouseY - this.guiTop);
                }
            }
        }
    }

    protected void onActivateElement(ILogicProgrammerElement<RenderPattern, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> element) {
        subGuiHolder.addSubGui(operatorConfigPattern = element.createSubGui(88, 18, 160, 87, this, (ContainerLogicProgrammerBase) getContainer()));
        operatorConfigPattern.initGui(guiLeft, guiTop);
        subGuiHolder.addSubGui(operatorInfoPattern = new SubGuiOperatorInfo(element));
        operatorInfoPattern.initGui(guiLeft, guiTop);
    }

    protected void onDeactivateElement(ILogicProgrammerElement element) {
        subGuiHolder.clear();
    }

    public boolean handleElementActivation(ILogicProgrammerElement element) {
        boolean activate = false;
        ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) getScrollingInventoryContainer();
        ILogicProgrammerElement newActive = null;
        onDeactivateElement(element);
        if(container.getActiveElement() != element) {
            activate = true;
            newActive = element;
            if(element != null) {
                onActivateElement(element);
            }
        }
        container.setActiveElement(newActive,
                operatorConfigPattern == null ? 0 : operatorConfigPattern.getX(),
                operatorConfigPattern == null ? 0 : operatorConfigPattern.getY());
        if(newActive != null) {
            ILogicProgrammerElementType type = newActive.getType();
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerActivateElementPacket(type.getName(), type.getName(newActive)));
        } else {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerActivateElementPacket("", ""));
        }
        return activate;
    }

    protected void setSearchFieldFocussed(boolean focused) {
        getSearchField().setFocused(focused);
    }

    protected boolean isSearchFieldFocussed() {
        return getSearchField().isFocused();
    }

    protected boolean selectPageElement(int elementId) {
        ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) getScrollingInventoryContainer();

        // Deactivate current element
        if (elementId < 0) {
            handleElementActivation(container.getActiveElement());
            return false;
        }

        // Activate a new element
        for(int i = 0; i < container.getPageSize(); i++) {
            if (container.isElementVisible(i)) {
                if (elementId-- == 0) {
                    ILogicProgrammerElement element = container.getVisibleElement(i);
                    if (container.getActiveElement() != element) {
                        handleElementActivation(element);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(keyCode != Keyboard.KEY_LSHIFT && keyCode != Keyboard.KEY_RSHIFT) {
            ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) getScrollingInventoryContainer();
            int pageSize = container.getPageSize();
            int stepModifier = isShiftKeyDown() ? pageSize - 1 : 1;
            boolean isElementFocused = container.getActiveElement() != null && container.getActiveElement().isFocused(operatorConfigPattern);

            if (ClientProxy.FOCUS_LP_SEARCH.isActiveAndMatches(keyCode)) {
                // Focus search field
                setSearchFieldFocussed(true);
            } else if (isElementFocused && ClientProxy.FOCUS_LP_RENAME.isActiveAndMatches(keyCode) && hasLabeller()) {
                // Open labeller gui
                operatorInfoPattern.onButtonEditClick();
            } else if (Keyboard.KEY_LEFT == keyCode && (isElementFocused || isSearchFieldFocussed())) {
                if (isElementFocused) {
                    container.getActiveElement().setFocused(operatorConfigPattern, false);
                    setSearchFieldFocussed(true);
                } else {
                    // Unfocus search field
                    setSearchFieldFocussed(false);
                }
            } else if (!isElementFocused && Keyboard.KEY_DOWN == keyCode) {
                // Scroll down
                if (!selectPageElement(relativeStep += stepModifier)) {
                    relativeStep -= stepModifier;
                    if (relativeStep > 0) {
                        scrollRelative(-stepModifier);
                        selectPageElement(relativeStep);
                    }
                }
            } else if (!isElementFocused && Keyboard.KEY_UP == keyCode) {
                // Scroll up
                if (!(relativeStep >= 0 && selectPageElement(relativeStep -= stepModifier))) {
                    scrollRelative(stepModifier);
                    selectPageElement(relativeStep = 0);
                }
            } else if (!isElementFocused
                    && (Keyboard.KEY_RIGHT == keyCode || Keyboard.KEY_TAB == keyCode
                    || Keyboard.KEY_RETURN == keyCode || Keyboard.KEY_NUMPADENTER == keyCode)) {
                if (container.getActiveElement() != null) {
                    container.getActiveElement().setFocused(operatorConfigPattern, true);
                }
            } else if (!subGuiHolder.keyTyped(this.checkHotbarKeys(keyCode), typedChar, keyCode)
                    && (keyCode == Keyboard.KEY_ESCAPE || !isElementFocused)) {
                // All others
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        subGuiHolder.mouseClicked(mouseX, mouseY, mouseButton);
        ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) getScrollingInventoryContainer();
        for(int i = 0; i < container.getPageSize(); i++) {
            if (container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);
                if (isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    boolean activated = handleElementActivation(element);
                    relativeStep = activated ? i : -1;
                    if (activated) {
                        container.getActiveElement().setFocused(operatorConfigPattern, true);
                    }
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // If the search box has been selected, de-active the current element.
        if(isSearchFieldFocussed() &&
                container.getActiveElement() != null && container.getActiveElement().isFocused(operatorConfigPattern)) {
            container.getActiveElement().setFocused(operatorConfigPattern, false);
        }
    }

    protected void label(String label) {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerLabelPacket(label));
    }

    protected boolean hasLabeller() {
        return this.hasLabeller;
    }

    public class SubGuiOperatorInfo extends GuiElementValueTypeString.SubGuiValueTypeInfo<RenderPattern, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> {

        public static final int BUTTON_EDIT = 1;

        private GuiTextField searchField;
        private GuiButtonText button = null;

        public SubGuiOperatorInfo(IGuiInputElement<RenderPattern, GuiLogicProgrammerBase, ContainerLogicProgrammerBase> element) {
            super(GuiLogicProgrammerBase.this, (ContainerLogicProgrammerBase) GuiLogicProgrammerBase.this.container, element, 88, 106, 139, 20);

            if(hasLabeller()) {
                buttonList.add(button = new GuiButtonText(BUTTON_EDIT, 0, 0, 6, 10, "E", true));
            }

            int searchWidth = 113;
            this.searchField = new GuiTextField(0, GuiLogicProgrammerBase.this.fontRenderer, 0, 0, searchWidth, 11);
            this.searchField.setMaxStringLength(64);
            this.searchField.setEnableBackgroundDrawing(true);
            this.searchField.setVisible(false);
            this.searchField.setTextColor(16777215);
            this.searchField.setCanLoseFocus(true);
            this.searchField.setText("");
            this.searchField.width = searchWidth;
        }

        @Override
        public void initGui(int guiLeft, int guiTop) {
            super.initGui(guiLeft, guiTop);
            int searchX = 90;
            int searchY = 110;
            this.searchField.x = guiLeft + searchX;
            this.searchField.y = guiTop + searchY;

            if (hasLabeller()) {
                button.x = guiLeft + 220;
                button.y = guiTop + 111;
            }
        }

        @Override
        protected boolean showError() {
            return container.canWriteActiveElementPre();
        }

        @Override
        protected L10NHelpers.UnlocalizedString getLastError() {
            return container.getLastError();
        }

        @Override
        protected ResourceLocation getTexture() {
            return texture;
        }

        @Override
        public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException {
            if (!checkHotbarKeys) {
                if (!this.searchField.getVisible() || !this.searchField.textboxKeyTyped(typedChar, keyCode)) {
                    return super.keyTyped(checkHotbarKeys, typedChar, keyCode);
                } else {
                    label(this.searchField.getText());
                    return true;
                }
            }
            return super.keyTyped(checkHotbarKeys, typedChar, keyCode);
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            if(this.searchField.getVisible()) {
                this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            Keyboard.enableRepeatEvents(true);
            this.searchField.drawTextBox();
        }

        @Override
        protected void actionPerformed(GuiButton guibutton) {
            super.actionPerformed(guibutton);
            if(guibutton.id == BUTTON_EDIT) {
                onButtonEditClick();
            }
        }

        public void onButtonEditClick() {
            this.searchField.setVisible(!this.searchField.getVisible());
            if(this.searchField.getVisible()) {
                this.searchField.setFocused(true);
                label(this.searchField.getText());
            } else {
                label("");
            }
        }
    }

}
