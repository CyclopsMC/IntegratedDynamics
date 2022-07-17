package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Stripped Menril Wood.
 * @author rubensworks
 *
 */
public class BlockMenrilWoodStrippedConfig extends BlockConfig {

    public BlockMenrilWoodStrippedConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_wood_stripped",
                eConfig -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.WOOD,
                                (blockState) -> MaterialColor.COLOR_CYAN)
                        .strength(2.0F).sound(SoundType.WOOD)) {
                    @Override
                    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                        return 5;
                    }

                    @Override
                    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                        return 20;
                    }
                },
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
        MinecraftForge.EVENT_BUS.addListener(BlockMenrilWoodStrippedConfig::toolActionEvent);
    }

    @Override
    public void onRegistered() {

    }

    public static void toolActionEvent(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() == ToolActions.AXE_STRIP && event.getState().getBlock() == RegistryEntries.BLOCK_MENRIL_WOOD) {
            BlockState blockStateNew = RegistryEntries.BLOCK_MENRIL_WOOD_STRIPPED.defaultBlockState();
            for (Property property : event.getState().getProperties()) {
                if(blockStateNew.hasProperty(property))
                    blockStateNew = blockStateNew.setValue(property, event.getState().getValue(property));
            }
            event.setFinalState(blockStateNew);
        }
    }

}
