package org.cyclops.integrateddynamics.part.aspect.read.world;

import com.google.common.base.Predicate;
import net.minecraft.entity.item.EntityItem;

/**
 * Aspect that checks if the target block space has an item.
 * @author rubensworks
 */
public class AspectReadBooleanWorldItem extends AspectReadBooleanWorldEntity {

    protected static final Predicate SELECTOR = new Predicate() {
        public boolean apply(Object entity) {
            return entity instanceof EntityItem;
        }
    };

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "item";
    }

    @Override
    protected Predicate Predicate() {
        return SELECTOR;
    }
}
