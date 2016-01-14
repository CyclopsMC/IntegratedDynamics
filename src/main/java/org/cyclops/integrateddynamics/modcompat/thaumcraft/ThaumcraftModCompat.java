package org.cyclops.integrateddynamics.modcompat.thaumcraft;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.logicprogrammer.IConfigRenderPattern;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;
import org.cyclops.integrateddynamics.core.evaluate.operator.ObjectItemStackOperator;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorBase;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.client.render.valuetype.AspectValueTypeWorldRenderer;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.operator.ObjectThaumcraftAspectOperator;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueObjectTypeAspect;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueTypeListProxyPositionedAspectContainer;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.logicprogrammer.ValueObjectTypeAspectElementType;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;

import java.util.Collections;
import java.util.List;

/**
 * Compatibility plugin for Thaumcraft.
 * @author rubensworks
 *
 */
public class ThaumcraftModCompat implements IModCompat {

	public static ValueObjectTypeAspect OBJECT_ASPECT;
	public static ValueTypeListProxyNBTFactory<ValueObjectTypeAspect, ValueObjectTypeAspect.ValueAspect, ValueTypeListProxyPositionedAspectContainer> POSITIONED_ASPECTCONTAINER;
	public static ValueObjectTypeAspectElementType OBJECT_ASPECT_ELEMENTTYPE;

    @Override
    public String getModID() {
        return Reference.MOD_THAUMCRAFT;
    }

    @Override
    public void onInit(Step step) {
    	if(step == Step.PREINIT) {
			// Part types
			PartTypes.REGISTRY.register(new PartTypeThaumcraftReader("thaumcraftReader"));

			// Value types
			OBJECT_ASPECT = ValueTypes.REGISTRY.register(new ValueObjectTypeAspect());

			// List proxy factories
			POSITIONED_ASPECTCONTAINER = ValueTypeListProxyFactories.REGISTRY.register(new ValueTypeListProxyNBTFactory<>("positionedAspectContainer", ValueTypeListProxyPositionedAspectContainer.class));

			// Logic programmer aspect value type creator
			OBJECT_ASPECT_ELEMENTTYPE = LogicProgrammerElementTypes.REGISTRY.addType(new ValueObjectTypeAspectElementType());

			// Operators
			/* Get aspects from item */
			Operators.REGISTRY.register(new ObjectItemStackOperator("aspects", "getitemthaumcraftaspects", new IValueType[]{ValueTypes.OBJECT_ITEMSTACK}, ValueTypes.LIST, new OperatorBase.IFunction() {
				@Override
				public IValue evaluate(IVariable... variables) throws EvaluationException {
					Optional<ItemStack> a = ((ValueObjectTypeItemStack.ValueItemStack) variables[0].getValue()).getRawValue();
					if(a.isPresent()) {
						AspectList aspectList = AspectHelper.getObjectAspects(a.get());
						Aspect[] aspectArray = aspectList.getAspectsSortedByAmount();
						List<ValueObjectTypeAspect.ValueAspect> list = Lists.newArrayListWithExpectedSize(aspectArray.length);
						for(Aspect aspect : aspectArray) {
							list.add(ValueObjectTypeAspect.ValueAspect.of(aspect, aspectList.getAmount(aspect)));
						}
						return ValueTypeList.ValueList.ofList(OBJECT_ASPECT, list);
					} else {
						return ValueTypeList.ValueList.ofList(OBJECT_ASPECT, Collections.EMPTY_LIST);
					}
				}
			}, IConfigRenderPattern.SUFFIX_1_LONG));
			/* Get amount of vis in aspect */
			Operators.REGISTRY.register(ObjectThaumcraftAspectOperator.toInt("amount", new ObjectThaumcraftAspectOperator.IIntegerFunction() {
				@Override
				public int evaluate(Aspect aspect, int amount) throws EvaluationException {
					return amount;
				}
			}));
			/* Check if the raw aspect of two aspects are equal independent of amount */
			Operators.REGISTRY.register(new ObjectThaumcraftAspectOperator("=Aspect=", "israwaspectequal", new IValueType[]{OBJECT_ASPECT, OBJECT_ASPECT}, ValueTypes.BOOLEAN, new OperatorBase.IFunction() {
				@Override
				public IValue evaluate(IVariable... variables) throws EvaluationException {
					Optional<Pair<Aspect, Integer>> a = ((ValueObjectTypeAspect.ValueAspect) variables[0].getValue()).getRawValue();
					Optional<Pair<Aspect, Integer>> b = ((ValueObjectTypeAspect.ValueAspect) variables[1].getValue()).getRawValue();
					boolean equal = false;
					if(a.isPresent() && b.isPresent()) {
						equal = a.get().getKey().equals(b.get().getKey());
					} else if(!a.isPresent() && !b.isPresent()) {
						equal = true;
					}
					return ValueTypeBoolean.ValueBoolean.of(equal);
				}
			}, IConfigRenderPattern.INFIX));
			/* Get the aspects this aspect is made up of */
			Operators.REGISTRY.register(new ObjectThaumcraftAspectOperator("compound aspects", "getcompoundaspects", new IValueType[]{OBJECT_ASPECT}, ValueTypes.LIST, new OperatorBase.IFunction() {
				@Override
				public IValue evaluate(IVariable... variables) throws EvaluationException {
					Optional<Pair<Aspect, Integer>> a = ((ValueObjectTypeAspect.ValueAspect) variables[0].getValue()).getRawValue();
					if(a.isPresent()) {
						Aspect[] aspectArray = a.get().getKey().getComponents();
						List<ValueObjectTypeAspect.ValueAspect> list = Lists.newArrayListWithExpectedSize(aspectArray.length);
						for(Aspect aspect : aspectArray) {
							list.add(ValueObjectTypeAspect.ValueAspect.of(aspect, 1));
						}
						return ValueTypeList.ValueList.ofList(OBJECT_ASPECT, list);
					}
					return ValueTypeList.ValueList.ofList(OBJECT_ASPECT, Collections.EMPTY_LIST);
				}
			}, IConfigRenderPattern.SUFFIX_1_LONG));
			/* Check if the given aspect is primal */
			Operators.REGISTRY.register(ObjectThaumcraftAspectOperator.toBoolean("isprimal", new ObjectThaumcraftAspectOperator.IBooleanFunction() {
				@Override
				public boolean evaluate(Aspect aspect, int amount) throws EvaluationException {
					return aspect.isPrimal();
				}
			}));

			if(MinecraftHelpers.isClientSide()) {
				initClient();
			}
		}
    }

	@SideOnly(Side.CLIENT)
	protected void initClient() {
		ValueTypeWorldRenderers.REGISTRY.register(ThaumcraftModCompat.OBJECT_ASPECT, new AspectValueTypeWorldRenderer());
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getComment() {
		return "Thaumcraft reader.";
	}

}
