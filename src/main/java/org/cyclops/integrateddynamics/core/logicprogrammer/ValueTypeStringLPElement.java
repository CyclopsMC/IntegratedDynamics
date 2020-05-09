package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.api.client.gui.subgui.ISubGuiBox;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammerBase;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Element for value types that can be read from and written to strings.
 * @author rubensworks
 */
public class ValueTypeStringLPElement extends ValueTypeLPElementBase {

    public ValueTypeStringLPElement(IValueType valueType) {
        super(valueType);
    }

    @Override
    public boolean canWriteElementPre() {
        return getInnerGuiElement().getInputString() != null;
    }

    @Override
    public boolean canCurrentlyReadFromOtherItem() {
        return this.getInnerGuiElement().getInputString() == null || this.getInnerGuiElement().getInputString().equals(getInnerGuiElement().getDefaultInputString());
    }

    @Override
    public void activate() {
        getInnerGuiElement().setInputString(new String(getInnerGuiElement().getDefaultInputString()));
    }

    @Override
    public void deactivate() {
        getInnerGuiElement().setInputString(null);
    }

    @Override
    public L10NHelpers.UnlocalizedString validate() {
        return getValueType().canDeserialize(getInnerGuiElement().getInputString());
    }

    @Override
    public IValue getValue() {
        return ValueHelpers.deserializeRaw(getInnerGuiElement().getValueType(), getInnerGuiElement().getInputString());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFocused(ISubGuiBox subGui) {
        return ((ValueTypeLPElementRenderPattern) subGui).getSearchField().isFocused();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setFocused(ISubGuiBox subGui, boolean focused) {
        ((ValueTypeLPElementRenderPattern) subGui).getSearchField().setFocused(focused);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISubGuiBox createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                            GuiLogicProgrammerBase gui, ContainerLogicProgrammerBase container) {
        return new ValueTypeLPElementRenderPattern(this, baseX, baseY, maxWidth, maxHeight, gui, container);
    }

}
