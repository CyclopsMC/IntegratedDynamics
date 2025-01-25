package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.ScreenInfoBook;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendix;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Aspect appendix.
 * @author rubensworks
 */
public class AspectAppendix extends SectionAppendix {

    private static final int SLOT_SIZE = 16;

    private final IAspect aspect;
    private ItemStack itemStack;

    public AspectAppendix(IInfoBook infoBook, IAspect aspect) {
        super(infoBook);
        this.aspect = aspect;
    }

    @Override
    protected int getOffsetY() {
        return 5;
    }

    @Override
    protected int getWidth() {
        return 100;
    }

    @Override
    protected int getHeight() {
        return 30 + (aspect.hasProperties() ? 10 * (aspect.getPropertyTypes().size() + 1) : 0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawElement(ScreenInfoBook gui, GuiGraphics guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        int yOffset = 5;
        gui.drawOuterBorder(guiGraphics, x - 1, y - 1 - yOffset, getWidth() + 2, getHeight() + 2, 0.5F, 0.5F, 0.5F, 0.4f);
        gui.drawTextBanner(guiGraphics, x + width / 2, y - 2 - yOffset);
        gui.drawScaledCenteredString(guiGraphics, IModHelpers.get().getL10NHelpers().localize("aspect.integrateddynamics.name"), x, y - 2 - yOffset, width, 0.9f, gui.getBannerWidth() - 6, IModHelpers.get().getBaseHelpers().RGBToInt(120, 20, 30));

        Lighting.setupForFlatItems();
        guiGraphics.renderItem(itemStack, x, y);

        // Base information
        String aspectName = IModHelpers.get().getL10NHelpers().localize(aspect.getTranslationKey());
        String valueTypeName = IModHelpers.get().getL10NHelpers().localize(aspect.getValueType().getTranslationKey());
        gui.drawScaledCenteredString(guiGraphics, IModHelpers.get().getL10NHelpers().localize(aspectName), x + 10, y + 8, width, 1f, gui.getBannerWidth() - 10, 0);
        String valueString = IModHelpers.get().getL10NHelpers().localize(aspect.getValueType().getDisplayColorFormat() + valueTypeName);
        //gui.getFont().setBidiFlag(true);
        guiGraphics.drawString(gui.getFont(), IModHelpers.get().getL10NHelpers().localize(aspect instanceof IAspectWrite ? L10NValues.GUI_INPUT : L10NValues.GUI_OUTPUT, valueString), x, y + 16, 0, false);

        // Settings
        if (aspect.hasProperties()) {
            int offsetY = 26;
            guiGraphics.drawString(gui.getFont(), ChatFormatting.DARK_GRAY + IModHelpers.get().getL10NHelpers().localize("gui.integrateddynamics.part.properties"), x, y + offsetY, 0, false);
            for (IAspectPropertyTypeInstance property : ((IAspect<?, ?>) aspect).getPropertyTypes()) {
                offsetY += 10;
                guiGraphics.drawString(gui.getFont(), ChatFormatting.DARK_GRAY + IModHelpers.get().getL10NHelpers().localize(property.getTranslationKey()), x + 10, y + offsetY, 0, false);
            }
        }
        //gui.getFont().setBidiFlag(wasUnicode);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void postDrawElement(ScreenInfoBook gui, GuiGraphics guiGraphics, int x, int y, int width, int height, int page, int mx, int my) {
        if(mx >= x && my >= y && mx <= x + SLOT_SIZE && my <= y + SLOT_SIZE ) {
            List<Component> lines = Lists.newArrayList();
            aspect.loadTooltip(lines, true);
            guiGraphics.renderComponentTooltip(gui.getFont(), lines, mx, my);
        }

        //GlStateManager._disableLighting();

        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void preBakeElement(InfoSection infoSection) {

    }

    @Override
    public void bakeElement(InfoSection infoSection) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        itemStack = registry.writeVariableFacadeItem(new ItemStack(RegistryEntries.ITEM_VARIABLE),
                new AspectVariableFacade(false, 0, aspect), Aspects.REGISTRY);
    }

}
