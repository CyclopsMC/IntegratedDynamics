package org.cyclops.integrateddynamics.world.biome;

import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import org.cyclops.integrateddynamics.world.gen.WorldGeneratorMenrilTree;

/**
 * Decorator for the Meneglin biome.
 * @author rubensworks
 */
public class MeneglinBiomeDecorator extends BiomeDecorator {

    public static final WorldGeneratorMenrilTree MENRIL_TREE_GEN = new WorldGeneratorMenrilTree(false);

    @Override
    protected void genDecorations(BiomeGenBase biomeGenBaseIn) {
        super.genDecorations(biomeGenBaseIn);
        int k1 = this.treesPerChunk / 3;
        if (this.randomGenerator.nextInt(10) == 0) {
            ++k1;
        }

        if(net.minecraftforge.event.terraingen.TerrainGen.decorate(currentWorld, randomGenerator, field_180294_c, net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.TREE)) {
            for (int j2 = 0; j2 < k1; ++j2) {
                int k6 = this.randomGenerator.nextInt(16) + 8;
                int l = this.randomGenerator.nextInt(16) + 8;
                MENRIL_TREE_GEN.func_175904_e();
                BlockPos blockpos = this.currentWorld.getHeight(this.field_180294_c.add(k6, 0, l));
                MENRIL_TREE_GEN.growTree(this.currentWorld, this.randomGenerator, blockpos);
            }
        }
    }
}
