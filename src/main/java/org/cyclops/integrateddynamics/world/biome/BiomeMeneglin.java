package org.cyclops.integrateddynamics.world.biome;

import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBiome;
import org.cyclops.cyclopscore.config.extendedconfig.BiomeConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.Helpers;

import java.util.Random;

/**
 * Meneglin biome.
 * @author rubensworks
 *
 */
public class BiomeMeneglin extends ConfigurableBiome {

    private static final BlockFlower.EnumFlowerType[] FLOWERS = new BlockFlower.EnumFlowerType[]{
            BlockFlower.EnumFlowerType.BLUE_ORCHID,
            BlockFlower.EnumFlowerType.OXEYE_DAISY,
            BlockFlower.EnumFlowerType.WHITE_TULIP
    };
    
    private static BiomeMeneglin _instance = null;
    
    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static BiomeMeneglin getInstance() {
        return _instance;
    }

    public BiomeMeneglin(ExtendedConfig<BiomeConfig> eConfig) {
        super(constructProperties(eConfig.downCast()).setBaseHeight(0.4F)
                .setHeightVariation(0.4F).setTemperature(0.75F)
                .setRainfall(0.25F).setWaterColor(Helpers.RGBToInt(85, 221, 168)), eConfig.downCast());

        this.decorator.treesPerChunk = 3;
        this.decorator.flowersPerChunk = 70;
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        if (!ArrayUtils.contains(BiomeMeneglinConfig.meneglinBiomeDimensionBlacklist, worldIn.provider.getDimension())) {
            super.decorate(worldIn, rand, pos);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getGrassColorAtPos(BlockPos blockPos) {
        return Helpers.RGBToInt(85, 221, 168);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getFoliageColorAtPos(BlockPos blockPos) {
        return Helpers.RGBToInt(128, 208, 185);
    }

    @Override
    public int getSkyColorByTemp(float temp) {
        return Helpers.RGBToInt(178, 238, 233);
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return getModdedBiomeDecorator(new MeneglinBiomeDecorator());
    }

    @Override
    public BlockFlower.EnumFlowerType pickRandomFlower(Random rand, BlockPos pos) {
        return FLOWERS[rand.nextInt(FLOWERS.length)];
    }

    @Override
    public void addDefaultFlowers() {
        for(BlockFlower.EnumFlowerType flower : FLOWERS) {
            addFlower(Blocks.RED_FLOWER.getDefaultState().withProperty(Blocks.RED_FLOWER.getTypeProperty(), flower), 20);
        }
    }
}
