package org.cyclops.integrateddynamics.part.aspect.read.world;

import com.google.common.base.Predicate;
import net.minecraft.entity.passive.IAnimals;

/**
 * Aspect that checks if the target block space has an animal.
 * @author rubensworks
 */
public class AspectReadBooleanWorldAnimal extends AspectReadBooleanWorldEntity {

    protected static final Predicate SELECTOR = new Predicate() {
        public boolean apply(Object entity) {
            return entity instanceof IAnimals;
        }
    };

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "animal";
    }

    @Override
    protected Predicate Predicate() {
        return SELECTOR;
    }
}
