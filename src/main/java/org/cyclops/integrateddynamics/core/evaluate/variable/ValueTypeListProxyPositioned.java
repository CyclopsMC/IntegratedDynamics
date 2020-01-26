package org.cyclops.integrateddynamics.core.evaluate.variable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
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
    public void writeGeneratedFieldsToNBT(CompoundNBT tag) {
        NBTClassType.writeNbt(DimPos.class, "pos", pos, tag);
        NBTClassType.writeNbt(Direction.class, "side", side, tag);
    }

    @Override
    public void readGeneratedFieldsFromNBT(CompoundNBT tag) {
        this.pos = NBTClassType.readNbt(DimPos.class, "pos", tag);
        this.side = NBTClassType.readNbt(Direction.class, "side", tag);
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
