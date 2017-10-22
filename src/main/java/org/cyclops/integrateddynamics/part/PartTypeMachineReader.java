package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * A machine reader part.
 * @author rubensworks
 */
public class PartTypeMachineReader extends PartTypeReadBase<PartTypeMachineReader, PartStateReaderBase<PartTypeMachineReader>> {

    public PartTypeMachineReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Read.Machine.BOOLEAN_ISWORKER,
                Aspects.Read.Machine.BOOLEAN_HASWORK,
                Aspects.Read.Machine.BOOLEAN_CANWORK,
                Aspects.Read.Machine.BOOLEAN_ISWORKING,
                Aspects.Read.Machine.BOOLEAN_ISTEMPERATURE,
                Aspects.Read.Machine.DOUBLE_TEMPERATURE,
                Aspects.Read.Machine.DOUBLE_MAXTEMPERATURE,
                Aspects.Read.Machine.DOUBLE_MINTEMPERATURE,
                Aspects.Read.Machine.DOUBLE_DEFAULTTEMPERATURE,
                Aspects.Read.Machine.BOOLEAN_ISRECIPEHANDLER,
                Aspects.Read.Machine.LIST_GETRECIPES,
                Aspects.Read.Machine.OPERATOR_GETRECIPEOUTPUT,
                Aspects.Read.Machine.OPERATOR_GETRECIPEINPUTS,
                Aspects.Read.Machine.OPERATOR_GETRECIPESBYINPUT,
                Aspects.Read.Machine.OPERATOR_GETRECIPESBYOUTPUT,
                Aspects.Read.Machine.BOOLEAN_ISENERGY,
                Aspects.Read.Machine.BOOLEAN_ISENERGYRECEIVER,
                Aspects.Read.Machine.BOOLEAN_ISENERGYPROVIDER,
                Aspects.Read.Machine.BOOLEAN_CANEXTRACTENERGY,
                Aspects.Read.Machine.BOOLEAN_CANINSERTENERGY,
                Aspects.Read.Machine.BOOLEAN_ISENERGYFULL,
                Aspects.Read.Machine.BOOLEAN_ISENERGYEMPTY,
                Aspects.Read.Machine.BOOLEAN_ISENERGYNONEMPTY,
                Aspects.Read.Machine.INTEGER_ENERGYSTORED,
                Aspects.Read.Machine.INTEGER_ENERGYCAPACITY,
                Aspects.Read.Machine.DOUBLE_ENERGYFILLRATIO
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeMachineReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeMachineReader>();
    }

}
