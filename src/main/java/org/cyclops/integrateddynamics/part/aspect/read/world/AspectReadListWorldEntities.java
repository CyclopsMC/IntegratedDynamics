package org.cyclops.integrateddynamics.part.aspect.read.world;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeEntity;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadListBase;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Read a block from the world.
 * @author rubensworks
 */
public class AspectReadListWorldEntities extends AspectReadListBase {

    @Override
    protected String getUnlocalizedListType() {
        return "world.entities";
    }

    @Override
    protected ValueTypeList.ValueList getValue(PartTarget target, IAspectProperties properties) {
        DimPos dimPos = target.getTarget().getPos();
        List<Entity> entities = dimPos.getWorld().getEntitiesInAABBexcluding(null,
                new AxisAlignedBB(dimPos.getBlockPos(), dimPos.getBlockPos().add(1, 1, 1)), EntitySelectors.selectAnything);
        return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ENTITY, Lists.transform(entities, new Function<Entity, ValueObjectTypeEntity.ValueEntity>() {
            @Nullable
            @Override
            public ValueObjectTypeEntity.ValueEntity apply(Entity input) {
                return ValueObjectTypeEntity.ValueEntity.of(input);
            }
        }));
    }
}
