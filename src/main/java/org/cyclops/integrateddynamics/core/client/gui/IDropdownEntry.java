package org.cyclops.integrateddynamics.core.client.gui;

import java.util.List;

/**
 * Entry for {@link GuiTextFieldDropdown}.
 * @author rubensworks
 */
public interface IDropdownEntry<V> {
    public String getMatchString();
    public String getDisplayString();
    public List<String> getTooltip();
    public V getValue();
}
