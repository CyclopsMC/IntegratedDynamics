package org.cyclops.integrateddynamics.core.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.cyclopscore.client.gui.component.button.ButtonImage;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.Helpers;
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

    public ContainerScreenMultipart(C container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    public void init() {
        buttons.clear();
        super.init();
        P partType = getContainer().getPartType();
        if(partType instanceof PartTypeConfigurable && partType.getContainerProviderSettings(null).isPresent()) {
            addButton(new ButtonImage(this.guiLeft + 174, this.guiTop + 4, 15, 15,
                    new TranslationTextComponent("gui.integrateddynamics.part_settings"),
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
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        // Draw part name
        // MCP: drawString
        font.func_243246_a(matrixStack, getTitle(), guiLeft + 8, guiTop + 6, Helpers.RGBToInt(0, 0, 0));
    }

}
