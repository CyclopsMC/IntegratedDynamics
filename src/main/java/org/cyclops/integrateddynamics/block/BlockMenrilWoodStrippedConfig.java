package org.cyclops.integrateddynamics.block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.world.BlockEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockHelpers;
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
                eConfig -> Blocks.log(MaterialColor.COLOR_CYAN, MaterialColor.COLOR_CYAN),
                getDefaultItemConstructor(IntegratedDynamics._instance)
        );
        MinecraftForge.EVENT_BUS.addListener(BlockMenrilWoodStrippedConfig::toolActionEvent);
    }

    @Override
    public void onRegistered() {

    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        BlockHelpers.setFireInfo(getInstance(), 5, 20);
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
