package org.cyclops.integrateddynamics.part.aspect.read.world;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;

import java.util.List;

/**
 * Aspect that counts the amount of entities in the target space
 * @author rubensworks
 */
public class AspectReadIntegerWorldEntity extends AspectReadIntegerWorldBase {

    @Override
    protected String getUnlocalizedIntegerWorldType() {
        return "entity";
    }

    protected Predicate Predicate() {
        return EntitySelectors.selectAnything;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ValueTypeInteger.ValueInteger getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        List<Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), Predicate());
        return ValueTypeInteger.ValueInteger.of(entities.size());
    }
}
