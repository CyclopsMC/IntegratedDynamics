package org.cyclops.integrateddynamics.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import org.cyclops.integrateddynamics.world.gen.WorldGeneratorMenrilTree;

import java.util.Random;

/**
 * Decorator for the Meneglin biome.
 * @author rubensworks
 */
public class MeneglinBiomeDecorator extends BiomeDecorator {

    public static final WorldGeneratorMenrilTree MENRIL_TREE_GEN = new WorldGeneratorMenrilTree(false);

    @Override
    protected void genDecorations(BiomeGenBase biomeGenBaseIn, World worldIn, Random random) {
        super.genDecorations(biomeGenBaseIn, worldIn, random);
        int k1 = this.treesPerChunk / 3;
        if (random.nextInt(10) == 0) {
            ++k1;
        }

        if(net.minecraftforge.event.terraingen.TerrainGen.decorate(worldIn, random, chunkPos, net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.TREE)) {
            for (int j2 = 0; j2 < k1; ++j2) {
                int k6 = random.nextInt(16) + 8;
                int l = random.nextInt(16) + 8;
                MENRIL_TREE_GEN.setDecorationDefaults();
                BlockPos blockpos = worldIn.getHeight(chunkPos.add(k6, 0, l));
                MENRIL_TREE_GEN.growTree(worldIn, random, blockpos);
            }
        }
    }
}
