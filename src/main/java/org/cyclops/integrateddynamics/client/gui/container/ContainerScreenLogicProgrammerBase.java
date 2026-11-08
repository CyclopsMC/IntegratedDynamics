package org.cyclops.integrateddynamics.client.gui.container;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonText;
import org.cyclops.cyclopscore.client.gui.component.input.WidgetTextFieldExtended;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenScrolling;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.client.gui.subgui.IGuiInputElement;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.SubGuiValueTypeInfoBase;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionParseException;
import org.cyclops.integrateddynamics.core.evaluate.expression.tree.ExpressionPrinter;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionStartPanel;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionTextController;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionPanelRendering;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.ExpressionTextField;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.OperatorLPElementRenderPattern;
import org.cyclops.integrateddynamics.core.logicprogrammer.client.RenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerActivateElementPacket;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerLabelPacket;
import org.cyclops.integrateddynamics.proxy.ClientProxy;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

/**
 * Base gui for the logic programmer.
 * @author rubensworks
 */
public class ContainerScreenLogicProgrammerBase<C extends ContainerLogicProgrammerBase>
        extends ContainerScreenScrolling<C>
        implements ContainerLogicProgrammerBase.ScreenCallbackHandler {

    public static final int BOX_HEIGHT = 18;
    private static final Rectangle ITEM_POSITION = new Rectangle(19, 18, 56, BOX_HEIGHT - 1);

    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();
    protected final ExpressionStartPanel expressionStartPanel;
    private final boolean hasLabeller;
    protected RenderPattern operatorConfigPattern = null;
    protected SubGuiOperatorInfo operatorInfoPattern = null;
    protected boolean firstInit = true;
    protected int relativeStep = -1;
    protected boolean swallowNextCharacter = false;
    private ButtonText resetButton;
    private ButtonText expressionPanelButton;

    /**
     * If the expression side panel is shown, hidden at first and remembered for the rest of the game session.
     */
    private static boolean expressionPanelOpen = false;
    /**
     * The typed text of the expression, kept while the panel of the active element is recreated.
     */
    @Nullable
    private ExpressionTextField expressionTextField = null;

    public ContainerScreenLogicProgrammerBase(C container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        container.setScreenCallbackHandler(this);
        this.titleLabelX = 87;
        this.expressionStartPanel = new ExpressionStartPanel(this, container);

        this.hasLabeller = playerInventory.contains(new ItemStack(RegistryEntries.ITEM_LABELLER));
    }

    @Override
    protected Rectangle getScrollRegion() {
        return new Rectangle(this.leftPos + 19, this.topPos + 18, 57, 178);
    }

    @Override
    public void init() {
        super.init();

        // Button to deselect the active LP element
        addRenderableWidget(this.resetButton = new ButtonText(this.leftPos + 87,  this.topPos + 4,
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_RESET),
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_RESET), button -> {
            if (container.getActiveElement() != null) {
                container.returnWriteItemToPlayer();
                handleElementActivation(container.getActiveElement(), true);
            }
        }));
        this.resetButton.setHeight(12);
        resetButton.visible = container.getActiveElement() != null;

        // Button to show or hide the expression side panel
        addRenderableWidget(this.expressionPanelButton = new ButtonText(this.leftPos + 238, this.topPos + 4, 12, 12,
                Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TOGGLE),
                Component.literal(expressionPanelOpen ? "\u00ab" : "\u00bb"), button -> {
            expressionPanelOpen = !expressionPanelOpen;
            rebuildWidgets();
        }, true));

        subGuiHolder.init(this.leftPos, this.topPos);
        if (firstInit) {
            setSearchFieldFocussed(true);
            firstInit = false;
        }
    }

    /**
     * @return The typed text of the expression, kept while the panel of the active element is recreated.
     */
    public ExpressionTextField getExpressionTextField() {
        if (expressionTextField == null) {
            expressionTextField = new ExpressionTextField(Minecraft.getInstance().font, ExpressionPanelRendering.TEXT_WIDTH);
        }
        return expressionTextField;
    }

    @Override
    public void resetExpressionText() {
        if (expressionTextField != null) {
            expressionTextField.reset();
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        subGuiHolder.tick();
        if (operatorConfigPattern instanceof OperatorLPElementRenderPattern expressionPattern) {
            expressionPattern.tickText();
        }
        expressionStartPanel.tick();
    }

    @Override
    public void onClose() {
        // Typed text is applied without confirming it, so also when closing right after typing, before the server closes
        applyPendingText();
        super.onClose();
    }

    protected void applyPendingText() {
        if (operatorConfigPattern instanceof OperatorLPElementRenderPattern expressionPattern) {
            expressionPattern.applyPendingText();
        }
        expressionStartPanel.applyPendingText();
    }

    /**
     * @return Why the typed text of the expression can not be applied, which keeps the card from being written, or null.
     */
    @Nullable
    public ExpressionParseException getExpressionDraftError() {
        return operatorConfigPattern instanceof OperatorLPElementRenderPattern expressionPattern ? expressionPattern.getDraftError() : null;
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, int slotId, int mouseButton, ContainerInput clickType) {
        if (slot != null && getMenu().isWriteSlot(slot)) {
            // The card gets the typed text, which is otherwise only applied once typing pauses, and the server reads it first
            applyPendingText();
            if (getExpressionDraftError() != null) {
                return;
            }
        }
        super.slotClicked(slot, slotId, mouseButton, clickType);
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
        return isExpressionPanelOpen() ? ContainerLogicProgrammerBase.EXPRESSION_PANEL_X + ContainerLogicProgrammerBase.EXPRESSION_PANEL_WIDTH : 256;
    }

    /**
     * @return If the expression side panel is shown.
     */
    public static boolean isExpressionPanelOpen() {
        return expressionPanelOpen;
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
    protected Identifier constructGuiTexture() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/logic_programmer.png");
    }

    protected float colorSmoothener(float color, boolean hover) {
        return 1F - ((1F - color) / (hover ? 2F : 4F));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(guiGraphics, mouseX, mouseY, partialTicks);
        subGuiHolder.renderBg(guiGraphics, this.leftPos, this.topPos, getMinecraft().getTextureManager(), font, partialTicks, mouseX, mouseY);
        expressionStartPanel.render(guiGraphics, font, mouseX, mouseY, partialTicks);

        // Draw container name
        guiGraphics.text(font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_FILTER),
                this.leftPos + offsetX + 5, this.topPos + offsetY + 208, IModHelpers.get().getBaseHelpers().RGBAToInt(80, 80, 80, 255),
                false);

        // Draw operators
        ContainerLogicProgrammerBase container = (ContainerLogicProgrammerBase) getMenu();
        int boxHeight = BOX_HEIGHT;
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);

                Triple<Float, Float, Float> rgb = IModHelpers.get().getBaseHelpers().intToRGB(element.getColor());
                boolean hover = isElementShown(element)
                        || isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY));
                int color = ARGB.colorFromFloat(
                        1F,
                        colorSmoothener(rgb.getLeft(), hover),
                        colorSmoothener(rgb.getMiddle(), hover),
                        colorSmoothener(rgb.getRight(), hover)
                );

                // Background
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + offsetX + ITEM_POSITION.x,
                        topPos + offsetY + ITEM_POSITION.y + boxHeight * i, 19, 18, ITEM_POSITION.width, ITEM_POSITION.height, 256, 256, color);

                // Arrow
                if(hover) {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + offsetX + ITEM_POSITION.x,
                            topPos + offsetY + ITEM_POSITION.y + boxHeight * i, 0, 240, 3, 16, 256, 256, color);
                }

                // Operator info
                String aspectName = element.getSymbol();
                IModHelpers.get().getRenderHelpers().drawScaledCenteredString(guiGraphics, font, aspectName,
                        this.leftPos + offsetX + (hover ? 22 : 21),
                        this.topPos + offsetY + 26 + boxHeight * i,
                        53, IModHelpers.get().getBaseHelpers().RGBAToInt(40, 40, 40, 255), false, Font.DisplayMode.NORMAL);
            }
        }

        // Draw arrow on write slot
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + offsetX + ContainerLogicProgrammerBase.OUTPUT_X - 4,
                topPos + offsetY + ContainerLogicProgrammerBase.OUTPUT_Y - 4, subGuiHolder.isEmpty() ? 7 : 3, 240, 4, 4, 256, 256);
    }

    /**
     * Draw the background texture, and the expression side panel to the right of it, which the texture partly covers.
     * @param guiGraphics The gui graphics.
     */
    protected void drawBackgroundTexture(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // The texture at its own width, and not that of the gui with the panel
        int imageWidthWithPanel = imageWidth;
        imageWidth = 256 + 2 * offsetX;
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        imageWidth = imageWidthWithPanel;
        if (isExpressionPanelOpen()) {
            int x = leftPos + offsetX;
            int y = topPos + offsetY;
            drawPanel(guiGraphics, x + ContainerLogicProgrammerBase.EXPRESSION_PANEL_X, y + ContainerLogicProgrammerBase.EXPRESSION_PANEL_Y,
                    ContainerLogicProgrammerBase.EXPRESSION_PANEL_WIDTH, ContainerLogicProgrammerBase.EXPRESSION_PANEL_HEIGHT);
            // The edge of the texture covers the left of the panel
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + ContainerLogicProgrammerBase.EXPRESSION_PANEL_X, y,
                    ContainerLogicProgrammerBase.EXPRESSION_PANEL_X, 0, 256 - ContainerLogicProgrammerBase.EXPRESSION_PANEL_X, 240, 256, 256);
        }
    }

    /**
     * Draw a panel in the vanilla style.
     */
    public static void drawPanel(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        int border = ARGB.color(0, 0, 0);
        int background = ARGB.color(198, 198, 198);
        int light = ARGB.color(255, 255, 255);
        int shadow = ARGB.color(85, 85, 85);
        guiGraphics.fill(x + 1, y, x + width - 1, y + height, border);
        guiGraphics.fill(x, y + 1, x + width, y + height - 1, border);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, background);
        guiGraphics.fill(x + 1, y + 1, x + width - 3, y + 3, light);
        guiGraphics.fill(x + 1, y + 1, x + 3, y + height - 3, light);
        guiGraphics.fill(x + 3, y + height - 3, x + width - 1, y + height - 1, shadow);
        guiGraphics.fill(x + width - 3, y + 3, x + width - 1, y + height - 1, shadow);
    }

    /**
     * Called when the expression node being edited in the active operator element has changed.
     */
    @Override
    public void onExpressionNavigated() {
        ILogicProgrammerElement element = container.getActiveElement();
        if (element == null) {
            return;
        }
        container.setExpressionSlotFilter(null);
        subGuiHolder.removeSubGui(operatorConfigPattern);
        subGuiHolder.addSubGui(operatorConfigPattern = (RenderPattern) element.getClient().createSubGui(ContainerLogicProgrammerBase.BASE_X, ContainerLogicProgrammerBase.BASE_Y, ContainerLogicProgrammerBase.MAX_WIDTH, ContainerLogicProgrammerBase.MAX_HEIGHT, this, (ContainerLogicProgrammerBase) getMenu()));
        operatorConfigPattern.init(leftPos, topPos);
        container.refreshElementInventory(element, operatorConfigPattern.getX(), operatorConfigPattern.getY());
    }

    protected Rectangle getElementPosition(ContainerLogicProgrammerBase container, int i, boolean absolute) {
        return new Rectangle(ITEM_POSITION.x + offsetX + (absolute ? this.leftPos : 0),
                ITEM_POSITION.y + BOX_HEIGHT * i + offsetY + (absolute ? this.topPos : 0),
                ITEM_POSITION.width, ITEM_POSITION.height
        );
    }

    @Override
    protected void drawCurrentScreen(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawCurrentScreen(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.nextStratum();
        // The list or signature shown while typing an expression, over everything but tooltips
        ExpressionTextController.getTextField(getMenu()).renderPopups(guiGraphics);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // Not for slots below the list or signature shown while typing
        if (!ExpressionTextController.getTextField(getMenu()).isOverPopups(mouseX, mouseY)) {
            super.extractTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack itemStack) {
        List<Component> lines = super.getTooltipFromContainerItem(itemStack);
        // Why the card can not be taken
        ExpressionParseException draftError = getExpressionDraftError();
        if (hoveredSlot != null && getMenu().isWriteSlot(hoveredSlot) && draftError != null) {
            lines = Lists.newArrayList(lines);
            lines.add(getDraftErrorMessage(draftError).withStyle(ChatFormatting.RED));
        }
        // A constant that its item does not show exactly, such as the amount of a fluid in a bucket
        if (hoveredSlot != null && hoveredSlot.container == getMenu().getTemporaryInputSlots()
                && getMenu().getActiveElement() instanceof OperatorLPElement element) {
            IValue value = element.getValueNotShownByItem(hoveredSlot.getContainerSlot());
            if (value != null) {
                lines = Lists.newArrayList(lines);
                lines.add(Math.min(1, lines.size()), Component.literal(ExpressionPrinter.printValue(value)).withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(guiGraphics, this.leftPos, this.topPos, getMinecraft().getTextureManager(), font, mouseX, mouseY);

        expressionStartPanel.renderTooltips(guiGraphics, mouseX, mouseY);

        // Tooltip on the expression panel button
        if (expressionPanelButton.isHovered()) {
            this.drawTooltip(Lists.newArrayList(Component.translatable(isExpressionPanelOpen()
                    ? L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TOGGLE_HIDE : L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_TOGGLE_SHOW)),
                    guiGraphics, mouseX, mouseY);
        }

        // Draw usage information
        if (subGuiHolder.isEmpty()) {
            // Create
            Images.ARROW_LEFT.draw(guiGraphics, offsetX + 85, offsetY + 17);
            guiGraphics.text(font, Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_INFO_CREATE),
                    offsetX + 100, offsetY + 23, IModHelpers.get().getBaseHelpers().RGBAToInt(80, 80, 80, 255), false);

            // Modify
            Images.ARROW_DOWN.draw(guiGraphics, offsetX + 230, offsetY + 90);
            MutableComponent modifyComponent = Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_INFO_MODIFY);
            guiGraphics.text(font, modifyComponent,
                    offsetX + 230 - font.width(modifyComponent), offsetY + 95, IModHelpers.get().getBaseHelpers().RGBAToInt(80, 80, 80, 255), false);

            // Tooltip on write slot
            if (this.isHovering(ContainerLogicProgrammerBase.OUTPUT_X, ContainerLogicProgrammerBase.OUTPUT_Y,
                    ContainerScreenLogicProgrammerBase.BOX_HEIGHT, ContainerScreenLogicProgrammerBase.BOX_HEIGHT, mouseX, mouseY)
                    && Minecraft.getInstance().player.containerMenu.getCarried().isEmpty()
                    && !container.hasWriteItemInSlot()) {
                this.drawTooltip(Lists.newArrayList(Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_TOOLTIP_WRITESLOT_MODIFY)), guiGraphics, mouseX, mouseY);
            }
        }

        // Draw operator tooltips
        ContainerLogicProgrammerBase container = getMenu();
        for(int i = 0; i < container.getPageSize(); i++) {
            if(container.isElementVisible(i)) {
                ILogicProgrammerElement<?, ?, ?, ?> element = container.getVisibleElement(i);
                if(isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    List<Component> lines = Lists.newLinkedList();
                    element.loadTooltip(lines::add);
                    drawTooltip(lines, guiGraphics, mouseX, mouseY);
                }
            }
        }
    }

    protected void onActivateElement(ILogicProgrammerElement<RenderPattern, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase, ?> element) {
        this.resetButton.setFocused(false);
        subGuiHolder.addSubGui(operatorInfoPattern = new SubGuiOperatorInfo(element));
        operatorInfoPattern.init(leftPos, topPos);
        subGuiHolder.addSubGui(operatorConfigPattern = element.getClient().createSubGui(ContainerLogicProgrammerBase.BASE_X, ContainerLogicProgrammerBase.BASE_Y, ContainerLogicProgrammerBase.MAX_WIDTH, ContainerLogicProgrammerBase.MAX_HEIGHT, this, (ContainerLogicProgrammerBase) getMenu()));
        operatorConfigPattern.init(leftPos, topPos);
    }

    /**
     * @return If the element is shown as selected: the active element, or the operator that is open in the slots of an expression.
     */
    protected boolean isElementShown(ILogicProgrammerElement element) {
        ILogicProgrammerElement active = getMenu().getActiveElement();
        if (active instanceof OperatorLPElement activeOperator && element instanceof OperatorLPElement operatorElement) {
            return operatorElement.getOperator() == activeOperator.getCurrentOperator();
        }
        return LogicProgrammerElementTypes.areEqual(active, element);
    }

    protected void onDeactivateElement(ILogicProgrammerElement element) {
        container.setExpressionSlotFilter(null);
        subGuiHolder.clear();
    }

    @Override
    public boolean handleElementActivation(ILogicProgrammerElement element, boolean sendToServer) {
        boolean activate = false;
        boolean deselect = false;
        ContainerLogicProgrammerBase container = getMenu();
        ILogicProgrammerElement newActive = null;
        if (container.getActiveElement() == element) {
            // Only allow deselection of the current LP element if the write slot is empty
            if (!container.hasWriteItemInSlot()) {
                deselect = true;
                onDeactivateElement(element);
            }
        } else {
            // Swap to another LP element
            onDeactivateElement(element);
            activate = true;
            newActive = element;
            if(element != null) {
                onActivateElement(element);
            }
        }
        resetButton.visible = newActive != null;
        if (activate || deselect) {
            container.setActiveElement(newActive,
                    operatorConfigPattern == null ? 0 : operatorConfigPattern.getX(),
                    operatorConfigPattern == null ? 0 : operatorConfigPattern.getY());
        }
        if (sendToServer) {
            if (newActive != null) {
                ILogicProgrammerElementType type = newActive.getType();
                IntegratedDynamics._instance.getPacketHandler().sendToServer(
                        new LogicProgrammerActivateElementPacket(type.getUniqueName(), type.getName(newActive)));
            } else if (deselect) {
                IntegratedDynamics._instance.getPacketHandler().sendToServer(
                        new LogicProgrammerActivateElementPacket(Identifier.parse(""), Identifier.parse("")));
            }
        }
        return activate;
    }

    @Nullable
    @Override
    public RenderPattern getOperatorConfigPattern() {
        return operatorConfigPattern;
    }

    protected void setSearchFieldFocussed(boolean focused) {
        getSearchField().setFocused(focused);
    }

    protected boolean isSearchFieldFocussed() {
        return getSearchField().isFocused();
    }

    protected boolean selectPageElement(int elementId) {
        ContainerLogicProgrammerBase container = getMenu();

        // Deactivate current element
        if (elementId < 0) {
            handleElementActivation(container.getActiveElement(), true);
            return false;
        }

        // Activate a new element
        for(int i = 0; i < container.getPageSize(); i++) {
            if (container.isElementVisible(i)) {
                if (elementId-- == 0) {
                    ILogicProgrammerElement element = container.getVisibleElement(i);
                    if (container.getActiveElement() != element) {
                        handleElementActivation(element, true);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean handleKeyCode(KeyEvent evt) {
        InputConstants.Key inputCode = InputConstants.getKey(evt);
        if(evt.key() != InputConstants.KEY_LSHIFT && evt.key() != InputConstants.KEY_RSHIFT) {
            ContainerLogicProgrammerBase container = getMenu();
            int pageSize = container.getPageSize();
            int stepModifier = IModHelpers.get().getMinecraftClientHelpers().isShifted() ? pageSize - 1 : 1;
            boolean isElementFocused = container.getActiveElement() != null && container.getActiveElement().getClient().isFocused(operatorConfigPattern);

            if (ClientProxy.FOCUS_LP_SEARCH.isActiveAndMatches(inputCode)) {
                // Focus search field
                setSearchFieldFocussed(true);
                swallowNextCharacter = true;
                return true;
            } else if (isElementFocused && ClientProxy.FOCUS_LP_RENAME.isActiveAndMatches(inputCode) && hasLabeller()) {
                // Open labeller gui
                operatorInfoPattern.onButtonEditClick();
                swallowNextCharacter = true;
                return true;
            } else if (InputConstants.KEY_LEFT == evt.key() && (!isElementFocused && isSearchFieldFocussed())) {
                // Unfocus search field
                setSearchFieldFocussed(isSearchFieldFocussed());
                return true;
            } else if (!isElementFocused && InputConstants.KEY_DOWN == evt.key()) {
                // Scroll down
                if (!selectPageElement(relativeStep += stepModifier)) {
                    relativeStep -= stepModifier;
                    if (relativeStep > 0) {
                        getScrollbar().scrollRelative(-stepModifier);
                        selectPageElement(relativeStep);
                    }
                }
                return true;
            } else if (!isElementFocused && InputConstants.KEY_UP == evt.key()) {
                // Scroll up
                if (!(relativeStep >= 0 && selectPageElement(relativeStep -= stepModifier))) {
                    getScrollbar().scrollRelative(stepModifier);
                    selectPageElement(relativeStep = 0);
                }
                return true;
            } else if (!isElementFocused
                    && (InputConstants.KEY_RIGHT == evt.key() || InputConstants.KEY_TAB == evt.key()
                    || InputConstants.KEY_RETURN == evt.key() || InputConstants.KEY_NUMPADENTER == evt.key())) {
                if (container.getActiveElement() != null) {
                    container.getActiveElement().getClient().setFocused(operatorConfigPattern, true);
                    setSearchFieldFocussed(false);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        if (swallowNextCharacter) {
            swallowNextCharacter = false;
            return true;
        }

        return expressionStartPanel.charTyped(evt) || subGuiHolder.charTyped(evt) || handleKeyCode(new KeyEvent(evt.codepoint(), 0, 0))
                || super.charTyped(evt);
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        // Escape first cancels an expression slot or parameter selection, before closing the gui
        if (evt.key() == InputConstants.KEY_ESCAPE && ((operatorConfigPattern instanceof OperatorLPElementRenderPattern expressionPattern
                && expressionPattern.cancelSelection()) || expressionStartPanel.cancel())) {
            return true;
        }
        if (evt.key() != InputConstants.KEY_ESCAPE) {
            if (expressionStartPanel.keyPressed(evt) || this.subGuiHolder.keyPressed(evt) || handleKeyCode(evt)) {
                return true;
            }
        }
        return super.keyPressed(evt);
    }

    // Sub guis only receive clicks, the text editor of an expression also scrolls and selects,
    // and parameters of an expression are dragged onto slots
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if ((operatorConfigPattern instanceof OperatorLPElementRenderPattern expressionPattern
                && expressionPattern.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
                || expressionStartPanel.mouseScrolled(mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent evt, double dragX, double dragY) {
        if ((operatorConfigPattern instanceof OperatorLPElementRenderPattern expressionPattern
                && expressionPattern.mouseDragged(evt.x(), evt.y(), evt.button(), dragX, dragY))
                || expressionStartPanel.mouseDragged(evt.x(), evt.y())) {
            return true;
        }
        return super.mouseDragged(evt, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent evt) {
        if (operatorConfigPattern instanceof OperatorLPElementRenderPattern expressionPattern) {
            expressionPattern.mouseReleased(evt.x(), evt.y(), evt.button());
        }
        expressionStartPanel.mouseReleased();
        return super.mouseReleased(evt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        if (expressionStartPanel.mouseClicked(evt, isDoubleClick) || subGuiHolder.mouseClicked(evt, isDoubleClick)) {
            if (isSearchFieldFocussed()) {
                setSearchFieldFocussed(false);
            }
            return true;
        }

        ContainerLogicProgrammerBase container = getMenu();
        for(int i = 0; i < container.getPageSize(); i++) {
            if (container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);
                if (isPointInRegion(getElementPosition(container, i, false), new Point((int) evt.x(), (int) evt.y()))) {
                    // Nest the operator in the selected expression slot
                    if (element instanceof OperatorLPElement operatorElement
                            && operatorConfigPattern instanceof OperatorLPElementRenderPattern expressionPattern
                            && expressionPattern.onOperatorPicked(operatorElement.getOperator())) {
                        return true;
                    }
                    // Activating the operator that is open in the slots would replace the whole expression
                    if (!LogicProgrammerElementTypes.areEqual(container.getActiveElement(), element) && isElementShown(element)) {
                        return true;
                    }
                    boolean activated = handleElementActivation(element, true);
                    relativeStep = activated ? i : -1;
                    if (activated) {
                        container.getActiveElement().getClient().setFocused(operatorConfigPattern, true);
                        setSearchFieldFocussed(false);
                        return true;
                    }
                }
            }
        }
        boolean superRet = super.mouseClicked(evt, isDoubleClick);

        // If the search box has been selected, de-active the current element.
        if(isSearchFieldFocussed() &&
                container.getActiveElement() != null && container.getActiveElement().getClient().isFocused(operatorConfigPattern)) {
            container.getActiveElement().getClient().setFocused(operatorConfigPattern, false);
            return true;
        }

        return superRet;
    }

    protected static MutableComponent getDraftErrorMessage(ExpressionParseException error) {
        return Component.translatable(L10NValues.GUI_LOGICPROGRAMMER_EXPRESSION_DRAFT_ERROR, error.getComponent());
    }

    protected void label(String label) {
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerLabelPacket(label));
    }

    protected boolean hasLabeller() {
        return this.hasLabeller;
    }

    public class SubGuiOperatorInfo extends SubGuiValueTypeInfoBase<RenderPattern, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase> {

        private WidgetTextFieldExtended searchField;
        private ButtonText button = null;

        public SubGuiOperatorInfo(IGuiInputElement<RenderPattern, ContainerScreenLogicProgrammerBase<?>, ContainerLogicProgrammerBase, ?> element) {
            super(ContainerScreenLogicProgrammerBase.this, getMenu(), element, 88, 106, 139, 20);

            if(hasLabeller()) {
                buttonList.add(button = new ButtonText(0, 0, 6, 10, Component.translatable("gui.integrateddynamics.button.edit"), Component.literal("E"),
                        (button) -> onButtonEditClick(), true));
            }

            int searchWidth = 113;
            this.searchField = new WidgetTextFieldExtended(ContainerScreenLogicProgrammerBase.this.font, 0, 0, searchWidth, 11,
                    Component.translatable("gui.cyclopscore.search"));
            this.searchField.setMaxLength(64);
            this.searchField.setBordered(true);
            this.searchField.setVisible(false);
            this.searchField.setTextColor(ARGB.opaque(16777215));
            this.searchField.setCanLoseFocus(true);
            this.searchField.setValue("");
            this.searchField.setWidth(searchWidth);
        }

        @Override
        public void init(int guiLeft, int guiTop) {
            super.init(guiLeft, guiTop);
            int searchX = 90;
            int searchY = 110;
            this.searchField.setX(guiLeft + searchX);
            this.searchField.setY(guiTop + searchY);

            if (hasLabeller()) {
                button.setX(guiLeft + 220);
                button.setY(guiTop + 111);
            }
        }

        @Override
        protected boolean showError() {
            return getExpressionDraftError() != null || container.canWriteActiveElementPre();
        }

        @Override
        protected Component getLastError() {
            ExpressionParseException draftError = getExpressionDraftError();
            return draftError != null ? getDraftErrorMessage(draftError) : container.getLastError();
        }

        @Override
        protected Identifier getTexture() {
            return texture;
        }

        @Override
        public boolean charTyped(CharacterEvent evt) {
            if (!this.searchField.isFocused() || !this.searchField.charTyped(evt)) {
                return super.charTyped(evt);
            } else {
                label(this.searchField.getValue());
                return true;
            }
        }

        @Override
        public boolean keyPressed(KeyEvent evt) {
            if (this.searchField.isFocused() && evt.key() != InputConstants.KEY_ESCAPE) {
                this.searchField.keyPressed(evt);
                label(this.searchField.getValue());
                return true;
            }
            return super.keyPressed(evt);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
            if(this.searchField.isVisible() && this.searchField.mouseClicked(evt, isDoubleClick)) {
                return true;
            }
            return super.mouseClicked(evt, isDoubleClick);
        }

        @Override
        public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font font, float partialTicks, int mouseX, int mouseY) {
            super.renderBg(guiGraphics, guiLeft, guiTop, textureManager, font, partialTicks, mouseX, mouseY);
            this.searchField.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean shouldRenderElementName() {
            return !this.searchField.isVisible();
        }

        public void onButtonEditClick() {
            this.searchField.setVisible(!this.searchField.isVisible());
            if(this.searchField.isVisible()) {
                this.searchField.setFocused(true);
                label(this.searchField.getValue());
            } else {
                label("");
            }
        }
    }

}
