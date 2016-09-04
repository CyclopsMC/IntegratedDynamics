package org.cyclops.integrateddynamics.core.network.diagnostics;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author rubensworks
 */
public class NetworkDiagnosticsPartOverlayRenderer {

    private static final NetworkDiagnosticsPartOverlayRenderer _INSTANCE = new NetworkDiagnosticsPartOverlayRenderer();
    private final Set<PartPos> partPositions = Sets.newHashSet();

    private NetworkDiagnosticsPartOverlayRenderer() {

    }

    public static NetworkDiagnosticsPartOverlayRenderer getInstance() {
        return _INSTANCE;
    }

    public synchronized void addPos(PartPos pos) {
        partPositions.add(pos);
    }

    public synchronized void removePos(PartPos pos) {
        partPositions.remove(pos);
    }

    public synchronized void clearPositions() {
        partPositions.clear();
    }

    public synchronized boolean hasPartPos(PartPos pos) {
        return partPositions.contains(pos);
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        float partialTicks = event.getPartialTicks();

        double offsetX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
        double offsetY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
        double offsetZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(6.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        List<PartPos> partList = Lists.newArrayList(partPositions);
        for (Iterator<PartPos> it = partList.iterator(); it.hasNext();) {
            PartPos partPos = it.next();
            if (partPos.getPos().getWorld() == player.worldObj && partPos.getPos().getBlockPos().distanceSq(player.getPosition()) < 10000) {
                PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
                if (partStateHolder != null) {
                    AxisAlignedBB bb = partStateHolder.getPart().getRenderPosition().getBoundingBox(partPos.getSide())
                            .offset(partPos.getPos().getBlockPos())
                            .offset(-offsetX, -offsetY, -offsetZ)
                            .expand(0.05, 0.05, 0.05);
                    RenderGlobal.func_189697_a(bb, 1.0F, 0.2F, 0.1F, 0.8F);
                } else {
                    it.remove();
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

}
