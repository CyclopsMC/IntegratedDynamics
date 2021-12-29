package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * Entry for {@link WidgetTextFieldDropdown}.
 * @author rubensworks
 */
public interface IDropdownEntry<V> {
    public String getMatchString();
    public MutableComponent getDisplayString();
    public List<MutableComponent> getTooltip();
    public V getValue();
}
