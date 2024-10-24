package org.cyclops.integrateddynamics.block.shapes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

/**
 * A collision context that is used to indicate that we're checking for block support,
 * e.g. when checking if a lever can be attached to a part.
 * @author rubensworks
 */
public class CollisionContextBlockSupport extends EntityCollisionContext {
    public CollisionContextBlockSupport() {
        super(false, -Double.MAX_VALUE, ItemStack.EMPTY, p_205118_ -> false, null);
    }
}
