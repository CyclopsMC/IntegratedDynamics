package org.cyclops.integrateddynamics.client.render.part;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.client.particle.ParticleBlur;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;

import java.util.Random;

/**
 * Overlay renderer for the omni-directional connector for rendering particle effects.
 * @author rubensworks
 */
@SideOnly(Side.CLIENT)
public class ConnectorOmniPartOverlayRenderer extends PartOverlayRendererBase {

    private static final TIntIntMap CACHED_GROUP_COLORS = new TIntIntHashMap();

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
    public void renderPartOverlay(IPartContainer partContainer, double x, double y, double z, float partialTick,
                                  int destroyStage, EnumFacing direction, IPartType partType,
                                  TileEntityRendererDispatcher rendererDispatcher) {
        BlockPos pos = partContainer.getPosition().getBlockPos();
        if(!shouldRender(pos)) return;

        if (rand.nextInt(20) == 0 && !Minecraft.getMinecraft().isGamePaused()) {
            PartTypeConnectorOmniDirectional.State partState = (PartTypeConnectorOmniDirectional.State) partContainer.getPartState(direction);
            if (partState.hasConnectorId()) {
                double tx = pos.getX() + 0.5F + direction.getFrontOffsetX() * 1.15F - 0.03F + rand.nextFloat() * 0.04F
                        + (direction.getAxis() != EnumFacing.Axis.X ? 0.25F - rand.nextFloat() * 0.5F : 0F);
                double ty = pos.getY() + 0.5F + direction.getFrontOffsetY() * 1.15F - 0.03F + rand.nextFloat() * 0.04F
                        + (direction.getAxis() != EnumFacing.Axis.Y ? 0.25F - rand.nextFloat() * 0.5F : 0F);
                double tz = pos.getZ() + 0.5F + direction.getFrontOffsetZ() * 1.15F - 0.03F + rand.nextFloat() * 0.04F
                        + (direction.getAxis() != EnumFacing.Axis.Z ? 0.25F - rand.nextFloat() * 0.5F : 0F);

                float scale = 0.15F;
                Triple<Float, Float, Float> colors = Helpers.intToRGB(getGroupColor(partState.getGroupId()));
                float red = colors.getLeft() + rand.nextFloat() * 0.1F - 0.05F;
                float green = colors.getMiddle() + rand.nextFloat() * 0.1F - 0.05F;
                float blue = colors.getRight() + rand.nextFloat() * 0.1F - 0.05F;
                float ageMultiplier = 17F;

                ParticleBlur blur = new ParticleBlur(Minecraft.getMinecraft().world, tx, ty, tz, scale,
                        -(direction.getFrontOffsetX() * 0.05F + rand.nextFloat() * 0.02F - 0.01F),
                        -(direction.getFrontOffsetY() * 0.05F + rand.nextFloat() * 0.02F - 0.01F),
                        -(direction.getFrontOffsetZ() * 0.05F + rand.nextFloat() * 0.02F - 0.01F),
                        red, green, blue, ageMultiplier);
                Minecraft.getMinecraft().effectRenderer.addEffect(blur);
            }
        }
    }
}
