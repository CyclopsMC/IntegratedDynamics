package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * An abstract operator that is based on something positioned.
 * @author rubensworks
 */
public abstract class PositionedOperator extends OperatorBase implements INBTProvider {

    private DimPos pos;
    private Direction side;

    protected PositionedOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                                 IFunction function, IConfigRenderPattern renderPattern, DimPos pos, Direction side) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
        this.pos = pos;
        this.side = side;
    }

    @Override
    public IOperator materialize() {
        return this;
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

    public DimPos getPos() {
        return pos;
    }

    public void setPos(DimPos pos) {
        this.pos = pos;
    }

    public Direction getSide() {
        return side;
    }

    public void setSide(Direction side) {
        this.side = side;
    }

    public static class Serializer implements IOperatorSerializer<PositionedOperator> {

        private final Class<? extends PositionedOperator> clazz;
        private final ResourceLocation uniqueName;

        public Serializer(Class<? extends PositionedOperator> clazz, ResourceLocation uniqueName) {
            this.clazz = clazz;
            this.uniqueName = uniqueName;
        }

        @Override
        public boolean canHandle(IOperator operator) {
            return this.clazz.isInstance(operator);
        }

        @Override
        public ResourceLocation getUniqueName() {
            return this.uniqueName;
        }

        @Override
        public INBT serialize(PositionedOperator operator) {
            CompoundNBT tag = new CompoundNBT();
            operator.writeGeneratedFieldsToNBT(tag);
            return tag;
        }

        @Override
        public PositionedOperator deserialize(INBT value) throws EvaluationException {
            try {
                Constructor<? extends PositionedOperator> constructor = this.clazz.getConstructor();
                PositionedOperator proxy = constructor.newInstance();
                proxy.readGeneratedFieldsFromNBT((CompoundNBT) value);
                return proxy;
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | ClassCastException | IllegalAccessException e) {
                e.printStackTrace();
                throw new EvaluationException(e.getMessage());
            }
        }
    }
}
