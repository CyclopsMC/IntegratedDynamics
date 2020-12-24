package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.util.text.IFormattableTextComponent;

import java.util.List;

/**
 * Entry for {@link WidgetTextFieldDropdown}.
 * @author rubensworks
 */
public interface IDropdownEntry<V> {
    public String getMatchString();
    public IFormattableTextComponent getDisplayString();
    public List<IFormattableTextComponent> getTooltip();
    public V getValue();
}
