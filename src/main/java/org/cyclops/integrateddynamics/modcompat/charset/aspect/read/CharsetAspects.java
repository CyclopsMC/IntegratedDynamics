package org.cyclops.integrateddynamics.modcompat.charset.aspect.read;

import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectReadBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import pl.asie.charset.api.pipes.IPipe;

/**
 * Builders for charset aspects
 * @author rubensworks
 */
public class CharsetAspects {

    public static final class Read {

        public static final class Pipe {

            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IPipe> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IPipe>() {
                @Override
                public IPipe getOutput(Pair<PartTarget, IAspectProperties> input) {
                    DimPos pos = input.getLeft().getTarget().getPos();
                    return CableHelpers.getInterface(pos, IPipe.class);
                }
            };

            public static final AspectReadBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IPipe>
                    BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "charsetpipe");
            public static final AspectReadBuilder<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack, IPipe>
                    BUILDER_OBJECT_ITEMSTACK = AspectReadBuilders.BUILDER_OBJECT_ITEMSTACK.handle(PROP_GET, "charsetpipe");

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISAPPLICABLE =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IPipe, Boolean>() {
                        @Override
                        public Boolean getOutput(IPipe pipe) {
                            return pipe != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").build();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_HASCONTENTS =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IPipe, Boolean>() {
                        @Override
                        public Boolean getOutput(IPipe pipe) {
                            if(pipe != null) {
                                return pipe.getTravellingStack(null) != null;
                            }
                            return false;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "hascontents").build();

            public static final IAspectRead<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack> ITEMSTACK_CONTENTS =
                    BUILDER_OBJECT_ITEMSTACK.handle(new IAspectValuePropagator<IPipe, ItemStack>() {
                        @Override
                        public ItemStack getOutput(IPipe pipe) {
                            if(pipe != null) {
                                return pipe.getTravellingStack(null);
                            }
                            return null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_ITEMSTACK, "contents").build();

        }

    }

}
