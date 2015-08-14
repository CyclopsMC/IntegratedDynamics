package org.cyclops.integrateddynamics.part.aspect.read.world;

import com.google.common.base.Predicate;
import net.minecraft.entity.monster.IMob;

/**
 * Aspect that checks if the target block space has a mob.
 * @author rubensworks
 */
public class AspectReadBooleanWorldMob extends AspectReadBooleanWorldEntity {

    protected static final Predicate SELECTOR = new Predicate() {
        public boolean apply(Object entity) {
            return entity instanceof IMob;
        }
    };

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "mob";
    }

    @Override
    protected Predicate Predicate() {
        return SELECTOR;
    }
}
