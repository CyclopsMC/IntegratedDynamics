package pl.asie.charset.api.pipes;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IShifter {
	enum Mode {
		Extract,
		Shift
	}

	Mode getMode();

	EnumFacing getDirection();

	int getShiftDistance();

	boolean isShifting();

	boolean hasFilter();

	boolean matches(ItemStack source);
}
