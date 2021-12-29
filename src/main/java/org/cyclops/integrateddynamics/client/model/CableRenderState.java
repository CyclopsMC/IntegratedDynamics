package org.cyclops.integrateddynamics.client.model;

import lombok.Data;
import net.minecraft.nbt.Tag;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

/**
 * @author rubensworks
 */
@Data
public class CableRenderState implements IRenderState {

    private final boolean realCable;
    private final EnumFacingMap<Boolean> connected;
    private final EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> partData;
    private final Tag facadeBlock;

}
