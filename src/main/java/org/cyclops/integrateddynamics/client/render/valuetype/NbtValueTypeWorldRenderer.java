package org.cyclops.integrateddynamics.client.render.valuetype;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
    public void renderValue(BlockEntityRendererProvider.Context context, IPartContainer partContainer,
                            Direction direction, IPartType partType, IValue value, float partialTicks,
                            PoseStack matrixStack, MultiBufferSource renderTypeBuffer,
                            int combinedLight, int combinedOverlay, float alpha) {
        Font fontRenderer = context.getFont();
        Wrapper<Float> maxWidth = new Wrapper<>(0F);

        List<String> lines = Lists.newLinkedList();
        ((ValueTypeNbt.ValueNbt) value).getRawValue().ifPresent(tag -> {
            if (tag instanceof CompoundTag) {
                CompoundTag tagCompound = (CompoundTag) tag;
                lines.add("{");
                for (String key : tagCompound.getAllKeys()) {
                    if (lines.size() >= MAX_LINES) {
                        lines.add("...");
                        break;
                    } else {
                        Tag subTag = ValueTypes.NBT.filterBlacklistedTags(tagCompound.get(key));
                        String string = "  " + key + ": " + StringUtils.abbreviate(subTag.toString(), 40) + "";
                        float width = fontRenderer.width(string) - 1;
                        lines.add(string);
                        maxWidth.set(Math.max(maxWidth.get(), width));
                    }
                }
                lines.add("}");
            } else {
                String string = tag.toString();
                lines.add(string);
                maxWidth.set((float) (fontRenderer.width(string) - 1));
            }
        });

        float singleHeight = fontRenderer.lineHeight;
        float totalHeight = singleHeight * lines.size();

        matrixStack.pushPose();

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
            context.getFont().drawInBatch(line, 0, offset, color,
                    false, matrixStack.last().pose(), renderTypeBuffer, Font.DisplayMode.NORMAL, 0, combinedLight);
            offset += singleHeight;
        }

        matrixStack.popPose();
    }
}
