package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.StringUtils;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * A text-based value type world renderer for NBT tags.
 * @author rubensworks
 */
public class NbtValueTypeWorldRenderer implements IValueTypeWorldRenderer {

    private static final int MAX_LINES = 30;
    private static final float MAX = 12.5F;
    private static final float MARGIN_FACTOR = 1.1F;

    @Override
    public void renderValue(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        FontRenderer fontRenderer = rendererDispatcher.getFontRenderer();
        Wrapper<Float> maxWidth = new Wrapper<>(0F);

        List<String> lines = Lists.newLinkedList();
        ((ValueTypeNbt.ValueNbt) value).getRawValue().ifPresent(tag -> {
            if (tag instanceof CompoundNBT) {
                CompoundNBT tagCompound = (CompoundNBT) tag;
                lines.add("{");
                for (String key : tagCompound.keySet()) {
                    if (lines.size() >= MAX_LINES) {
                        lines.add("...");
                        break;
                    } else {
                        INBT subTag = ValueTypes.NBT.filterBlacklistedTags(tagCompound.get(key));
                        String string = "  " + key + ": " + StringUtils.abbreviate(subTag.toString(), 40) + "";
                        float width = fontRenderer.getStringWidth(string) - 1;
                        lines.add(string);
                        maxWidth.set(Math.max(maxWidth.get(), width));
                    }
                }
                lines.add("}");
            } else {
                String string = tag.toString();
                lines.add(string);
                maxWidth.set((float) (fontRenderer.getStringWidth(string) - 1));
            }
        });

        float singleHeight = fontRenderer.FONT_HEIGHT;
        float totalHeight = singleHeight * lines.size();

        matrixStack.push();

        float scaleX = MAX / (maxWidth.get() * MARGIN_FACTOR);
        float scaleY = MAX / (totalHeight * MARGIN_FACTOR);
        float scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
        float newWidth = maxWidth.get() * scale;
        float newHeight = totalHeight * scale;
        matrixStack.translate((MAX - newWidth) / 2, (MAX - newHeight) / 2, 0F);
        matrixStack.scale(scale, scale, 1F);

        int offset = 0;
        for(String line : lines) {
            int color = Helpers.addAlphaToColor(ValueTypes.NBT.getDisplayColor(), alpha);
            rendererDispatcher.getFontRenderer().renderString(line, 0, offset, color,
                    false, matrixStack.getLast().getMatrix(), renderTypeBuffer, false, 0, combinedLight);
            offset += singleHeight;
        }

        matrixStack.pop();
    }
}
