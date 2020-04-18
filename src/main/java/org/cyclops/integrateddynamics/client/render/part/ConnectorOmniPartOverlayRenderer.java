package org.cyclops.integrateddynamics.client.render.part;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.particle.ParticleBlur;
import org.cyclops.cyclopscore.client.particle.ParticleBlurData;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;

import java.util.Random;

/**
 * Overlay renderer for the omni-directional connector for rendering particle effects.
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ConnectorOmniPartOverlayRenderer extends PartOverlayRendererBase {

    private static final Int2IntMap CACHED_GROUP_COLORS = new Int2IntOpenHashMap();

    private final Random rand = new Random();

    protected static int getGroupColor(int group) {
        if (!CACHED_GROUP_COLORS.containsKey(group)) {
            Random rand = new Random(group);
            int color = rand.nextInt(1 << 23) | (255 << 24);
            CACHED_GROUP_COLORS.put(group, color);
            return color;
        }
        return CACHED_GROUP_COLORS.get(group);
    }

    @Override
    public void renderPartOverlay(TileEntityRendererDispatcher rendererDispatcher, IPartContainer partContainer,
                                  Direction direction, IPartType partType, float partialTicks, MatrixStack matrixStack,
                                  IRenderTypeBuffer renderTypeBuffer, int combinedLight, int combinedOverlay) {
        BlockPos pos = partContainer.getPosition().getBlockPos();
        if(!shouldRender(pos)) return;

        if (rand.nextInt(20) == 0 && !Minecraft.getInstance().isGamePaused()) {
            IPartState partStateUnsafe = partContainer.getPartState(direction);
            if (partStateUnsafe instanceof PartTypeConnectorOmniDirectional.State) {
                PartTypeConnectorOmniDirectional.State partState = (PartTypeConnectorOmniDirectional.State) partStateUnsafe;
                if (partState.hasConnectorId()) {
                    double tx = pos.getX() + 0.5F + direction.getXOffset() * 1.15F - 0.03F + rand.nextFloat() * 0.04F
                            + (direction.getAxis() != Direction.Axis.X ? 0.25F - rand.nextFloat() * 0.5F : 0F);
                    double ty = pos.getY() + 0.5F + direction.getYOffset() * 1.15F - 0.03F + rand.nextFloat() * 0.04F
                            + (direction.getAxis() != Direction.Axis.Y ? 0.25F - rand.nextFloat() * 0.5F : 0F);
                    double tz = pos.getZ() + 0.5F + direction.getZOffset() * 1.15F - 0.03F + rand.nextFloat() * 0.04F
                            + (direction.getAxis() != Direction.Axis.Z ? 0.25F - rand.nextFloat() * 0.5F : 0F);

                    float scale = 0.15F;
                    Triple<Float, Float, Float> colors = Helpers.intToRGB(getGroupColor(partState.getGroupId()));
                    float red = colors.getLeft() + rand.nextFloat() * 0.1F - 0.05F;
                    float green = colors.getMiddle() + rand.nextFloat() * 0.1F - 0.05F;
                    float blue = colors.getRight() + rand.nextFloat() * 0.1F - 0.05F;
                    float ageMultiplier = 17F;

                    ParticleBlur blur = new ParticleBlur(new ParticleBlurData(red, green, blue, scale, ageMultiplier),
                            Minecraft.getInstance().world, tx, ty, tz,
                            -(direction.getXOffset() * 0.05F + rand.nextFloat() * 0.02F - 0.01F),
                            -(direction.getYOffset() * 0.05F + rand.nextFloat() * 0.02F - 0.01F),
                            -(direction.getZOffset() * 0.05F + rand.nextFloat() * 0.02F - 0.01F));
                    Minecraft.getInstance().particles.addEffect(blur);
                }
            }
        }
    }
}
