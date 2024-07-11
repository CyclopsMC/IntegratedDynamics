package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;

/**
 * A list proxy for a certain position.
 */
public abstract class ValueTypeListProxyPositioned<T extends IValueType<V>, V extends IValue> extends ValueTypeListProxyBase<T, V> implements INBTProvider {

    private DimPos pos;
    private Direction side;

    public ValueTypeListProxyPositioned(ResourceLocation name, T valueType, DimPos pos, Direction side) {
        super(name, valueType);
        this.pos = pos;
        this.side = side;
    }

    @Override
    public void writeGeneratedFieldsToNBT(CompoundTag tag, HolderLookup.Provider holderLookupProvider) {
        NBTClassType.writeNbt(DimPos.class, "pos", pos, tag, holderLookupProvider);
        NBTClassType.writeNbt(Direction.class, "side", side, tag, holderLookupProvider);
    }

    @Override
    public void readGeneratedFieldsFromNBT(CompoundTag tag, HolderLookup.Provider holderLookupProvider) {
        this.pos = NBTClassType.readNbt(DimPos.class, "pos", tag, holderLookupProvider);
        this.side = NBTClassType.readNbt(Direction.class, "side", tag, holderLookupProvider);
    }

    protected DimPos getPos() {
        return pos;
    }

    protected void setPos(DimPos pos) {
        this.pos = pos;
    }

    protected Direction getSide() {
        return side;
    }

    protected void setSide(Direction side) {
        this.side = side;
    }
}
