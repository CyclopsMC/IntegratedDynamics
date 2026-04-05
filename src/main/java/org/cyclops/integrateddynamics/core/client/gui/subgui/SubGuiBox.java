package org.cyclops.integrateddynamics.core.client.gui.subgui;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;

import java.util.List;

/**
 * A sub gui that simply renders a box.
 * @author rubensworks
 */
public abstract class SubGuiBox implements ISubGuiBox {

    protected static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/sub_gui.png");

    private final Box type;

    protected List<Button> buttonList = Lists.newArrayList();
    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();

    public SubGuiBox(Box type) {
        this.type = type;
    }

    @Override
    public void init(int guiLeft, int guiTop) {
        buttonList.clear();
        subGuiHolder.init(guiLeft, guiTop);
    }

    public void drawScreen(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < this.buttonList.size(); ++i) {
            this.buttonList.get(i).extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    protected boolean isDrawBackground() {
        return true;
    }

    @Override
    public void renderBg(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, float partialTicks, int mouseX, int mouseY) {
        if (this.isDrawBackground()) {
            int textureWidth = 19;
            int textureHeight = textureWidth;

            int x = guiLeft + getX();
            int y = guiTop + getY();
            int width = getWidth();
            int height = getHeight();
            int tx = type.getX();
            int ty = type.getY();

            // Corners
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, tx, tx, 1, 1, 256, 256); // top left
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + width - 1, y, tx + textureWidth - 1, ty, 1, 1, 256, 256); // top right
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + height - 1, 0, tx + textureHeight - 1, ty + 1, 1, 256, 256); // bottom left
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + width - 1, y + height - 1, tx + textureWidth - 1, ty + textureHeight - 1, 1, 1, 256, 256); // bottom right

            int i, j;

            // Sides
            i = 1;
            while (i < width - 1) {
                int currentWidth = Math.max(1, Math.min(width - i, textureWidth - 2) - 1);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + i, y, tx + 1, ty, currentWidth, 1, 256, 256);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + i, y + height - 1, tx + 1, ty + textureHeight - 1, currentWidth, 1, 256, 256);
                i += currentWidth;
            }

            i = 1;
            while (i < height - 1) {
                int currentHeight = Math.max(1, Math.min(height - i, textureHeight - 2) - 1);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + i, tx, ty + 1, 1, currentHeight, 256, 256);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + width - 1, y + i, tx + textureWidth - 1, ty + 1, 1, currentHeight, 256, 256);
                i += currentHeight;
            }

            // Center
            i = 1;
            while (i < width - 1) {
                int currentWidth = Math.max(1, Math.min(width - i, textureWidth - 2) - 1);
                j = 1;
                while (j < height - 1) {
                    int currentHeight = Math.max(1, Math.min(height - j, textureHeight - 2) - 1);
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + i, y + j, tx + 1, ty + 1, currentWidth, currentHeight, 256, 256);
                    j += currentHeight;
                }
                i += currentWidth;
            }
        }

        // Draw buttons
        drawScreen(guiGraphics, mouseX, mouseY, partialTicks);

        subGuiHolder.renderBg(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
    }

    @Override
    public void drawGuiContainerForegroundLayer(GuiGraphicsExtractor guiGraphics, int guiLeft, int guiTop, TextureManager textureManager, Font fontRenderer, int mouseX, int mouseY) {
        subGuiHolder.drawGuiContainerForegroundLayer(guiGraphics, guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(CharacterEvent evt) {
        return subGuiHolder.charTyped(evt);
    }

    @Override
    public boolean keyPressed(KeyEvent evt) {
        return subGuiHolder.keyPressed(evt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
        subGuiHolder.mouseClicked(evt, isDoubleClick);
        for (int i = 0; i < this.buttonList.size(); ++i) {
            Button guibutton = this.buttonList.get(i);
            if (guibutton.mouseClicked(evt, isDoubleClick)) {
                guibutton.playDownSound(Minecraft.getInstance().getSoundManager());
                this.actionPerformed(guibutton);
                return true;
            }
        }
        return false;
    }

    protected void actionPerformed(Button guibutton) {

    }

    public static enum Box {

        LIGHT(0, 0),
        DARK(0, 19);

        private final int x, y;

        private Box(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

    }

    public static class Base extends SubGuiBox {

        private final int x, y, width, height;

        public Base(Box type, int x, int y, int width, int height) {
            super(type);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void init(int guiLeft, int guiTop) {

        }

        @Override
        public void tick() {

        }

        @Override
        public boolean charTyped(CharacterEvent evt) {
            return false;
        }

        @Override
        public boolean keyPressed(KeyEvent evt) {
            return false;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent evt, boolean isDoubleClick) {
            return super.mouseClicked(evt, isDoubleClick);
        }

    }

}
