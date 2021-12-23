package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeString;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.RenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerActivateElementPacket;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerLabelPacket;
import org.cyclops.integrateddynamics.proxy.ClientProxy;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;

/**
 * Base gui for the logic programmer.
 * @author rubensworks
 */
public class ContainerScreenLogicProgrammerBase<C extends ContainerLogicProgrammerBase> extends ContainerScreenScrolling<C> {

    public static final int BOX_HEIGHT = 18;
    private static final Rectangle ITEM_POSITION = new Rectangle(19, 18, 56, BOX_HEIGHT - 1);

    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();
    private final boolean hasLabeller;
    protected RenderPattern operatorConfigPattern = null;
    protected SubGuiOperatorInfo operatorInfoPattern = null;
    protected boolean firstInit = true;
    protected int relativeStep = -1;

    public ContainerScreenLogicProgrammerBase(C container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        container.setGui(this);

        this.hasLabeller = playerInventory.contains(new ItemStack(RegistryEntries.ITEM_LABELLER));
    }

    @Override
    protected Rectangle getScrollRegion() {
        return new Rectangle(this.leftPos + 19, this.topPos + 18, 57, 178);
    }

    @Override
    public void init() {
        super.init();
        subGuiHolder.init(this.leftPos, this.topPos);
        if (firstInit) {
            setSearchFieldFocussed(true);
            firstInit = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        subGuiHolder.tick();
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
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/logic_programmer.png");
    }

    protected float colorSmoothener(float color, boolean hover) {
        return 1F - ((1F - color) / (hover ? 2F : 4F));
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        // super.renderBg(matrixStack, partialTicks, mouseX, mouseY); // TODO: rm
        subGuiHolder.drawGuiContainerBackgroundLayer(matrixStack, this.leftPos, this.topPos, getMinecraft().textureManager, font, partialTicks, mouseX, mouseY);

        // Draw container name
        // MCP: drawString
        font.drawShadow(matrixStack, new TranslationTextComponent(L10NValues.GUI_LOGICPROGRAMMER_FILTER),
                this.leftPos + offsetX + 5, this.topPos + offsetY + 208, Helpers.RGBToInt(80, 80, 80));

        // Draw operators
        ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) getMenu();
        int boxHeight = BOX_HEIGHT;
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);

                RenderSystem.disableAlphaTest();
                Triple<Float, Float, Float> rgb = Helpers.intToRGB(element.getColor());
                boolean hover = LogicProgrammerElementTypes.areEqual(container.getActiveElement(), element)
                        || isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY));
                RenderSystem.color4f(colorSmoothener(rgb.getLeft(), hover), colorSmoothener(rgb.getMiddle(), hover),
                        colorSmoothener(rgb.getRight(), hover), 1);

                // Background
                RenderHelpers.bindTexture(texture);
                blit(matrixStack, leftPos + offsetX + ITEM_POSITION.x,
                        topPos + offsetY + ITEM_POSITION.y + boxHeight * i, 19, 18, ITEM_POSITION.width, ITEM_POSITION.height);

                RenderSystem.enableAlphaTest();
                // Arrow
                if(hover) {
                    blit(matrixStack, leftPos + offsetX + ITEM_POSITION.x,
                            topPos + offsetY + ITEM_POSITION.y + boxHeight * i, 0, 240, 3, 16);
                }
                RenderSystem.disableAlphaTest();
                RenderSystem.color3f(1, 1, 1);

                // Operator info
                String aspectName = element.getSymbol();
                RenderHelpers.drawScaledCenteredString(matrixStack, font, aspectName,
                        this.leftPos + offsetX + (hover ? 22 : 21),
                        this.topPos + offsetY + 26 + boxHeight * i,
                        53, Helpers.RGBToInt(40, 40, 40));
            }
        }
    }

    protected Rectangle getElementPosition(ContainerLogicProgrammerBase container, int i, boolean absolute) {
        return new Rectangle(ITEM_POSITION.x + offsetX + (absolute ? this.leftPos : 0),
                ITEM_POSITION.y + BOX_HEIGHT * i + offsetY + (absolute ? this.topPos : 0),
                ITEM_POSITION.width, ITEM_POSITION.height
        );
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(matrixStack, this.leftPos, this.topPos, getMinecraft().textureManager, font, mouseX, mouseY);
        // Draw operator tooltips
        ContainerLogicProgrammerBase container = getMenu();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);
                if(isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    List<ITextComponent> lines = Lists.newLinkedList();
                    element.loadTooltip(lines);
                    drawTooltip(lines, mouseX - this.leftPos, mouseY - this.topPos);
                }
            }
        }
    }

    protected void onActivateElement(ILogicProgrammerElement<RenderPattern, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase> element) {
        subGuiHolder.addSubGui(operatorInfoPattern = new SubGuiOperatorInfo(element));
        operatorInfoPattern.init(leftPos, topPos);
        subGuiHolder.addSubGui(operatorConfigPattern = element.createSubGui(88, 18, 160, 87, this, (ContainerLogicProgrammerBase) getMenu()));
        operatorConfigPattern.init(leftPos, topPos);
    }

    protected void onDeactivateElement(ILogicProgrammerElement element) {
        subGuiHolder.clear();
    }

    public boolean handleElementActivation(ILogicProgrammerElement element) {
        boolean activate = false;
        ContainerLogicProgrammerBase container = getMenu();
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
                    new LogicProgrammerActivateElementPacket(type.getUniqueName(), type.getName(newActive)));
        } else {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(
                    new LogicProgrammerActivateElementPacket(new ResourceLocation(""), new ResourceLocation("")));
        }
        return activate;
    }

    protected void setSearchFieldFocussed(boolean focused) {
        getSearchField().changeFocus(focused);
    }

    protected boolean isSearchFieldFocussed() {
        return getSearchField().isFocused();
    }

    protected boolean selectPageElement(int elementId) {
        ContainerLogicProgrammerBase container = getMenu();

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

    protected boolean handleKeyCode(int keyCode, int scanCode) {
        InputMappings.Input inputCode = InputMappings.getKey(keyCode, scanCode);
        if(keyCode != GLFW.GLFW_KEY_LEFT_SHIFT && keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT) {
            ContainerLogicProgrammerBase container = getMenu();
            int pageSize = container.getPageSize();
            int stepModifier = MinecraftHelpers.isShifted() ? pageSize - 1 : 1;
            boolean isElementFocused = container.getActiveElement() != null && container.getActiveElement().isFocused(operatorConfigPattern);

            if (ClientProxy.FOCUS_LP_SEARCH.isActiveAndMatches(inputCode)) {
                // Focus search field
                setSearchFieldFocussed(true);
                return true;
            } else if (isElementFocused && ClientProxy.FOCUS_LP_RENAME.isActiveAndMatches(inputCode) && hasLabeller()) {
                // Open labeller gui
                operatorInfoPattern.onButtonEditClick();
                return true;
            } else if (GLFW.GLFW_KEY_LEFT == keyCode && (!isElementFocused && isSearchFieldFocussed())) {
                // Unfocus search field
                setSearchFieldFocussed(isSearchFieldFocussed());
                return true;
            } else if (!isElementFocused && GLFW.GLFW_KEY_DOWN == keyCode) {
                // Scroll down
                if (!selectPageElement(relativeStep += stepModifier)) {
                    relativeStep -= stepModifier;
                    if (relativeStep > 0) {
                        getScrollbar().scrollRelative(-stepModifier);
                        selectPageElement(relativeStep);
                    }
                }
                return true;
            } else if (!isElementFocused && GLFW.GLFW_KEY_UP == keyCode) {
                // Scroll up
                if (!(relativeStep >= 0 && selectPageElement(relativeStep -= stepModifier))) {
                    getScrollbar().scrollRelative(stepModifier);
                    selectPageElement(relativeStep = 0);
                }
                return true;
            } else if (!isElementFocused
                    && (GLFW.GLFW_KEY_RIGHT == keyCode || GLFW.GLFW_KEY_TAB == keyCode
                    || GLFW.GLFW_KEY_ENTER == keyCode || GLFW.GLFW_KEY_KP_ENTER == keyCode)) {
                if (container.getActiveElement() != null) {
                    container.getActiveElement().setFocused(operatorConfigPattern, true);
                    setSearchFieldFocussed(false);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char keyCode, int scanCode) {
        return subGuiHolder.charTyped(keyCode, scanCode) || handleKeyCode(keyCode, scanCode) || super.charTyped(keyCode, scanCode);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if (this.subGuiHolder.keyPressed(keyCode, scanCode, modifiers) || handleKeyCode(keyCode, scanCode)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        subGuiHolder.mouseClicked(mouseX, mouseY, mouseButton);
        ContainerLogicProgrammerBase container = getMenu();
        for(int i = 0; i < container.getPageSize(); i++) {
            if (container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);
                if (isPointInRegion(getElementPosition(container, i, false), new Point((int) mouseX, (int) mouseY))) {
                    boolean activated = handleElementActivation(element);
                    relativeStep = activated ? i : -1;
                    if (activated) {
                        container.getActiveElement().setFocused(operatorConfigPattern, true);
                        setSearchFieldFocussed(false);
                        return true;
                    }
                }
            }
        }
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        // If the search box has been selected, de-active the current element.
        if(isSearchFieldFocussed() &&
                container.getActiveElement() != null && container.getActiveElement().isFocused(operatorConfigPattern)) {
            container.getActiveElement().setFocused(operatorConfigPattern, false);
            return true;
        }

        return false;
    }

    protected void label(String label) {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerLabelPacket(label));
    }

    protected boolean hasLabeller() {
        return this.hasLabeller;
    }

    public class SubGuiOperatorInfo extends GuiElementValueTypeString.SubGuiValueTypeInfo<RenderPattern, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase> {

        private WidgetTextFieldExtended searchField;
        private ButtonText button = null;

        public SubGuiOperatorInfo(IGuiInputElement<RenderPattern, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase> element) {
            super(ContainerScreenLogicProgrammerBase.this, getMenu(), element, 88, 106, 139, 20);

            if(hasLabeller()) {
                buttonList.add(button = new ButtonText(0, 0, 6, 10, new TranslationTextComponent("gui.integrateddynamics.button.edit"), new StringTextComponent("E"),
                        (button) -> onButtonEditClick(), true));
            }

            int searchWidth = 113;
            this.searchField = new WidgetTextFieldExtended(ContainerScreenLogicProgrammerBase.this.font, 0, 0, searchWidth, 11,
                    new TranslationTextComponent("gui.cyclopscore.search"));
            this.searchField.setMaxLength(64);
            this.searchField.setBordered(true);
            this.searchField.setVisible(false);
            this.searchField.setTextColor(16777215);
            this.searchField.setCanLoseFocus(true);
            this.searchField.setValue("");
            this.searchField.setWidth(searchWidth);
        }

        @Override
        public void init(int guiLeft, int guiTop) {
            super.init(guiLeft, guiTop);
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
        protected ITextComponent getLastError() {
            return container.getLastError();
        }

        @Override
        protected ResourceLocation getTexture() {
            return texture;
        }

        @Override
        public boolean charTyped(char typedChar, int keyCode) {
            if (!this.searchField.isFocused() || !this.searchField.charTyped(typedChar, keyCode)) {
                return super.charTyped(typedChar, keyCode);
            } else {
                label(this.searchField.getValue());
                return true;
            }
        }

        @Override
        public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
            if (this.searchField.isFocused() && typedChar != GLFW.GLFW_KEY_ESCAPE) {
                label(this.searchField.getValue());
                this.searchField.keyPressed(typedChar, keyCode, modifiers);
                return true;
            }
            return super.keyPressed(typedChar, keyCode, modifiers);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            if(this.searchField.isVisible() && this.searchField.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, int guiLeft, int guiTop, TextureManager textureManager, FontRenderer font, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(matrixStack, guiLeft, guiTop, textureManager, font, partialTicks, mouseX, mouseY);
            Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
            this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        public void onButtonEditClick() {
            this.searchField.setVisible(!this.searchField.isVisible());
            if(this.searchField.isVisible()) {
                this.searchField.changeFocus(true);
                label(this.searchField.getValue());
            } else {
                label("");
            }
        }
    }

}
