package org.cyclops.integrateddynamics.modcompat.tesla.aspect;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.lib.TeslaUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;

/**
 * Builders for RF API aspects
 * @author rubensworks
 */
public class TeslaAspects {

    public static final class Read {

        public static final class Energy {

            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ITeslaConsumer> PROP_GET_RECEIVER = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ITeslaConsumer>() {
                @Override
                public ITeslaConsumer getOutput(Pair<PartTarget, IAspectProperties> input) {
                    DimPos pos = input.getLeft().getTarget().getPos();
                    return TileHelpers.getCapability(pos, input.getLeft().getTarget().getSide(), Capabilities.TESLA_CONSUMER);
                }
            };
            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ITeslaProducer> PROP_GET_PROVIDER = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ITeslaProducer>() {
                @Override
                public ITeslaProducer getOutput(Pair<PartTarget, IAspectProperties> input) {
                    DimPos pos = input.getLeft().getTarget().getPos();
                    return TileHelpers.getCapability(pos, input.getLeft().getTarget().getSide(), Capabilities.TESLA_PRODUCER);
                }
            };
            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ITeslaHolder> PROP_GET_HOLDER = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, ITeslaHolder>() {
                @Override
                public ITeslaHolder getOutput(Pair<PartTarget, IAspectProperties> input) {
                    DimPos pos = input.getLeft().getTarget().getPos();
                    return TileHelpers.getCapability(pos, input.getLeft().getTarget().getSide(), Capabilities.TESLA_HOLDER);
                }
            };

            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, ITeslaConsumer>
                    BUILDER_BOOLEAN_RECEIVER = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_RECEIVER, "tesla");
            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, ITeslaProducer>
                    BUILDER_BOOLEAN_PROVIDER = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_PROVIDER, "tesla");
            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, ITeslaHolder>
                    BUILDER_BOOLEAN_HOLDER = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_HOLDER, "tesla");
            public static final AspectBuilder<ValueTypeLong.ValueLong, ValueTypeLong, ITeslaHolder>
                    BUILDER_LONG_HOLDER = AspectReadBuilders.BUILDER_LONG.handle(PROP_GET_HOLDER, "tesla");
            public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, ITeslaHolder>
                    BUILDER_DOUBLE_HOLDER = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET_HOLDER, "tesla");
            public static final AspectBuilder<ValueTypeString.ValueString, ValueTypeString, ITeslaHolder>
                    BUILDER_STRING_HOLDER = AspectReadBuilders.BUILDER_STRING.handle(PROP_GET_HOLDER, "tesla");

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISAPPLICABLE =
                    BUILDER_BOOLEAN_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, Boolean>() {
                        @Override
                        public Boolean getOutput(ITeslaHolder data) {
                            return data != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISRECEIVER =
                    BUILDER_BOOLEAN_RECEIVER.handle(new IAspectValuePropagator<ITeslaConsumer, Boolean>() {
                        @Override
                        public Boolean getOutput(ITeslaConsumer data) {
                            return data != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isreceiver").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISPROVIDER =
                    BUILDER_BOOLEAN_PROVIDER.handle(new IAspectValuePropagator<ITeslaProducer, Boolean>() {
                        @Override
                        public Boolean getOutput(ITeslaProducer data) {
                            return data != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isprovider").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANEXTRACT =
                    BUILDER_BOOLEAN_PROVIDER.handle(new IAspectValuePropagator<ITeslaProducer, Boolean>() {
                        @Override
                        public Boolean getOutput(ITeslaProducer data) {
                            return data != null && data.takePower(1, true) == 1;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "canextract").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANINSERT =
                    BUILDER_BOOLEAN_RECEIVER.handle(new IAspectValuePropagator<ITeslaConsumer, Boolean>() {
                        @Override
                        public Boolean getOutput(ITeslaConsumer data) {
                            return data != null && data.givePower(1, true) == 1;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "caninsert").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISFULL =
                    BUILDER_BOOLEAN_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, Boolean>() {
                        @Override
                        public Boolean getOutput(ITeslaHolder data) {
                            return data != null && data.getStoredPower() == data.getStoredPower();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isfull").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISEMPTY =
                    BUILDER_BOOLEAN_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, Boolean>() {
                        @Override
                        public Boolean getOutput(ITeslaHolder data) {
                            return data != null && data.getStoredPower() == 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISNONEMPTY =
                    BUILDER_BOOLEAN_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, Boolean>() {
                        @Override
                        public Boolean getOutput(ITeslaHolder data) {
                            return data != null && data.getStoredPower() != 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isnonempty").buildRead();

            public static final IAspectRead<ValueTypeLong.ValueLong, ValueTypeLong> LONG_STORED =
                    BUILDER_LONG_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, Long>() {
                        @Override
                        public Long getOutput(ITeslaHolder data) {
                            return data != null ? data.getStoredPower() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_LONG, "amount").buildRead();
            public static final IAspectRead<ValueTypeLong.ValueLong, ValueTypeLong> LONG_CAPACITY =
                    BUILDER_LONG_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, Long>() {
                        @Override
                        public Long getOutput(ITeslaHolder data) {
                            return data != null ? data.getCapacity() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_LONG, "capacity").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_FILLRATIO =
                    BUILDER_DOUBLE_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, Double>() {
                        @Override
                        public Double getOutput(ITeslaHolder data) {
                            return data != null ? (((double) data.getStoredPower()) / data.getCapacity()) : 0.0D;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

            public static final IAspectRead<ValueTypeString.ValueString, ValueTypeString> STRING_STORED =
                    BUILDER_STRING_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, String>() {
                        @Override
                        public String getOutput(ITeslaHolder data) {
                            return TeslaUtils.getDisplayableTeslaCount(data != null ? data.getStoredPower() : 0);
                        }
                    }).handle(AspectReadBuilders.PROP_GET_STRING, "amount").buildRead();
            public static final IAspectRead<ValueTypeString.ValueString, ValueTypeString> STRING_CAPACITY =
                    BUILDER_STRING_HOLDER.handle(new IAspectValuePropagator<ITeslaHolder, String>() {
                        @Override
                        public String getOutput(ITeslaHolder data) {
                            return TeslaUtils.getDisplayableTeslaCount(data != null ? data.getCapacity() : 0);
                        }
                    }).handle(AspectReadBuilders.PROP_GET_STRING, "capacity").buildRead();

        }

    }

}
