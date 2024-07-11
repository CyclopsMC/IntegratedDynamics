package org.cyclops.integrateddynamics.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockEnergyBattery extends BlockEnergyBatteryBase {

    public static final MapCodec<BlockEnergyBattery> CODEC = simpleCodec(BlockEnergyBattery::new);

    public TextureAtlasSprite iconOverlay;

    public BlockEnergyBattery(Block.Properties properties) {
        super(properties);
        if(MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getModEventBus().addListener(this::postTextureStitch);
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    public void postTextureStitch(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            iconOverlay = event.getAtlas().getSprite(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/energy_battery_overlay"));
        }
    }

    public boolean isCreative() {
        return false;
    }

}
