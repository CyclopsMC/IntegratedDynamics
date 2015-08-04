package org.cyclops.integrateddynamics.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.tuple.Pair;
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
import org.cyclops.integrateddynamics.core.evaluate.operator.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerActivateOperatorPacket;

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

    private static final int BOX_HEIGHT = 18;
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
                IOperator operator = container.getVisibleElement(i);

                GlStateManager.disableAlpha();
                Triple<Float, Float, Float> rgb = Helpers.intToRGB(operator.getOutputType().getDisplayColor());
                boolean hover = container.getActiveOperator() == operator
                        || isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY));
                GlStateManager.color(colorSmoothener(rgb.getLeft(), hover), colorSmoothener(rgb.getMiddle(), hover),
                        colorSmoothener(rgb.getRight(), hover), 1);

                // Background
                mc.renderEngine.bindTexture(texture);
                drawTexturedModalRect(guiLeft + offsetX + ITEM_POSITION.x,
                        guiTop + offsetY + ITEM_POSITION.y + boxHeight * i, 19, 18, ITEM_POSITION.width, ITEM_POSITION.height);
                GlStateManager.color(1, 1, 1);

                // Operator info
                String aspectName = L10NHelpers.localize(operator.getSymbol());
                RenderHelpers.drawScaledCenteredString(fontRenderer, aspectName,
                        this.guiLeft + offsetX + 18,
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
                IOperator operator = container.getVisibleElement(i);
                if(isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    java.util.List<String> lines = Lists.newLinkedList();
                    operator.loadTooltip(lines, true);
                    drawTooltip(lines, mouseX - this.guiLeft, mouseY - this.guiTop);
                }
            }
        }
    }

    protected void onActivateOperator(IOperator operator) {
        subGuiHolder.addSubGui(operatorConfigPattern = new SubGuiConfigRenderPattern(operator, 88, 18, 160, 87));
        subGuiHolder.addSubGui(new SubGuiOperatorInfo(operator));
    }

    protected void onDeactivateOperator(IOperator operator) {
        subGuiHolder.clear();
    }

    public void handleOperatorActivation(IOperator operator) {
        ContainerLogicProgrammer container = (ContainerLogicProgrammer) getScrollingInventoryContainer();
        IOperator newActive = null;
        onDeactivateOperator(operator);
        if(container.getActiveOperator() != operator) {
            newActive = operator;
            if(operator != null) {
                onActivateOperator(operator);
            }
        }
        container.setActiveOperator(newActive,
                operatorConfigPattern == null ? 0 : operatorConfigPattern.getX(),
                operatorConfigPattern == null ? 0 : operatorConfigPattern.getY());
        IntegratedDynamics._instance.getPacketHandler().sendToServer(new LogicProgrammerActivateOperatorPacket(
                newActive == null ? "" : operator.getUnlocalizedName()));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ContainerLogicProgrammer container = (ContainerLogicProgrammer) getScrollingInventoryContainer();
        for(int i = 0; i < container.getPageSize(); i++) {
            if (container.isElementVisible(i)) {
                IOperator operator = container.getVisibleElement(i);
                if (isPointInRegion(getElementPosition(container, i, false), new Point(mouseX, mouseY))) {
                    handleOperatorActivation(operator);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public class SubGuiConfigRenderPattern extends SubGuiBox {

        private final IOperator operator;
        private final int x, y;

        public SubGuiConfigRenderPattern(IOperator operator, int baseX, int baseY, int maxWidth, int maxHeight) {
            super(Box.LIGHT);
            this.operator = operator;
            IConfigRenderPattern configRenderPattern = operator.getRenderPattern();
            this.x = baseX + (maxWidth  - configRenderPattern. getWidth()) / 2;
            this.y = baseY + (maxHeight - configRenderPattern.getHeight()) / 2;
        }

        protected void drawSlot(int x, int y) {
            this.drawTexturedModalRect(x, y, 3, 0, 18, 18);
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
            IConfigRenderPattern configRenderPattern = operator.getRenderPattern();

            int baseX = getX() + guiLeft;
            int baseY = getY() + guiTop;

            for(Pair<Integer, Integer> slot : configRenderPattern.getSlotPositions()) {
                drawSlot(baseX + slot.getLeft(), baseY + slot.getRight());
            }

            int width = fontRenderer.getStringWidth(operator.getSymbol());
            RenderHelpers.drawScaledCenteredString(fontRenderer, operator.getSymbol(),
                    baseX + configRenderPattern.getSymbolPosition().getLeft(),
                    baseY + configRenderPattern.getSymbolPosition().getRight() + 8,
                    width, 1, 0);
            GlStateManager.color(1, 1, 1);
        }

        @Override
        public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
            super.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
            IConfigRenderPattern configRenderPattern = operator.getRenderPattern();
            ContainerLogicProgrammer container = (ContainerLogicProgrammer) getContainer();

            // Input type tooltips
            IValueType[] valueTypes = operator.getInputTypes();
            for(int i = 0; i < valueTypes.length; i++) {
                IValueType valueType = valueTypes[i];
                IInventory temporaryInputSlots = container.getTemporaryInputSlots();
                if(temporaryInputSlots.getStackInSlot(i) == null) {
                    Pair<Integer, Integer> slotPosition = configRenderPattern.getSlotPositions()[i];
                    if(isPointInRegion(getX() + slotPosition.getLeft(), getY() + slotPosition.getRight(),
                            BOX_HEIGHT, BOX_HEIGHT, mouseX, mouseY)) {
                        List<String> lines = Lists.newLinkedList();
                        lines.add(valueType.getDisplayColorFormat() + L10NHelpers.localize(valueType.getUnlocalizedName()));
                        drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                    }
                }
            }

            // Output type tooltip
            IValueType outputType = operator.getOutputType();
            if(!container.hasWriteItemInSlot()) {
                if(isPointInRegion(ContainerLogicProgrammer.OUTPUT_X, ContainerLogicProgrammer.OUTPUT_Y,
                        BOX_HEIGHT, BOX_HEIGHT, mouseX, mouseY)) {
                    List<String> lines = Lists.newLinkedList();
                    lines.add(outputType.getDisplayColorFormat() + L10NHelpers.localize(outputType.getUnlocalizedName()));
                    drawTooltip(lines, mouseX - guiLeft, mouseY - guiTop);
                }
            }
        }

        @Override
        protected int getX() {
            return this.x;
        }

        @Override
        protected int getY() {
            return this.y;
        }

        @Override
        protected int getWidth() {
            return operator.getRenderPattern().getWidth();
        }

        @Override
        protected int getHeight() {
            return operator.getRenderPattern().getHeight();
        }
    }

    public class SubGuiOperatorInfo extends SubGuiBox.Base {

        private final IOperator operator;

        public SubGuiOperatorInfo(IOperator operator) {
            super(Box.DARK, 88, 106, 139, 20);
            this.operator = operator;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
            super.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);

            int x = guiLeft + getX();
            int y = guiTop + getY();

            fontRenderer.drawString(operator.getLocalizedNameFull(), x + 2, y + 6, Helpers.RGBToInt(240, 240, 240));

            ContainerLogicProgrammer container = (ContainerLogicProgrammer) getContainer();
            if(container.canWriteActiveOperatorPre()) {
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
            if(container.canWriteActiveOperatorPre()) {
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
