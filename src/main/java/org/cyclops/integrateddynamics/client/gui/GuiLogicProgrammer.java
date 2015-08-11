package org.cyclops.integrateddynamics.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.helper.StringHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammer;
import org.cyclops.integrateddynamics.block.BlockLogicProgrammerConfig;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiBox;
import org.cyclops.integrateddynamics.core.client.gui.subgui.SubGuiHolder;
import org.cyclops.integrateddynamics.core.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.logicprogrammer.ILogicProgrammerElementType;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.logicprogrammer.SubGuiConfigRenderPattern;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerActivateElementPacket;

import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Gui for the {@link org.cyclops.integrateddynamics.block.BlockLogicProgrammer}.
 * @author rubensworks
 */
public class GuiLogicProgrammer extends ScrollingGuiContainer {

    private static final int ERROR_WIDTH = 13;
    private static final int ERROR_HEIGHT = 13;
    private static final int OK_WIDTH = 14;
    private static final int OK_HEIGHT = 12;

    public static final int BOX_HEIGHT = 18;
    private static final Rectangle ITEM_POSITION = new Rectangle(19, 18, 56, BOX_HEIGHT - 1);

    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();
    protected SubGuiConfigRenderPattern operatorConfigPattern = null;

    /**
     * Make a new instance.
     */
    public GuiLogicProgrammer(InventoryPlayer inventoryPlayer) {
        super(new ContainerLogicProgrammer(inventoryPlayer));
        ContainerLogicProgrammer container = (ContainerLogicProgrammer) getContainer();
        container.setGui(this);
    }

    @Override
    public void initGui() {
        super.initGui();
        subGuiHolder.initGui(this.guiLeft, this.guiTop);
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
        return 213;
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
        return getContainer().getGuiProvider().getMod().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
                + BlockLogicProgrammerConfig._instance.getNamedId() + ".png";
    }

    protected float colorSmoothener(float color, boolean hover) {
        return 1F - ((1F - color) / (hover ? 2F : 4F));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        subGuiHolder.drawGuiContainerBackgroundLayer(this.guiLeft, this.guiTop, mc.renderEngine, fontRendererObj, partialTicks, mouseX, mouseY);
        FontRenderer fontRenderer = fontRendererObj;

        // Draw container name
        fontRenderer.drawString(L10NHelpers.localize(BlockLogicProgrammer.getInstance().getLocalizedName()),
                this.guiLeft + offsetX + 87, this.guiTop + offsetY + 7, Helpers.RGBToInt(80, 80, 80));

        // Draw operators
        ContainerLogicProgrammer container = (ContainerLogicProgrammer) getScrollingInventoryContainer();
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
                            guiTop + offsetY + ITEM_POSITION.y + boxHeight * i, 14, 240, 3, 16);
                }
                GlStateManager.disableAlpha();
                GlStateManager.color(1, 1, 1);

                // Operator info
                String aspectName = L10NHelpers.localize(element.getSymbol());
                RenderHelpers.drawScaledCenteredString(fontRenderer, aspectName,
                        this.guiLeft + offsetX + (hover ? 20 : 18),
                        this.guiTop + offsetY + 26 + boxHeight * i,
                        60, Helpers.RGBToInt(40, 40, 40));
            }
        }
    }

    protected Rectangle getElementPosition(ContainerLogicProgrammer container, int i, boolean absolute) {
        return new Rectangle(ITEM_POSITION.x + offsetX + (absolute ? this.guiLeft : 0),
                ITEM_POSITION.y + BOX_HEIGHT * i + offsetY + (absolute ? this.guiTop : 0),
                ITEM_POSITION.width, ITEM_POSITION.height
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        subGuiHolder.drawGuiContainerForegroundLayer(this.guiLeft, this.guiTop, mc.renderEngine, fontRendererObj, mouseX, mouseY);
        // Draw operator tooltips
        ContainerLogicProgrammer container = (ContainerLogicProgrammer) getScrollingInventoryContainer();
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

    protected void onActivateElement(ILogicProgrammerElement element) {
        subGuiHolder.addSubGui(operatorConfigPattern = element.createSubGui(88, 18, 160, 87, this, (ContainerLogicProgrammer) getContainer()));
        operatorConfigPattern.initGui(guiLeft, guiTop);
        subGuiHolder.addSubGui(new SubGuiOperatorInfo(element));
    }

    protected void onDeactivateElement(ILogicProgrammerElement element) {
        subGuiHolder.clear();
    }

    public void handleElementActivation(ILogicProgrammerElement element) {
        ContainerLogicProgrammer container = (ContainerLogicProgrammer) getScrollingInventoryContainer();
        ILogicProgrammerElement newActive = null;
        onDeactivateElement(element);
        if(container.getActiveElement() != element) {
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
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(!subGuiHolder.keyTyped(this.checkHotbarKeys(keyCode), typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        subGuiHolder.mouseClicked(mouseX, mouseY, mouseButton);
        ContainerLogicProgrammer container = (ContainerLogicProgrammer) getScrollingInventoryContainer();
        for(int i = 0; i < container.getPageSize(); i++) {
            if (container.isElementVisible(i)) {
                ILogicProgrammerElement element = container.getVisibleElement(i);
                if (isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    handleElementActivation(element);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public class SubGuiOperatorInfo extends SubGuiBox.Base {

        private final ILogicProgrammerElement element;

        public SubGuiOperatorInfo(ILogicProgrammerElement element) {
            super(Box.DARK, 88, 106, 139, 20);
            this.element = element;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

            int x = guiLeft + getX();
            int y = guiTop + getY();

            fontRenderer.drawString(element.getLocalizedNameFull(), x + 2, y + 6, Helpers.RGBToInt(240, 240, 240));

            ContainerLogicProgrammer container = (ContainerLogicProgrammer) getContainer();
            if(container.canWriteActiveElementPre()) {
                L10NHelpers.UnlocalizedString lastError = container.getLastError();
                mc.renderEngine.bindTexture(texture);
                if (lastError != null) {
                    drawTexturedModalRect(x + 120, y + 3, 0, 231, ERROR_WIDTH, ERROR_HEIGHT);
                } else {
                    drawTexturedModalRect(x + 120, y + 5, 0, 244, OK_WIDTH, OK_HEIGHT);
                }
            }
        }

        @Override
        public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);

            int x = getX();
            int y = getY();

            ContainerLogicProgrammer container = (ContainerLogicProgrammer) getContainer();
            if(container.canWriteActiveElementPre()) {
                L10NHelpers.UnlocalizedString lastError = container.getLastError();
                if (lastError != null && isPointInRegion(x + 120, y + 3, ERROR_WIDTH, ERROR_HEIGHT, mouseX, mouseY)) {
                    List<String> lines = Lists.newLinkedList();
                    lines.addAll(StringHelpers.splitLines(lastError.localize(), L10NHelpers.MAX_TOOLTIP_LINE_LENGTH,
                            EnumChatFormatting.RED.toString()));
                    drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

    }

}
