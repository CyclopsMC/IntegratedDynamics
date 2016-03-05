package pl.asie.charset.api.lib;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

@Deprecated
public interface IItemInjectable {
	/**
	 * Checks if items can be injected from a given side.
	 *
	 * @param side
	 * @return True if items can be injected from this side.
	 */
	boolean canInjectItems(EnumFacing side);

	/**
	 * Tries to inject an item into the pipe.
	 *
	 * @param stack    The stack offered for injection. Make a copy before editing.
	 * @param side     The side the item is being injected from.
	 * @param simulate If true, no actual injection should take place.
	 * @return The amount of items used from the stack.
	 */
	int injectItem(ItemStack stack, EnumFacing side, boolean simulate);
}
