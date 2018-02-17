package org.cyclops.integrateddynamics.block;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockSapling;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.world.biome.MeneglinBiomeDecorator;
import org.cyclops.integrateddynamics.world.gen.WorldGeneratorMenrilTree;

/**
 * Config for the Menril Sapling.
 * @author rubensworks
 *
 */
public class BlockMenrilSaplingConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockMenrilSaplingConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockMenrilSaplingConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menril_sapling",
                null,
                null
        );
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }

    @Override
    protected ConfigurableBlockSapling initSubInstance() {
        return new ConfigurableBlockSapling(this, Material.PLANTS, new WorldGeneratorMenrilTree(false));
    }
    
    @Override
    public String getOreDictionaryId() {
        return Reference.DICT_SAPLINGTREE;
    }

    @SubscribeEvent
    public void onDecorate(DecorateBiomeEvent.Decorate decorateBiomeEvent) {
        if(decorateBiomeEvent.getType() == DecorateBiomeEvent.Decorate.EventType.TREE) {
            if(!ArrayUtils.contains(GeneralConfig.wildMenrilTreeDimensionBlacklist, decorateBiomeEvent.getWorld().provider.getDimension())
                    && GeneralConfig.wildMenrilTreeChance > 0 && decorateBiomeEvent.getRand().nextInt(GeneralConfig.wildMenrilTreeChance) == 0) {
                int k6 = decorateBiomeEvent.getRand().nextInt(16) + 8;
                int l = decorateBiomeEvent.getRand().nextInt(16) + 8;
                MeneglinBiomeDecorator.MENRIL_TREE_GEN.setDecorationDefaults();
                BlockPos blockpos = decorateBiomeEvent.getWorld().getHeight(decorateBiomeEvent.getPos().add(k6, 0, l));
                MeneglinBiomeDecorator.MENRIL_TREE_GEN.growTree(decorateBiomeEvent.getWorld(), decorateBiomeEvent.getRand(), blockpos);
            }
        }
    }
    
}
