package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
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
    private EnumFacing side;

    protected PositionedOperator(String symbol, String operatorName, IValueType[] inputTypes, IValueType outputType,
                                 IFunction function, IConfigRenderPattern renderPattern, DimPos pos, EnumFacing side) {
        super(symbol, operatorName, inputTypes, outputType, function, renderPattern);
        this.pos = pos;
        this.side = side;
    }

    @Override
    public IOperator materialize() {
        return this;
    }

    @Override
    public void writeGeneratedFieldsToNBT(NBTTagCompound tag) {
        NBTClassType.writeNbt(DimPos.class, "pos", pos, tag);
        NBTClassType.writeNbt(EnumFacing.class, "side", side, tag);
    }

    @Override
    public void readGeneratedFieldsFromNBT(NBTTagCompound tag) {
        this.pos = NBTClassType.readNbt(DimPos.class, "pos", tag);
        this.side = NBTClassType.readNbt(EnumFacing.class, "side", tag);
    }

    public DimPos getPos() {
        return pos;
    }

    public void setPos(DimPos pos) {
        this.pos = pos;
    }

    public EnumFacing getSide() {
        return side;
    }

    public void setSide(EnumFacing side) {
        this.side = side;
    }

    public static class Serializer implements IOperatorSerializer<PositionedOperator> {

        private final Class<? extends PositionedOperator> clazz;
        private final String uniqueName;

        public Serializer(Class<? extends PositionedOperator> clazz, String uniqueName) {
            this.clazz = clazz;
            this.uniqueName = uniqueName;
        }

        @Override
        public boolean canHandle(IOperator operator) {
            return this.clazz.isInstance(operator);
        }

        @Override
        public String getUniqueName() {
            return this.uniqueName;
        }

        @Override
        public String serialize(PositionedOperator operator) {
            NBTTagCompound tag = new NBTTagCompound();
            operator.writeGeneratedFieldsToNBT(tag);
            return tag.toString();
        }

        @Override
        public PositionedOperator deserialize(String value) throws EvaluationException {
            try {
                Constructor<? extends PositionedOperator> constructor = this.clazz.getConstructor();
                PositionedOperator proxy = constructor.newInstance();
                NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
                proxy.readGeneratedFieldsFromNBT(tag);
                return proxy;
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | NBTException | IllegalAccessException e) {
                e.printStackTrace();
                throw new EvaluationException(e.getMessage());
            }
        }
    }
}
