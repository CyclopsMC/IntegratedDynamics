package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendixClient;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.List;

/**
 * @author rubensworks
 */
public class AspectAppendixClient extends SectionAppendixClient<AspectAppendix> {

    private static final int SLOT_SIZE = 16;

    protected AspectAppendixClient(AspectAppendix sectionAppendix) {
        super(sectionAppendix);
    }

    @Override
    protected void drawElement(ScreenInfoBook gui, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        AspectAppendix section = getSectionAppendix();
        IAspect aspect = section.getAspect();

        int yOffset = 5;
        gui.drawOuterBorder(guiGraphics, x - 1, y - 1 - yOffset, section.getWidth() + 2, section.getHeight() + 2, 0.5F, 0.5F, 0.5F, 0.4f);
        gui.drawTextBanner(guiGraphics, x + width / 2, y - 2 - yOffset);
        gui.drawScaledCenteredString(guiGraphics, IModHelpers.get().getL10NHelpers().localize("aspect.integrateddynamics.name"), x, y - 2 - yOffset, width, 0.9f, gui.getBannerWidth() - 6, IModHelpers.get().getBaseHelpers().RGBAToInt(30, 20, 120, 255));

        guiGraphics.item(section.getItemStack(), x, y);

        // Base information
        String aspectName = IModHelpers.get().getL10NHelpers().localize(aspect.getTranslationKey());
        String valueTypeName = IModHelpers.get().getL10NHelpers().localize(aspect.getValueType().getTranslationKey());
        gui.drawScaledCenteredString(guiGraphics, IModHelpers.get().getL10NHelpers().localize(aspectName), x + 10, y + 8, width, 1f, gui.getBannerWidth() - 10, ARGB.opaque(0));
        String valueString = IModHelpers.get().getL10NHelpers().localize(aspect.getValueType().getDisplayColorFormat() + valueTypeName);
        //gui.getFont().setBidiFlag(true);
        guiGraphics.text(gui.getFont(), IModHelpers.get().getL10NHelpers().localize(aspect instanceof IAspectWrite ? L10NValues.GUI_INPUT : L10NValues.GUI_OUTPUT, valueString), x, y + 16, ARGB.opaque(0), false);

        // Settings
        if (aspect.hasProperties()) {
            int offsetY = 26;
            guiGraphics.text(gui.getFont(), ChatFormatting.DARK_GRAY + IModHelpers.get().getL10NHelpers().localize("gui.integrateddynamics.part.properties"), x, y + offsetY, ARGB.opaque(0), false);
            for (IAspectPropertyTypeInstance property : ((IAspect<?, ?>) aspect).getPropertyTypes()) {
                offsetY += 10;
                guiGraphics.text(gui.getFont(), ChatFormatting.DARK_GRAY + IModHelpers.get().getL10NHelpers().localize(property.getTranslationKey()), x + 10, y + offsetY, ARGB.opaque(0), false);
            }
        }
        //gui.getFont().setBidiFlag(wasUnicode);
    }

    @Override
    protected void postDrawElement(ScreenInfoBook gui, GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        if(mx >= x && my >= y && mx <= x + SLOT_SIZE && my <= y + SLOT_SIZE ) {
            List<Component> lines = Lists.newArrayList();
            getSectionAppendix().getAspect().loadTooltip(lines::add, true);
            guiGraphics.setComponentTooltipForNextFrame(gui.getFont(), lines, mx, my);
        }
    }
}
