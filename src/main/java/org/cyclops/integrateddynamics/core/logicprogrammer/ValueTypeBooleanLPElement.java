package org.cyclops.integrateddynamics.core.logicprogrammer;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.client.gui.container.ContainerScreenLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.gui.GuiElementValueTypeString;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Element for the boolean value type that is controlled via a checkbox.
 * @author rubensworks
 */
public class ValueTypeBooleanLPElement extends ValueTypeLPElementBase {

    @Getter
    @Setter
    private boolean inputBoolean = false;

    public ValueTypeBooleanLPElement(IValueType valueType) {
        super(valueType);
    }

    @Override
    public void activate() {
        this.inputBoolean = false;
    }

    @Override
    public ITextComponent validate() {
        return null;
    }

    @Override
    public IValue getValue() {
        return ValueTypeBoolean.ValueBoolean.of(inputBoolean);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                   ContainerScreenLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new ValueTypeBooleanLPElementRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
