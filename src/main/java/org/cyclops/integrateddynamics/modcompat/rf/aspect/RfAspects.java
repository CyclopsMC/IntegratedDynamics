package org.cyclops.integrateddynamics.modcompat.rf.aspect;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;

/**
 * Builders for RF API aspects
 * @author rubensworks
 */
public class RfAspects {

    public static final class Read {

        public static final class Energy {

            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Pair<IEnergyConnection, EnumFacing>> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, Pair<IEnergyConnection, EnumFacing>>() {
                @Override
                public Pair<IEnergyConnection, EnumFacing> getOutput(Pair<PartTarget, IAspectProperties> input) {
                    DimPos pos = input.getLeft().getTarget().getPos();
                    return Pair.of(TileHelpers.getSafeTile(pos, IEnergyConnection.class), input.getLeft().getTarget().getSide());
                }
            };

            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, Pair<IEnergyConnection, EnumFacing>>
                    BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "rf");
            public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, Pair<IEnergyConnection, EnumFacing>>
                    BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "rf");
            public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, Pair<IEnergyConnection, EnumFacing>>
                    BUILDER_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET, "rf");

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISAPPLICABLE =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            return data.getLeft() != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISRECEIVER =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            return data.getLeft() != null && data.getLeft() instanceof IEnergyReceiver;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isreceiver").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISPROVIDER =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            return data.getLeft() != null && data.getLeft() instanceof IEnergyProvider;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isprovider").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANEXTRACT =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            return data.getLeft() != null && data.getLeft() instanceof IEnergyProvider
                                    && ((IEnergyProvider) data.getLeft()).extractEnergy(data.getRight(), 1, true) == 1;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "canextract").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANINSERT =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            return data.getLeft() != null && data.getLeft() instanceof IEnergyReceiver
                                    && ((IEnergyReceiver) data.getLeft()).receiveEnergy(data.getRight(), 1, true) == 1;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "caninsert").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISFULL =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            if(data.getLeft() != null && data.getLeft() instanceof IEnergyProvider) {
                                IEnergyProvider energyProvider = (IEnergyProvider) data.getLeft();
                                EnumFacing side = data.getRight();
                                return energyProvider.getEnergyStored(side) == energyProvider.getMaxEnergyStored(side);
                            }
                            return false;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isfull").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISEMPTY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            if(data.getLeft() != null && data.getLeft() instanceof IEnergyProvider) {
                                IEnergyProvider energyProvider = (IEnergyProvider) data.getLeft();
                                EnumFacing side = data.getRight();
                                return energyProvider.getEnergyStored(side) == 0;
                            }
                            return false;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISNONEMPTY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Boolean>() {
                        @Override
                        public Boolean getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            if(data.getLeft() != null && data.getLeft() instanceof IEnergyProvider) {
                                IEnergyProvider energyProvider = (IEnergyProvider) data.getLeft();
                                EnumFacing side = data.getRight();
                                return energyProvider.getEnergyStored(side) > 0;
                            }
                            return false;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isnonempty").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_STORED =
                    BUILDER_INTEGER.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Integer>() {
                        @Override
                        public Integer getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            IEnergyConnection energyConnection = data.getLeft();
                            EnumFacing side = data.getRight();
                            if (energyConnection != null && energyConnection instanceof IEnergyHandler) {
                                return ((IEnergyReceiver) energyConnection).getEnergyStored(side);
                            }
                            return 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "amount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CAPACITY =
                    BUILDER_INTEGER.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Integer>() {
                        @Override
                        public Integer getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            IEnergyConnection energyConnection = data.getLeft();
                            EnumFacing side = data.getRight();
                            if(energyConnection != null && energyConnection instanceof IEnergyHandler) {
                                return ((IEnergyHandler) energyConnection).getMaxEnergyStored(side);
                            }
                            return 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "capacity").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_FILLRATIO =
                    BUILDER_DOUBLE.handle(new IAspectValuePropagator<Pair<IEnergyConnection, EnumFacing>, Double>() {
                        @Override
                        public Double getOutput(Pair<IEnergyConnection, EnumFacing> data) {
                            if(data.getLeft() != null && data.getLeft() instanceof IEnergyProvider) {
                                IEnergyProvider energyProvider = (IEnergyProvider) data.getLeft();
                                EnumFacing side = data.getRight();
                                double capacity = (double) energyProvider.getMaxEnergyStored(side);
                                if(capacity == 0.0D) {
                                    return 0.0D;
                                }
                                return ((double) energyProvider.getEnergyStored(side)) / capacity;
                            }
                            return 0.0D;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

        }

    }

}
