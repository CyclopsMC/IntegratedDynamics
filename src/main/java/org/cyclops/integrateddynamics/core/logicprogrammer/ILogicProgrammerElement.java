package org.cyclops.integrateddynamics.core.logicprogrammer;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.client.gui.GuiLogicProgrammer;
import org.cyclops.integrateddynamics.core.evaluate.operator.IConfigRenderPattern;
import org.cyclops.integrateddynamics.core.item.IVariableFacade;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammer;

import java.util.List;

/**
 * An element inside the logic programmer.
 * @author rubensworks
 */
public interface ILogicProgrammerElement {

    /**
     * @return The element type.
     */
    public ILogicProgrammerElementType getType();

    /**
     * @return The string used to match regex searching.
     */
    public String getMatchString();

    /**
     * @return Localized name used for rendering.
     */
    public String getLocalizedNameFull();

    /**
     * @param lines The list to add tooltip lines to.
     */
    public void loadTooltip(List<String> lines);

    /**
     * @return The render pattern.
     */
    public IConfigRenderPattern getRenderPattern();

    /**
     * Called when an input item slot has been updated.
     * @param slotId The slot id.
     * @param itemStack The itemstack currently in the slot, can be null.
     */
    public void onInputSlotUpdated(int slotId, ItemStack itemStack);

    /**
     * @return If this element can be written to an item in its current state.
     */
    public boolean canWriteElementPre();

    /**
     * The stack to write the current state of this element to.
     * @param itemStack The stack to write to.
     * @return The resulting itemstack.
     */
    public ItemStack writeElement(ItemStack itemStack);

    /**
     * If this element in its current state can be deactivated because of another item being inserted into the
     * write slot.
     * @return If this element can be deactivated.
     */
    public boolean canCurrentlyReadFromOtherItem();

    /**
     * Called when this element is activated.
     */
    public void activate();

    /**
     * Called when this element is deactivated.
     */
    public void deactivate();

    /**
     * Validates the current state of the element.
     * @return An error or null.
     */
    public L10NHelpers.UnlocalizedString validate();

    /**
     * @return The color used to identify this element.
     */
    public int getColor();

    /**
     * @return The symbol used to identify this element.
     */
    public String getSymbol();

    /**
     * @param variableFacade A variable facade
     * @return If this element corresponds to the given variable facade.
     */
    public boolean isFor(IVariableFacade variableFacade);

    /**
     * @param baseX Base x
     * @param baseY Base y
     * @param maxWidth Max width
     * @param maxHeight Max height
     * @param gui The parent gui
     * @param container The parent container
     * @return A subgui that is shown when activated.
     */
    @SideOnly(Side.CLIENT)
    public SubGuiConfigRenderPattern createSubGui(int baseX, int baseY, int maxWidth, int maxHeight,
                                                  GuiLogicProgrammer gui, ContainerLogicProgrammer container);

}
