package org.cyclops.integrateddynamics.blockentity;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.render.blockentity.RenderBlockEntityEnergyBattery;

/**
 * @author rubensworks
 */
public class BlockEntityEnergyBatteryConfigClient {

    public static TextureAtlasSprite ICON_OVERLAY;

    public void onRegistered(BlockEntityEnergyBatteryConfig config) {
        config.getMod().getProxy().registerRenderer(config.getInstance(), RenderBlockEntityEnergyBattery::new);
        IntegratedDynamics._instance.getModEventBus().addListener(this::postTextureStitch);
    }

    public void postTextureStitch(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            ICON_OVERLAY = event.getAtlas().getSprite(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/energy_battery_overlay"));
        }
    }

}
