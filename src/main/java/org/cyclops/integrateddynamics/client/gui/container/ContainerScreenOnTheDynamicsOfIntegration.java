package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.infobook.OnTheDynamicsOfIntegrationBook;
import org.cyclops.integrateddynamics.inventory.container.ContainerOnTheDynamicsOfIntegration;

/**
 * Gui for the On the Dynamics of Integration book.
 * @author rubensworks
 */
public class ContainerScreenOnTheDynamicsOfIntegration extends ScreenInfoBook<ContainerOnTheDynamicsOfIntegration> {

    public ContainerScreenOnTheDynamicsOfIntegration(ContainerOnTheDynamicsOfIntegration container, Inventory playerInventory, Component title) {
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
    public void playPageFlipSound(SoundManager soundHandler) {
        soundHandler.play(SimpleSoundInstance.forUI(RegistryEntries.SOUNDEVENT_EFFECT_PAGE_FLIPSINGLE, 1.0F));
    }

    @Override
    public void playPagesFlipSound(SoundManager soundHandler) {
        soundHandler.play(SimpleSoundInstance.forUI(RegistryEntries.SOUNDEVENT_EFFECT_PAGE_FLIPMULTIPLE, 1.0F));
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID,
                Reference.TEXTURE_PATH_GUI + "on_the_dynamics_of_integration_gui.png");
    }
}
