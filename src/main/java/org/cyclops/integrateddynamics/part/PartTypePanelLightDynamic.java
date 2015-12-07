package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.block.BlockInvisibleLight;
import org.cyclops.integrateddynamics.block.BlockInvisibleLightConfig;
import org.cyclops.integrateddynamics.core.block.IDynamicLightBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.core.evaluate.variable.IValueTypeLightLevelRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLightLevels;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;

/**
 * A part that can display variables.
 * @author rubensworks
 */
public class PartTypePanelLightDynamic extends PartTypePanelVariableDriven<PartTypePanelLightDynamic, PartTypePanelLightDynamic.State> {

    public PartTypePanelLightDynamic(String name) {
        super(name);
    }

    @Override
    public Class<? super PartTypePanelLightDynamic> getPartTypeClass() {
        return PartTypePanelLightDynamic.class;
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus(blockConfig);
    }

    @Override
    public PartTypePanelLightDynamic.State constructDefaultState() {
        return new PartTypePanelLightDynamic.State();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onValueChanged(Network network, PartTarget target, State state, IValue lastValue, IValue newValue) {
        super.onValueChanged(network, target, state, lastValue, newValue);
        if(newValue != null) {
            IValueTypeLightLevelRegistry.ILightLevelCalculator lightLevelCalculator = ValueTypeLightLevels.REGISTRY.
                    getLightLevelCalculator(newValue.getType());
            if(lightLevelCalculator == null) {
                state.addGlobalError(new L10NHelpers.UnlocalizedString(L10NValues.PART_PANEL_ERROR_INVALIDTYPE,
                        new L10NHelpers.UnlocalizedString(newValue.getType().getUnlocalizedName())));
            } else {
                int lightLevel = lightLevelCalculator.getLightLevel(newValue);
                setLightLevel(target, lightLevel);
                state.sendUpdate();
            }
        }
    }

    public static void setLightLevel(PartTarget target, int lightLevel) {
        if(ConfigHandler.isEnabled(BlockInvisibleLightConfig.class)) {
            World world = target.getTarget().getPos().getWorld();
            BlockPos pos = target.getTarget().getPos().getBlockPos();
            if(world.isAirBlock(pos)) {
                if(lightLevel > 0) {
                    world.setBlockState(pos, BlockInvisibleLight.getInstance().getDefaultState().
                            withProperty(BlockInvisibleLight.LIGHT, lightLevel));
                } else {
                    world.setBlockToAir(pos);
                }
            }
        } else {
            IBlockAccess world = target.getCenter().getPos().getWorld();
            BlockPos pos = target.getCenter().getPos().getBlockPos();
            Block block = world.getBlockState(pos).getBlock();
            if(block instanceof IDynamicLightBlock) {
                ((IDynamicLightBlock) block).setLightLevel(world, pos, target.getCenter().getSide(), lightLevel);
            }
        }
    }

    public static class State extends PartTypePanelVariableDriven.State<PartTypePanelLightDynamic, PartTypePanelLightDynamic.State> {

        @Override
        public Class<? extends IPartState> getPartStateClass() {
            return PartTypePanelLightDynamic.State.class;
        }

    }

}
