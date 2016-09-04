package org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect;

import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueObjectTypeItemStack;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import refinedstorage.api.network.INetworkMaster;
import refinedstorage.api.network.INetworkNode;

import java.util.Collections;

/**
 * Builders for Refined Storage aspects
 * @author rubensworks
 */
public class RefinedStorageAspects {

    public static final class Read {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetworkNode> PROP_GET_NODE = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetworkNode>() {
            @Override
            public INetworkNode getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos pos = input.getLeft().getTarget().getPos();
                return CableHelpers.getInterface(pos, INetworkNode.class);
            }
        };
        public static final IAspectValuePropagator<INetworkNode, INetworkMaster> PROP_GET_MASTER = new IAspectValuePropagator<INetworkNode, INetworkMaster>() {
            @Override
            public INetworkMaster getOutput(INetworkNode input) {
                return input != null ? input.getNetwork() : null;
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, INetworkMaster>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_NODE, "refinedstorage").handle(PROP_GET_MASTER);
        public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, INetworkMaster>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET_NODE, "refinedstorage").handle(PROP_GET_MASTER);

        public static final class Network {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    BUILDER_BOOLEAN.appendKind("network").handle(new IAspectValuePropagator<INetworkMaster, Boolean>() {
                        @Override
                        public Boolean getOutput(INetworkMaster networkMaster) {
                            return networkMaster != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

        }

        public static final class Inventory {

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ITEMSTACKS =
                    BUILDER_LIST.appendKind("inventory").handle(new IAspectValuePropagator<INetworkMaster, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(INetworkMaster networkMaster) {
                            if (networkMaster != null) {
                                return ValueTypeList.ValueList.ofFactory(
                                        new ValueTypeListProxyPositionedNetworkMasterInventory(
                                                DimPos.of(networkMaster.getNetworkWorld(), networkMaster.getPosition())));
                            }
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.<ValueObjectTypeItemStack.ValueItemStack>emptyList());
                        }
                    }, "itemstacks").buildRead();

        }

    }

}
