package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.evaluate.variable.Variable;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * An operator that is partially being applied.
 * @author rubensworks
 */
public class CurriedOperator implements IOperator {

    private final IOperator baseOperator;
    private final IVariable[] appliedVariables;

    public CurriedOperator(IOperator baseOperator, IVariable... appliedVariables) {
        this.baseOperator = baseOperator;
        this.appliedVariables = appliedVariables;
    }

    protected String getAppliedSymbol() {
        String symbol = "";
        for (IVariable appliedVariable : appliedVariables) {
            symbol += appliedVariable.getType().getTypeName() + ";";
        }
        return symbol;
    }

    @Override
    public String getSymbol() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseOperator.getSymbol());
        sb.append(" [");
        sb.append(getAppliedSymbol());
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String getUniqueName() {
        return "curriedOperator";
    }

    @Override
    public String getTranslationKey() {
        return baseOperator.getTranslationKey();
    }

    @Override
    public String getUnlocalizedCategoryName() {
        return baseOperator.getUnlocalizedCategoryName();
    }

    @Override
    public String getLocalizedNameFull() {
        return L10NHelpers.localize(L10NValues.OPERATOR_APPLIED_OPERATORNAME,
                baseOperator.getLocalizedNameFull(), getAppliedSymbol());
    }

    @Override
    public void loadTooltip(List<String> lines, boolean appendOptionalInfo) {
        baseOperator.loadTooltip(lines, appendOptionalInfo);
        lines.add(L10NHelpers.localize(L10NValues.OPERATOR_APPLIED_TYPE, getAppliedSymbol()));
    }

    @Override
    public IValueType[] getInputTypes() {
        IValueType[] baseInputTypes = baseOperator.getInputTypes();
        return Arrays.copyOfRange(baseInputTypes, appliedVariables.length, baseInputTypes.length);
    }

    @Override
    public IValueType getOutputType() {
        return baseOperator.getOutputType();
    }

    protected IVariable[] deriveFullInputVariables(IVariable[] partialInput) {
        IVariable[] fullInput = new IVariable[Math.min(baseOperator.getRequiredInputLength(), partialInput.length + appliedVariables.length)];
        for (int i = 0; i < appliedVariables.length; i++) {
            fullInput[i] = appliedVariables[i];
        }
        System.arraycopy(partialInput, 0, fullInput, appliedVariables.length, fullInput.length - appliedVariables.length);
        return fullInput;
    }

    protected IValueType[] deriveFullInputTypes(IValueType[] partialInput) {
        IValueType[] fullInput = new IValueType[Math.min(baseOperator.getRequiredInputLength(), partialInput.length + appliedVariables.length)];
        for (int i = 0; i < appliedVariables.length; i++) {
            fullInput[i] = appliedVariables[i].getType();
        }
        System.arraycopy(partialInput, 0, fullInput, appliedVariables.length, fullInput.length - appliedVariables.length);
        return fullInput;
    }

    @Override
    public IValueType getConditionalOutputType(IVariable[] input) {
        return baseOperator.getConditionalOutputType(deriveFullInputVariables(input));
    }

    @Override
    public IValue evaluate(IVariable[] input) throws EvaluationException {
        return baseOperator.evaluate(deriveFullInputVariables(input));
    }

    @Override
    public int getRequiredInputLength() {
        return baseOperator.getRequiredInputLength() - appliedVariables.length;
    }

    @Override
    public L10NHelpers.UnlocalizedString validateTypes(IValueType[] input) {
        return baseOperator.validateTypes(deriveFullInputTypes(input));
    }

    @Override
    public IConfigRenderPattern getRenderPattern() {
        return IConfigRenderPattern.NONE;
    }

    @Override
    public IOperator materialize() throws EvaluationException {
        IVariable[] variables = new IVariable[appliedVariables.length];
        for (int i = 0; i < appliedVariables.length; i++) {
            IVariable appliedVariable = appliedVariables[i];
            variables[i] = new Variable<>(appliedVariable.getType(), appliedVariable.getValue());
        }
        return new CurriedOperator(baseOperator, variables);
    }

    public IOperator getBaseOperator() {
        return baseOperator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurriedOperator)) return false;
        CurriedOperator that = (CurriedOperator) o;
        return Objects.equals(baseOperator, that.baseOperator) &&
                Arrays.equals(appliedVariables, that.appliedVariables);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(baseOperator);
        result = 31 * result + Arrays.hashCode(appliedVariables);
        return result;
    }

    public static class Serializer implements IOperatorSerializer<CurriedOperator> {

        @Override
        public boolean canHandle(IOperator operator) {
            return operator instanceof CurriedOperator;
        }

        @Override
        public String getUniqueName() {
            return "curry";
        }

        @Override
        public String serialize(CurriedOperator operator) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < operator.appliedVariables.length; i++) {
                IVariable appliedVariable = operator.appliedVariables[i];
                IValue value;
                try {
                    value = appliedVariable.getValue();
                } catch (EvaluationException e) {
                    value = appliedVariable.getType().getDefault();
                }
                NBTTagCompound valueTag = new NBTTagCompound();
                IValueType valueType = value.getType();
                valueTag.setString("valueType", valueType.getTranslationKey());
                valueTag.setString("value", ValueHelpers.serializeRaw(value));
                list.appendTag(valueTag);
            }

            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("values", list);
            tag.setString("baseOperator", Operators.REGISTRY.serialize(operator.baseOperator));
            return tag.toString();
        }

        @Override
        public CurriedOperator deserialize(String valueOperator) throws EvaluationException {
            NBTTagCompound tag;
            try {
                tag = JsonToNBT.getTagFromJson(valueOperator);
            } catch (NBTException e) {
                e.printStackTrace();
                throw new EvaluationException(e.getMessage());
            }
            NBTTagList list = tag.getTagList("values", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
            IVariable[] variables = new IVariable[list.tagCount()];
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound valuetag = list.getCompoundTagAt(i);
                IValueType valueType = ValueTypes.REGISTRY.getValueType(valuetag.getString("valueType"));
                IValue value = ValueHelpers.deserializeRaw(valueType, valuetag.getString("value"));
                variables[i] = new Variable(valueType, value);
            }
            IOperator baseOperator = Objects.requireNonNull(Operators.REGISTRY.deserialize(tag.getString("baseOperator")));
            return new CurriedOperator(baseOperator, variables);
        }
    }
}
