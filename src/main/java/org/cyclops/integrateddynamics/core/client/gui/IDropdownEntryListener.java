package org.cyclops.integrateddynamics.core.client.gui;

/**
 * Listener for {@link GuiTextFieldDropdown} dropdown element selection..
 * @author rubensworks
 */
public interface IDropdownEntryListener {
    /**
     * Will be called when a dropdown entry is selected or completely matches.
     * @param dropdownEntry The selected entry.
     */
    public void onSetDropdownPossiblity(IDropdownEntry<?> dropdownEntry);
}
