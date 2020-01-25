package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.List;

/**
 * Base class for parse operators.
 * @author rubensworks/LostOfThought
 */
public class ParseOperator<T2 extends IValueType<V2>, V2 extends IValue> extends OperatorBase {

  private final T2 to;

  public ParseOperator(final T2 to, OperatorBase.IFunction operator) {
    super("parse_" + to.getTypeName(),
        "parse_" + to.getTranslationKey(),
        constructInputVariables(1, ValueTypes.STRING),
        to,
        operator,
        IConfigRenderPattern.PREFIX_1_LONG);
    this.to = to;
  }

  @Override
  public String getUniqueName() {
    return "operator." + getModId() + ".parse." + to.getTranslationKey();
  }

  @Override
  public String getUnlocalizedType() {
    return "parse";
  }

  @Override
  protected String getUnlocalizedPrefix() {
    return "operator." + getModId() + "." + getUnlocalizedType();
  }

  @Override
  public void loadTooltip(List<ITextComponent> lines, boolean appendOptionalInfo) {
    lines.add(new TranslationTextComponent("operator.integrateddynamics.parse.tooltip",
            new TranslationTextComponent(to.getTranslationKey()))
    );
    super.loadTooltip(lines, appendOptionalInfo);
  }

}