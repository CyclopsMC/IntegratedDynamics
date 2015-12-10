package org.cyclops.integrateddynamics.core.client.gui.container;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.gui.FontRenderer;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonImage;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerExtended;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;
import org.cyclops.integrateddynamics.core.part.PartTypeConfigurable;

import java.awt.*;

/**
 * Gui for parts.
 * @author rubensworks
 */
@EqualsAndHashCode(callSuper = false)
@Data
public abstract class GuiMultipart<P extends IPartType<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends GuiContainerExtended {

    private static final Rectangle ITEM_POSITION = new Rectangle(8, 17, 18, 18);

    protected final DisplayErrorsComponent displayErrors = new DisplayErrorsComponent();
    private final PartTarget target;
    private final IPartContainer partContainer;
    private final P partType;

    /**
     * Make a new instance.
     * @param container The container to make the GUI for.
     */
    public GuiMultipart(ContainerMultipart<P, S> container) {
        super(container);
        this.target = container.getTarget();
        this.partContainer = container.getPartContainer();
        this.partType = container.getPartType();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        super.initGui();
        if(getPartType() instanceof PartTypeConfigurable && ((PartTypeConfigurable) getPartType()).hasSettings()) {
            buttonList.add(new GuiButtonImage(ContainerMultipartAspects.BUTTON_SETTINGS, this.guiLeft + 174, this.guiTop + 4, 15, 15, Images.CONFIG_BOARD, -2, -3, true));
        }
    }

    @SuppressWarnings("unchecked")
    public S getPartState() {
        return ((ContainerMultipart<P, S>) container).getPartState();
    }

    protected abstract String getNameId();

    @Override
    public String getGuiTexture() {
        return getContainer().getGuiProvider().getMod().getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_GUI)
               + getNameId() + ".png";
    }

    protected float colorSmoothener(float color) {
        return 1F - ((1F - color) / 4F);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        FontRenderer fontRenderer = fontRendererObj;

        // Draw part name
        fontRenderer.drawString(L10NHelpers.localize(getPartType().getUnlocalizedName()), guiLeft + 8, guiTop + 6, Helpers.RGBToInt(0, 0, 0));
    }

}
