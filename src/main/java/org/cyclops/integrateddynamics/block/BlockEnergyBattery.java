package org.cyclops.integrateddynamics.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.Reference;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockEnergyBattery extends BlockEnergyBatteryBase {

    public TextureAtlasSprite iconOverlay;

    public BlockEnergyBattery(Block.Properties properties) {
        super(properties);
        if(MinecraftHelpers.isClientSide()) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postTextureStitch);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void postTextureStitch(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            iconOverlay = event.getAtlas().getSprite(new ResourceLocation(Reference.MOD_ID, "block/energy_battery_overlay"));
        }
    }

    public boolean isCreative() {
        return false;
    }

}
