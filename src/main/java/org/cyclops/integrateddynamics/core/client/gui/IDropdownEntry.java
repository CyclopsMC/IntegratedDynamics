package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

/**
 * Entry for {@link WidgetTextFieldDropdown}.
 * @author rubensworks
 */
public interface IDropdownEntry<V> {
    public String getMatchString();
    public String getDisplayString();
    public List<ITextComponent> getTooltip();
    public V getValue();
}
