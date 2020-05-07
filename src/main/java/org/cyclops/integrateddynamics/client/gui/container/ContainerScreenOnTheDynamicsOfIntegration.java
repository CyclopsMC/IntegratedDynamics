package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.integrateddynamics.IntegratedDynamicsSoundEvents;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.infobook.OnTheDynamicsOfIntegrationBook;
import org.cyclops.integrateddynamics.inventory.container.ContainerOnTheDynamicsOfIntegration;

/**
 * Gui for the On the Dynamics of Integration book.
 * @author rubensworks
 */
public class ContainerScreenOnTheDynamicsOfIntegration extends ScreenInfoBook<ContainerOnTheDynamicsOfIntegration> {

    public ContainerScreenOnTheDynamicsOfIntegration(ContainerOnTheDynamicsOfIntegration container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title, OnTheDynamicsOfIntegrationBook.getInstance());
    }

    @Override
    protected int getGuiWidth() {
        return 283;
    }

    @Override
    protected int getGuiHeight() {
        return 180;
    }

    @Override
    protected int getPageWidth() {
        return 142;
    }

    @Override
    protected int getPageYOffset() {
        return 9;
    }

    @Override
    protected int getFootnoteOffsetX() {
        return -2;
    }

    @Override
    protected int getFootnoteOffsetY() {
        return -8;
    }

    @Override
    protected int getPrevNextOffsetY() {
        return 7;
    }

    @Override
    protected int getPrevNextOffsetX() {
        return 16;
    }

    @Override
    protected int getOffsetXForPageBase(int page) {
        return page == 0 ? 20 : 10;
    }

    @Override
    public int getTitleColor() {
        return Helpers.RGBToInt(70, 70, 150);
    }

    @Override
    public void playPageFlipSound(SoundHandler soundHandler) {
        soundHandler.play(SimpleSound.master(IntegratedDynamicsSoundEvents.effect_page_flipsingle, 1.0F));
    }

    @Override
    public void playPagesFlipSound(SoundHandler soundHandler) {
        soundHandler.play(SimpleSound.master(IntegratedDynamicsSoundEvents.effect_page_flipmultiple, 1.0F));
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation(Reference.MOD_ID,
                Reference.TEXTURE_PATH_GUI + "on_the_dynamics_of_integration_gui.png");
    }
}
