package org.cyclops.integrateddynamics.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;


/**
 * Config for the Menril Sapling.
 * @author rubensworks
 *
 */
public class BlockMenrilSaplingConfig extends BlockConfigCommon<IntegratedDynamics> {

    public static final ResourceKey<Feature> MENTRIL_TREE = ResourceKey
            .create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "tree_menril"));
    public static final TreeGrower MENRIL_TREE_GROWER = new TreeGrower(
            Reference.MOD_ID + ":menril_sapling",
            WeightedList.of(MENTRIL_TREE),
            WeightedList.of(), // Mega trees
            WeightedList.of(), // Flower trees
            MENTRIL_TREE // Shortest tree, for the sapling's growth height check
    );

    public BlockMenrilSaplingConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_sapling",
                (eConfig, properties) -> new SaplingBlock(MENRIL_TREE_GROWER, properties
                        .noCollision()
                        .randomTicks()
                        .strength(0)
                        .sound(SoundType.GRASS)),
                getDefaultItemConstructor(IntegratedDynamics._instance,
                        properties -> properties.compostable(ContextIntProviders.COMPOSTABLE_LOW))
        );
    }

}
