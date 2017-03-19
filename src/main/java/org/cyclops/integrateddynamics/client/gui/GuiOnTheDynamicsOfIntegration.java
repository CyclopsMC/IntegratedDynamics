package org.cyclops.integrateddynamics.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.infobook.GuiInfoBook;
import org.cyclops.integrateddynamics.IntegratedDynamicsSoundEvents;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.infobook.OnTheDynamicsOfIntegrationBook;

/**
 * Gui for the On the Dynamics of Integration book.
 * @author rubensworks
 */
public class GuiOnTheDynamicsOfIntegration extends GuiInfoBook {

    protected static final ResourceLocation texture = new ResourceLocation(Reference.MOD_ID,
            Reference.TEXTURE_PATH_GUI + "on_the_dynamics_of_integration_gui.png");

    public GuiOnTheDynamicsOfIntegration(EntityPlayer player, int itemIndex) {
        super(player, itemIndex, OnTheDynamicsOfIntegrationBook.getInstance(), texture);
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
    public void playPageFlipSound(SoundHandler soundHandler) {
        soundHandler.playSound(PositionedSoundRecord.getMasterRecord(IntegratedDynamicsSoundEvents.effect_page_flipsingle, 1.0F));
    }

    @Override
    public void playPagesFlipSound(SoundHandler soundHandler) {
        soundHandler.playSound(PositionedSoundRecord.getMasterRecord(IntegratedDynamicsSoundEvents.effect_page_flipmultiple, 1.0F));
    }
}
