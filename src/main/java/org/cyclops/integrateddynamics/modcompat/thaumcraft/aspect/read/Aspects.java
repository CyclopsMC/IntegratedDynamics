package org.cyclops.integrateddynamics.modcompat.thaumcraft.aspect.read;

import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.modcompat.thaumcraft.evaluate.variable.ValueTypeListProxyPositionedAspectContainer;
import org.cyclops.integrateddynamics.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import thaumcraft.api.aspects.IAspectContainer;

/**
 * Builders for thaumcraft aspects
 * @author rubensworks
 */
public class Aspects {

    public static final class Read {

        public static final class Aspect {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISASPECTCONTAINER =
                    AspectReadBuilders.BUILDER_BOOLEAN.appendKind("thaumcraft").handle(new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<PartTarget, IAspectProperties> input) {
                            DimPos dimPos = input.getLeft().getTarget().getPos();
                            return TileHelpers.getSafeTile(dimPos.getWorld(), dimPos.getBlockPos(), IAspectContainer.class) != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isaspectcontainer").build();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ASPECTCONTAINER =
                    AspectReadBuilders.BUILDER_LIST.appendKind("thaumcraft").handle(new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(Pair<PartTarget, IAspectProperties> input) {
                            return ValueTypeList.ValueList.ofFactory(new ValueTypeListProxyPositionedAspectContainer(input.getLeft().getTarget().getPos()));
                        }
                    }).appendKind("aspectcontainer").build();

        }

    }

}
