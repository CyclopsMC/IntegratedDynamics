package org.cyclops.integrateddynamics.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the Stripped Menril Log.
 * @author rubensworks
 *
 */
public class BlockMenrilLogStrippedConfig extends BlockConfig {

    public BlockMenrilLogStrippedConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_log_stripped",
                eConfig -> new RotatedPillarBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_CYAN)
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
        NeoForge.EVENT_BUS.addListener(BlockMenrilLogStrippedConfig::toolActionEvent);
    }

    public static void toolActionEvent(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() == ItemAbilities.AXE_STRIP && event.getState().getBlock() == RegistryEntries.BLOCK_MENRIL_LOG.get()) {
            BlockState blockStateNew = RegistryEntries.BLOCK_MENRIL_LOG_STRIPPED.get().defaultBlockState();
            for (Property property : event.getState().getProperties()) {
                if(blockStateNew.hasProperty(property))
                    blockStateNew = blockStateNew.setValue(property, event.getState().getValue(property));
            }
            event.setFinalState(blockStateNew);
        }
    }

}
