package org.cyclops.integrateddynamics.capability.dynamiclight;

import net.minecraft.core.Direction;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.api.block.IDynamicLight;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityMultipartTicking;

/**
 * Default implementation of {@link IDynamicLight}.
 * @author rubensworks
 */
public class DynamicLightTileMultipartTicking implements IDynamicLight {

    private final BlockEntityMultipartTicking tile;
    private final Direction side;

    public DynamicLightTileMultipartTicking(BlockEntityMultipartTicking tile, Direction side) {
        this.tile = tile;
        this.side = side;
    }

    protected EnumFacingMap<Integer> getLightLevels() {
        return tile.getLightLevels();
    }

    @Override
    public void setLightLevel(int level) {
        if(!tile.getLevel().isClientSide) {
            boolean sendUpdate = false;
            EnumFacingMap<Integer> lightLevels = getLightLevels();
            if(lightLevels.containsKey(side)) {
                if(lightLevels.get(side) != level) {
                    sendUpdate = true;
                    lightLevels.put(side, level);
                }
            } else {
                sendUpdate = true;
                lightLevels.put(side, level);
            }
            if(sendUpdate) {
                tile.updateLightInfo();
            }
        }
    }

    @Override
    public int getLightLevel() {
        EnumFacingMap<Integer> lightLevels = getLightLevels();
        if(lightLevels.containsKey(side)) {
            return lightLevels.get(side);
        }
        return 0;
    }
}
