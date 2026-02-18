package org.cyclops.integrateddynamics.client.model;

import net.minecraft.nbt.Tag;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

/**
 * @author rubensworks
 */
public class CableRenderState implements IRenderState {

    private final boolean realCable;
    private final EnumFacingMap<Boolean> connected;
    private final EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> partData;
    private final Tag facadeBlock;

    public CableRenderState(boolean realCable, EnumFacingMap<Boolean> connected, EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> partData, Tag facadeBlock) {
        this.realCable = realCable;
        this.connected = connected;
        this.partData = partData;
        this.facadeBlock = facadeBlock;
    }

    public boolean isRealCable() {
        return realCable;
    }

    public EnumFacingMap<Boolean> getConnected() {
        return connected;
    }

    public EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> getPartData() {
        return partData;
    }

    public Tag getFacadeBlock() {
        return facadeBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CableRenderState that = (CableRenderState) o;
        if (realCable != that.realCable) return false;
        if (!java.util.Objects.equals(connected, that.connected)) return false;
        if (!java.util.Objects.equals(partData, that.partData)) return false;
        return java.util.Objects.equals(facadeBlock, that.facadeBlock);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(realCable, connected, partData, facadeBlock);
    }

    @Override
    public String toString() {
        return "CableRenderState(" + "realCable=" + realCable + ", connected=" + connected + ", partData=" + partData + ", facadeBlock=" + facadeBlock + ")";
    }

}
