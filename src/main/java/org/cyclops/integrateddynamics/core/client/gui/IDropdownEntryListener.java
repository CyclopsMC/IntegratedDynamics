package org.cyclops.integrateddynamics.core.client.gui;

import javax.annotation.Nullable;

/**
 * Listener for {@link WidgetTextFieldDropdown} dropdown element selection.
 * @param <T> The dropdown entry type.
 * @author rubensworks
 */
public interface IDropdownEntryListener<T> {
    /**
     * Will be called when a dropdown entry is selected or completely matches.
     * @param dropdownEntry The selected entry.
     */
    public void onSetDropdownPossiblity(@Nullable IDropdownEntry<T> dropdownEntry);
}
