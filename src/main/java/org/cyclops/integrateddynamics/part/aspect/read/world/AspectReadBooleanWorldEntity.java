package org.cyclops.integrateddynamics.part.aspect.read.world;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

import java.util.List;

/**
 * Aspect that checks if the target block space has an entity.
 * @author rubensworks
 */
public class AspectReadBooleanWorldEntity extends AspectReadBooleanWorldBase {

    @Override
    protected String getUnlocalizedBooleanWorldType() {
        return "entity";
    }

    protected Predicate Predicate() {
        return EntitySelectors.selectAnything;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeBoolean.ValueBoolean getValue(PartTarget target, AspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        List<Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), Predicate());
        return ValueTypeBoolean.ValueBoolean.of(!entities.isEmpty());
    }
}
