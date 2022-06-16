package org.cyclops.integrateddynamics.core.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.PartTypeConfigurable;

import java.awt.*;

/**
 * Gui for parts.
 * @author rubensworks
 */
public abstract class ContainerScreenMultipart<P extends IPartType<P, S>, S extends IPartState<P>, C extends ContainerMultipart<P, S>>
        extends ContainerScreenExtended<C> {

    private static final Rectangle ITEM_POSITION = new Rectangle(8, 17, 18, 18);

    protected final DisplayErrorsComponent displayErrors = new DisplayErrorsComponent();

    public ContainerScreenMultipart(C container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        clearWidgets();
        super.init();
        P partType = getMenu().getPartType();
        if(partType instanceof PartTypeConfigurable && partType.getContainerProviderSettings(null).isPresent()) {
            addRenderableWidget(new ButtonImage(this.leftPos + 174, this.topPos + 4, 15, 15,
                    Component.translatable("gui.integrateddynamics.part_settings"),
                    createServerPressable(ContainerMultipart.BUTTON_SETTINGS, (button) -> {}), true,
                    Images.CONFIG_BOARD, -2, -3));
        }
    }

    protected abstract String getNameId();

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/" + getNameId() + ".png");
    }

    protected float colorSmoothener(float color) {
        return 1F - ((1F - color) / 4F);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

        // Draw part name
        // MCP: drawString
        font.draw(matrixStack, getTitle(), leftPos + 8, topPos + 6, 4210752);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {
        // super.drawGuiContainerForegroundLayer(matrixStack, x, y);
    }
}
