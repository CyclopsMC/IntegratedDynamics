package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * An abstract operator that is based on something positioned.
 * @author rubensworks
 */
public abstract class PositionedOperator extends OperatorBase implements INBTProvider {

    private DimPos pos;
    private Direction side;

    protected PositionedOperator(String symbol, String operatorName, String interactName, IValueType[] inputTypes, IValueType outputType,
                                 IFunction function, IConfigRenderPattern renderPattern, DimPos pos, Direction side) {
        super(symbol, operatorName, interactName, null, false, inputTypes, outputType, function, renderPattern);
        this.pos = pos;
        this.side = side;
    }

    @Override
    public IOperator materialize() {
        return this;
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
        public Tag serialize(ValueDeseralizationContext valueDeseralizationContext, PositionedOperator operator) {
            CompoundTag tag = new CompoundTag();
            operator.writeGeneratedFieldsToNBT(tag, valueDeseralizationContext.holderLookupProvider());
            return tag;
        }

        @Override
        public PositionedOperator deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag value) throws EvaluationException {
            try {
                Constructor<? extends PositionedOperator> constructor = this.clazz.getConstructor();
                PositionedOperator proxy = constructor.newInstance();
                proxy.readGeneratedFieldsFromNBT((CompoundTag) value, valueDeseralizationContext.holderLookupProvider());
                return proxy;
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | ClassCastException | IllegalAccessException e) {
                e.printStackTrace();
                throw new EvaluationException(Component.translatable(L10NValues.VALUETYPE_ERROR_DESERIALIZE,
                        value, e.getMessage()));
            }
        }
    }
}
