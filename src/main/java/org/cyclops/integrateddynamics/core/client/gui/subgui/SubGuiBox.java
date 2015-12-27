package org.cyclops.integrateddynamics.core.client.gui.subgui;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;

import java.io.IOException;
import java.util.List;

/**
 * A sub gui that simply renders a box.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public abstract class SubGuiBox extends Gui implements ISubGuiBox {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID,
            IntegratedDynamics._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI) + "subGui.png");

    private final Box type;

    protected List<GuiButton> buttonList = Lists.newArrayList();
    protected final SubGuiHolder subGuiHolder = new SubGuiHolder();

    public SubGuiBox(Box type) {
        this.type = type;
    }

    @Override
    public void initGui(int guiLeft, int guiTop) {
        buttonList.clear();
        subGuiHolder.initGui(guiLeft, guiTop);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < this.buttonList.size(); ++i) {
            this.buttonList.get(i).drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
        }
    }

    @Override
    public void drawGuiContainerBackgroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, float partialTicks, int mouseX, int mouseY) {
        textureManager.bindTexture(TEXTURE);
        GlStateManager.color(1, 1, 1);

        int x = guiLeft + getX();
        int y = guiTop + getY();
        int width = getWidth();
        int height = getHeight();
        int tx = type.getX();
        int ty = type.getY();

        // Corners
        this.drawTexturedModalRect(x, y, tx, tx, 1, 1); // top left
        this.drawTexturedModalRect(x + width - 1, y, tx + 2, ty, 1, 1); // top right
        this.drawTexturedModalRect(x, y + height - 1, 0, tx + 2, ty + 1, 1); // bottom left
        this.drawTexturedModalRect(x + width - 1, y + height - 1, tx + 2, ty + 2, 1, 1); // bottom right

        // Sides
        for(int i = 1; i < width - 1; i += 1) {
            this.drawTexturedModalRect(x + i, y, tx + 1, ty, 1, 1);
            this.drawTexturedModalRect(x + i, y + height - 1, tx + 1, ty + 2, 1, 1);
        }
        for(int i = 1; i < height - 1; i += 1) {
            this.drawTexturedModalRect(x, y + i, tx, ty + 1, 1, 1);
            this.drawTexturedModalRect(x + width - 1, y + i, tx + 2, ty + 1, 1, 1);
        }

        // Center
        for(int i = 1; i < width - 1; i += 1) {
            for(int j = 1; j < height - 1; j += 1) {
                this.drawTexturedModalRect(x + i, y + j, tx + 1, ty + 1, 1, 1);
            }
        }

        // Draw buttons
        drawScreen(mouseX, mouseY, partialTicks);

        subGuiHolder.drawGuiContainerBackgroundLayer(guiLeft, guiTop, textureManager, fontRenderer, partialTicks, mouseX, mouseY);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int guiLeft, int guiTop, TextureManager textureManager, FontRenderer fontRenderer, int mouseX, int mouseY) {
        subGuiHolder.drawGuiContainerForegroundLayer(guiLeft, guiTop, textureManager, fontRenderer, mouseX, mouseY);
    }

    @Override
    public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException {
        return subGuiHolder.keyTyped(checkHotbarKeys, typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        subGuiHolder.mouseClicked(mouseX, mouseY, mouseButton);
        for (int i = 0; i < this.buttonList.size(); ++i) {
            GuiButton guibutton = this.buttonList.get(i);
            if (guibutton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
                guibutton.playPressSound(Minecraft.getMinecraft().getSoundHandler());
                this.actionPerformed(guibutton);
            }
        }
    }

    protected void actionPerformed(GuiButton guibutton) {

    }

    public static enum Box {

        LIGHT(0, 0),
        DARK(0, 3);

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

    @EqualsAndHashCode(callSuper = false)
    @Data
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
        public void initGui(int guiLeft, int guiTop) {

        }

        @Override
        public boolean keyTyped(boolean checkHotbarKeys, char typedChar, int keyCode) throws IOException {
            return false;
        }

        @Override
        public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        }

    }

}
