package org.cyclops.integrateddynamics.part;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.GeneralConfig;
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
            state.addGlobalError(new TranslatableComponent(L10NValues.PART_PANEL_ERROR_INVALIDTYPE,
                    new TranslatableComponent(value.getType().getTranslationKey())));
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
                                      BlockGetter world, Block neighbourBlock, BlockPos neighbourPos) {
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
            Level world = target.getTarget().getPos().getLevel(true);
            BlockPos pos = target.getTarget().getPos().getBlockPos();
            if(world.isEmptyBlock(pos)) {
                if(lightLevel > 0) {
                    world.setBlockAndUpdate(pos, RegistryEntries.BLOCK_INVISIBLE_LIGHT.defaultBlockState()
                            .setValue(BlockInvisibleLight.LIGHT, lightLevel));
                } else {
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
                }
            }
        } else {
            BlockEntityHelpers.getCapability(target.getCenter().getPos(), target.getCenter().getSide(), DynamicLightConfig.CAPABILITY)
                    .ifPresent(dynamicLight -> dynamicLight.setLightLevel(lightLevel));
        }
    }

    public static class State extends PartTypePanelVariableDriven.State<PartTypePanelLightDynamic, PartTypePanelLightDynamic.State> {

    }

}
