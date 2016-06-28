package org.cyclops.integrateddynamics.modcompat.ic2.aspect;

import ic2.api.tile.IEnergyStorage;
import ic2.core.block.TileEntityBlock;
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
import org.cyclops.integrateddynamics.modcompat.ic2.EnergyStorageWrapper;
import org.cyclops.integrateddynamics.modcompat.ic2.EnergyWrapper;
import org.cyclops.integrateddynamics.modcompat.ic2.IEnergyWrapper;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;

/**
 * Builders for IC2 aspects
 * @author rubensworks
 */
public class Ic2Aspects {

    public static final class Read {

        public static final class Energy {

            public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IEnergyWrapper> PROP_GET = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, IEnergyWrapper>() {
                @Override
                public IEnergyWrapper getOutput(Pair<PartTarget, IAspectProperties> input) {
                    DimPos pos = input.getLeft().getTarget().getPos();
                    IEnergyStorage energyStorage = TileHelpers.getSafeTile(pos, IEnergyStorage.class);
                    if (energyStorage != null) {
                        return new EnergyStorageWrapper(energyStorage);
                    }
                    TileEntityBlock tile = TileHelpers.getSafeTile(pos, TileEntityBlock.class);
                    if (tile != null && tile.hasComponent(ic2.core.block.comp.Energy.class)) {
                        return new EnergyWrapper(tile);
                    }
                    return null;
                }
            };

            public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, IEnergyWrapper>
                    BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET, "ic2.eu");
            public static final AspectBuilder<ValueTypeInteger.ValueInteger, ValueTypeInteger, IEnergyWrapper>
                    BUILDER_INTEGER = AspectReadBuilders.BUILDER_INTEGER.handle(PROP_GET, "ic2.eu");
            public static final AspectBuilder<ValueTypeDouble.ValueDouble, ValueTypeDouble, IEnergyWrapper>
                    BUILDER_DOUBLE = AspectReadBuilders.BUILDER_DOUBLE.handle(PROP_GET, "ic2.eu");

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISAPPLICABLE =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyWrapper, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyWrapper data) {
                            return data != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANEXTRACT =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyWrapper, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyWrapper data) {
                            return data != null && data.getStored() > 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "canextract").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_CANINSERT =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyWrapper, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyWrapper data) {
                            return data != null && data.getStored() < data.getCapacity();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "caninsert").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISFULL =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyWrapper, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyWrapper data) {
                            return data != null && data.getStored() == data.getCapacity();
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isfull").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISEMPTY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyWrapper, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyWrapper data) {
                            return data != null && data.getStored() == 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isempty").buildRead();
            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_ISNONEMPTY =
                    BUILDER_BOOLEAN.handle(new IAspectValuePropagator<IEnergyWrapper, Boolean>() {
                        @Override
                        public Boolean getOutput(IEnergyWrapper data) {
                            return data != null && data.getStored() > 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "isnonempty").buildRead();

            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_STORED =
                    BUILDER_INTEGER.handle(new IAspectValuePropagator<IEnergyWrapper, Integer>() {
                        @Override
                        public Integer getOutput(IEnergyWrapper data) {
                            return data != null ? data.getStored() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "amount").buildRead();
            public static final IAspectRead<ValueTypeInteger.ValueInteger, ValueTypeInteger> INTEGER_CAPACITY =
                    BUILDER_INTEGER.handle(new IAspectValuePropagator<IEnergyWrapper, Integer>() {
                        @Override
                        public Integer getOutput(IEnergyWrapper data) {
                            return data != null ? data.getCapacity() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_INTEGER, "capacity").buildRead();

            public static final IAspectRead<ValueTypeDouble.ValueDouble, ValueTypeDouble> DOUBLE_FILLRATIO =
                    BUILDER_DOUBLE.handle(new IAspectValuePropagator<IEnergyWrapper, Double>() {
                        @Override
                        public Double getOutput(IEnergyWrapper data) {
                            return data != null ? (double) data.getStored() / data.getCapacity() : 0;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_DOUBLE, "fillratio").buildRead();

        }

    }

}
