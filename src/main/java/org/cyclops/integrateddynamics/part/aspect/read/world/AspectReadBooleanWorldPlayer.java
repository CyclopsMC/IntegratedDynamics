package org.cyclops.integrateddynamics.part.aspect.read.world;

import com.google.common.base.Predicate;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Aspect that checks if the target block space has a player.
 * @author rubensworks
 */
public class AspectReadBooleanWorldPlayer extends AspectReadBooleanWorldEntity {

    protected static final Predicate SELECTOR = new Predicate() {
        public boolean apply(Object entity) {
            return entity instanceof EntityPlayer;
        }
    };

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "player";
    }

    @Override
    protected Predicate Predicate() {
        return SELECTOR;
    }
}
