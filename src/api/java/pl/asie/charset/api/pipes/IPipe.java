package pl.asie.charset.api.pipes;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import pl.asie.charset.api.lib.IItemInjectable;

public interface IPipe extends IItemInjectable {
	/**
	 * Get the stack closest to the middle of a given side of the pipe.
	 * WARNING: This is not a free function and should be used primarily
	 * by item detectors.
	 *
	 * @param side The side (null - center)
	 * @return The closest stack found.
	 */
	ItemStack getTravellingStack(EnumFacing side);
}
