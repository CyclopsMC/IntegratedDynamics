package org.cyclops.integrateddynamics.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;

import java.util.Optional;

/**
 * Config for the Menril Sapling.
 * @author rubensworks
 *
 */
public class BlockMenrilSaplingConfig extends BlockConfig {

    public static final ResourceKey<ConfiguredFeature<?, ?>> MENTRIL_TREE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "tree_menril"));
    public static final TreeGrower MENRIL_TREE_GROWER = new TreeGrower(
            Reference.MOD_ID + ":menril_sapling",
            Optional.empty(), // Mega tree
            Optional.of(MENTRIL_TREE),
            Optional.empty() // Flowers
    );

    public BlockMenrilSaplingConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_sapling",
                eConfig -> new SaplingBlock(MENRIL_TREE_GROWER, Block.Properties.of()
                        .noCollission()
                        .randomTicks()
                        .strength(0)
                        .sound(SoundType.GRASS)),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        ComposterBlock.COMPOSTABLES.put(getItemInstance(), 0.3F);
    }
}
