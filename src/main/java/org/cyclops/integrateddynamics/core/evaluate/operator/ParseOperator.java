package org.cyclops.integrateddynamics.core.evaluate.operator;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.codehaus.plexus.util.StringUtils;
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

  public ParseOperator(final T2 to, IFunction operator) {
    super("parse_" + to.getTypeName(),
            "parse_" + to.getTranslationKey(),
            "parseAs" + StringUtils.capitalise(to.getTypeName()),
            null,
            false,
            constructInputVariables(1, ValueTypes.STRING),
            to,
            operator, IConfigRenderPattern.PREFIX_1_LONG);
    this.to = to;
  }

  @Override
  public ResourceLocation getUniqueName() {
    return new ResourceLocation(getModId(), "operator." + getModId() + ".parse." + to.getTranslationKey());
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
  public void loadTooltip(List<Component> lines, boolean appendOptionalInfo) {
    lines.add(Component.translatable("operator.integrateddynamics.parse.tooltip",
            Component.translatable(to.getTranslationKey()))
    );
    super.loadTooltip(lines, appendOptionalInfo);
  }

}
