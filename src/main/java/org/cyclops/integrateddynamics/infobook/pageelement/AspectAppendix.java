package org.cyclops.integrateddynamics.infobook.pageelement;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
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
    protected void drawElement(ScreenInfoBook gui, PoseStack matrixStack, int x, int y, int width, int height, int page, int mx, int my) {
        int yOffset = 5;
        gui.drawOuterBorder(matrixStack, x - 1, y - 1 - yOffset, getWidth() + 2, getHeight() + 2, 0.5F, 0.5F, 0.5F, 0.4f);
        gui.drawTextBanner(matrixStack, x + width / 2, y - 2 - yOffset);
        gui.drawScaledCenteredString(matrixStack, L10NHelpers.localize("aspect.integrateddynamics.name"), x, y - 2 - yOffset, width, 0.9f, gui.getBannerWidth() - 6, Helpers.RGBToInt(120, 20, 30));

        Lighting.setupForFlatItems();
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(matrixStack, itemStack, x, y);

        // Base information
        String aspectName = L10NHelpers.localize(aspect.getTranslationKey());
        String valueTypeName = L10NHelpers.localize(aspect.getValueType().getTranslationKey());
        gui.drawScaledCenteredString(matrixStack, L10NHelpers.localize(aspectName), x + 10, y + 8, width, 1f, gui.getBannerWidth() - 10, 0);
        String valueString = L10NHelpers.localize(aspect.getValueType().getDisplayColorFormat() + valueTypeName);
        //gui.getFont().setBidiFlag(true);
        gui.getFont().draw(matrixStack, L10NHelpers.localize(aspect instanceof IAspectWrite ? L10NValues.GUI_INPUT : L10NValues.GUI_OUTPUT, valueString), x, y + 16, 0);

        // Settings
        if (aspect.hasProperties()) {
            int offsetY = 26;
            gui.getFont().draw(matrixStack, ChatFormatting.DARK_GRAY + L10NHelpers.localize("gui.integrateddynamics.part.properties"), x, y + offsetY, 0);
            for (IAspectPropertyTypeInstance property : ((IAspect<?, ?>) aspect).getPropertyTypes()) {
                offsetY += 10;
                gui.getFont().draw(matrixStack, ChatFormatting.DARK_GRAY + L10NHelpers.localize(property.getTranslationKey()), x + 10, y + offsetY, 0);
            }
        }
        //gui.getFont().setBidiFlag(wasUnicode);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void postDrawElement(ScreenInfoBook gui, PoseStack matrixStack, int x, int y, int width, int height, int page, int mx, int my) {
        matrixStack.pushPose();
        if(mx >= x && my >= y && mx <= x + SLOT_SIZE && my <= y + SLOT_SIZE ) {
            List<Component> lines = Lists.newArrayList();
            aspect.loadTooltip(lines, true);
            gui.renderComponentTooltip(matrixStack, lines, mx, my);
        }
        matrixStack.popPose();

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
