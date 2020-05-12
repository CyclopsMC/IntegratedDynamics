package org.cyclops.integrateddynamics.part;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.InvalidValueTypeException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.block.BlockInvisibleLight;
import org.cyclops.integrateddynamics.block.BlockInvisibleLightConfig;
import org.cyclops.integrateddynamics.capability.dynamiclight.DynamicLightConfig;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLightLevels;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.part.panel.PartTypePanelVariableDriven;
import org.cyclops.integrateddynamics.GeneralConfig;

/**
 * A part that can display variables.
 * @author rubensworks
 */
public class PartTypePanelLightDynamic extends PartTypePanelVariableDriven<PartTypePanelLightDynamic, PartTypePanelLightDynamic.State> {

    public PartTypePanelLightDynamic(String name) {
        super(name);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus();
    }

    @Override
    public PartTypePanelLightDynamic.State constructDefaultState() {
        return new PartTypePanelLightDynamic.State();
    }
    
    @Override
    public int getConsumptionRate(State state) {
        return GeneralConfig.panelLightDynamicBaseConsumption;
    }

    @Override
    protected IgnoredBlockStatus.Status getStatus(PartTypePanelVariableDriven.State state) {
        IgnoredBlockStatus.Status status = super.getStatus(state);
        if (status == IgnoredBlockStatus.Status.ACTIVE && state.getDisplayValue() != null
                && getLightLevel((State) state, state.getDisplayValue()) == 0) {
            return IgnoredBlockStatus.Status.INACTIVE;
        }
        return status;
    }

    @Override
    protected void onValueChanged(INetwork network, IPartNetwork partNetwork, PartTarget target, State state, IValue lastValue, IValue newValue) {
        super.onValueChanged(network, partNetwork, target, state, lastValue, newValue);
        int lightLevel = 0;
        if(newValue != null) {
            lightLevel = getLightLevel(state, newValue);
        }
        setLightLevel(target, lightLevel);
        state.sendUpdate();
    }

    protected int getLightLevel(State state, IValue value) {
        try {
            return ValueTypeLightLevels.REGISTRY.getLightLevel(value);
        } catch (InvalidValueTypeException e) {
            state.addGlobalError(new TranslationTextComponent(L10NValues.PART_PANEL_ERROR_INVALIDTYPE,
                    new TranslationTextComponent(value.getType().getTranslationKey())));
        }
        return 0;
    }

    @Override
    public void onNetworkRemoval(INetwork network, IPartNetwork partNetwork, PartTarget target, State state) {
        super.onNetworkRemoval(network, partNetwork, target, state);
        PartTypePanelLightDynamic.setLightLevel(target, 0);
    }

    @Override
    public void onPostRemoved(INetwork network, IPartNetwork partNetwork, PartTarget target, State state) {
        super.onPostRemoved(network, partNetwork, target, state);
        setLightLevel(target, 0);
    }

    @Override
    public void onBlockNeighborChange(INetwork network, IPartNetwork partNetwork, PartTarget target, State state,
                                      IBlockReader world, Block neighbourBlock, BlockPos neighbourPos) {
        super.onBlockNeighborChange(network, partNetwork, target, state, world, neighbourBlock, neighbourPos);
        setLightLevel(target, state.getDisplayValue() == null ? 0 : getLightLevel(state, state.getDisplayValue()));
    }

    @Override
    public void postUpdate(INetwork network, IPartNetwork partNetwork, PartTarget target, State state, boolean updated) {
        boolean wasEnabled = isEnabled(state);
        super.postUpdate(network, partNetwork, target, state, updated);
        boolean isEnabled = isEnabled(state);
        if(wasEnabled != isEnabled) {
            setLightLevel(target, isEnabled ? getLightLevel(state, state.getDisplayValue()) : 0);
        }
    }

    public static void setLightLevel(PartTarget target, int lightLevel) {
        if (BlockInvisibleLightConfig.invisibleLightBlock) {
            World world = target.getTarget().getPos().getWorld(true);
            BlockPos pos = target.getTarget().getPos().getBlockPos();
            if(world.isAirBlock(pos)) {
                if(lightLevel > 0) {
                    world.setBlockState(pos, RegistryEntries.BLOCK_INVISIBLE_LIGHT.getDefaultState().
                            with(BlockInvisibleLight.LIGHT, lightLevel));
                } else {
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
                }
            }
        } else {
            TileHelpers.getCapability(target.getCenter().getPos(), target.getCenter().getSide(), DynamicLightConfig.CAPABILITY)
                    .ifPresent(dynamicLight -> dynamicLight.setLightLevel(lightLevel));
        }
    }

    public static class State extends PartTypePanelVariableDriven.State<PartTypePanelLightDynamic, PartTypePanelLightDynamic.State> {

    }

}
