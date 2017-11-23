package org.cyclops.integrateddynamics.core.helper;

import org.cyclops.integrateddynamics.api.APIReference;

/**
 * Collection of generic L10N entries.
 * @author rubensworks
 */
public class L10NValues {

    public static final String NS = APIReference.API_OWNER;

    public static final String GENERAL_ENERGY_UNIT = "general." + NS + ".energy_unit.name";
    public static final String GENERAL_ERROR_NONETWORK = "general." + NS + ".error.no_network";
    public static final String GENERAL_ITEM_ID = "item.items." + NS + ".general.id";
    public static final String GENERAL_TRUE = "general." + NS + ".true";
    public static final String GENERAL_FALSE = "general." + NS + ".false";

    public static final String PART_PANEL_ERROR_INVALIDTYPE = "parttype.parttypes." + NS + ".data_driven_panel.error.invalid_type";
    public static final String PART_ERROR_LOWENERGY = "parttype.parttypes." + NS + ".error.low_energy";
    public static final String PART_TOOLTIP_DISABLED = "parttype.parttypes." + NS + ".tooltip.disabled";
    public static final String PART_TOOLTIP_INACTIVE = "parttype.parttypes." + NS + ".tooltip.inactive";
    public static final String PART_TOOLTIP_ERRORS = "parttype.parttypes." + NS + ".tooltip.errors";
    public static final String PART_TOOLTIP_WRITER_ACTIVEASPECT = "parttype.parttypes." + NS + ".tooltip.writer.active_aspect";
    public static final String PART_TOOLTIP_DISPLAY_ACTIVEVALUE = "parttype.parttypes." + NS + ".tooltip.display.active_value";
    public static final String PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP = "parttype.parttypes." + NS + ".tooltip.monodirectionalconnector.group";

    public static final String GUI_RENAME = "gui." + NS + ".button.rename";
    public static final String GUI_LOGICPROGRAMMER_FILTER = "gui." + NS + ".logicprogrammer.filter";
    public static final String GUI_INPUT = "gui." + NS + ".input";
    public static final String GUI_OUTPUT = "gui." + NS + ".output";
    // 0: value
    public static final String GUI_MECHANICAL_SQUEEZER_TOGGLEFLUIDAUTOEJECT = "gui." + NS + ".mechanical_squeezer.togglefluidautoeject";

    public static final String VALUE_ERROR = "valuetype." + NS + ".error.value";
    public static final String VALUETYPE_VALUETYPE = "valuetype." + NS + ".value_type";
    public static final String VALUETYPE_TOOLTIP_TYPENAME = "valuetype." + NS + ".tooltip.type_name";
    public static final String VALUETYPE_TOOLTIP_VALUE = "valuetype." + NS + ".tooltip.value";
    public static final String VALUETYPEOPERATOR_TOOLTIP_SIGNATURE = "valuetype." + NS + ".operator.tooltip.signature";
    public static final String VALUETYPE_ERROR_INVALIDINPUT = "valuetype." + NS + ".error.invalid_input";
    public static final String VALUETYPE_ERROR_INVALIDINPUTITEM = "valuetype." + NS + ".error.invalid_input_item";
    public static final String VALUETYPE_ERROR_INVALIDLISTELEMENT = "valuetype." + NS + ".error.invalid_list_element";
    // 0: list value type, 1: given type
    public static final String VALUETYPE_ERROR_INVALIDLISTVALUETYPE = "valuetype." + NS + ".error.invalid_list_value_type";
    public static final String VALUETYPE_ERROR_INVALIDOPERATOROPERATOR = "valuetype." + NS + ".error.invalid_operator_operator";
    public static final String VALUETYPE_ERROR_INVALIDOPERATORSIGNATURE = "valuetype." + NS + ".error.invalid_operator_signature";
    public static final String VALUETYPE_OBJECT_BLOCK_ERROR_NOBLOCK = "valuetype." + NS + ".error.block.no_block";
    public static final String VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID = "valuetype." + NS + ".error.fluid.no_fluid";
    public static final String VALUETYPE_OBJECT_THAUMCRAFTASPECT_ERROR_NOASPECT = "valuetype." + NS + ".error.thaumcraftaspect.no_aspect";

    public static final String ASPECT_TOOLTIP_PARTID = "aspect." + NS + ".tooltip.part_id";
    public static final String ASPECT_TOOLTIP_ASPECTNAME = "aspect." + NS + ".tooltip.aspect_name";
    public static final String ASPECT_TOOLTIP_VALUETYPENAME = "aspect." + NS + ".tooltip.value_type_name";
    public static final String ASPECT_ERROR_PARTNOTINNETWORK = "variable." + NS + ".error.part_not_in_network";
    public static final String ASPECT_ERROR_INVALIDTYPE = "aspect." + NS + ".error.invalid_type";

    public static final String PROXY_TOOLTIP_PROXYID = "proxy." + NS + ".tooltip.proxy_id";
    public static final String PROXY_ERROR_PROXYNOTINNETWORK = "proxy." + NS + ".error.proxy_not_in_network";
    public static final String PROXY_ERROR_PROXYINVALID = "proxy." + NS + ".error.proxy_invalid";
    public static final String PROXY_ERROR_PROXYINVALIDTYPE = "proxy." + NS + ".error.proxy_invalid_type";

    public static final String DELAY_TOOLTIP_DELAYID = "delay." + NS + ".tooltip.delay_id";
    public static final String DELAY_ERROR_DELAYNOTINNETWORK = "delay." + NS + ".error.delay_not_in_network";
    public static final String DELAY_ERROR_DELAYINVALID = "delay." + NS + ".error.delay_invalid";
    public static final String DELAY_ERROR_DELAYINVALIDTYPE = "delay." + NS + ".error.delay_invalid_type";

    public static final String VARIABLE_ERROR_INVALIDITEM = "variable." + NS + ".error.invalid_item";
    public static final String VARIABLE_ERROR_PARTNOTINNETWORK = "variable." + NS + ".error.part_not_in_network";
    public static final String VARIABLE_ERROR_RECURSION = "variable." + NS + ".error.recursion";

    public static final String OPERATOR_APPLIED_OPERATORNAME = "operator." + NS + ".applied.operator_name";
    // 0: operator, 1: given-input, 2: position, 3: expected-input
    public static final String OPERATOR_APPLIED_TYPE = "operator." + NS + ".applied.type";
    public static final String OPERATOR_TOOLTIP_OPERATORNAME = "operator." + NS + ".tooltip.operator_name";
    public static final String OPERATOR_TOOLTIP_OPERATORCATEGORY = "operator." + NS + ".tooltip.operator_category";
    public static final String OPERATOR_TOOLTIP_INPUTTYPENAME = "operator." + NS + ".tooltip.input_type_name";
    public static final String OPERATOR_TOOLTIP_OUTPUTTYPENAME = "operator." + NS + ".tooltip.output_type_name";
    public static final String OPERATOR_TOOLTIP_VARIABLEIDS = "operator." + NS + ".tooltip.variable_ids";
    public static final String OPERATOR_ERROR_VARIABLENOTINNETWORK = "operator." + NS + ".error.variable_not_in_network";
    public static final String OPERATOR_ERROR_CYCLICREFERENCE = "operator." + NS + ".error.cyclic_reference";
    public static final String OPERATOR_ERROR_WRONGINPUTLENGTH = "operator." + NS + ".error.wrong_input_length";
    public static final String OPERATOR_ERROR_NULLTYPE = "operator." + NS + ".error.null_type";
    public static final String OPERATOR_ERROR_WRONGTYPE = "operator." + NS + ".error.wrong_type";
    public static final String OPERATOR_ERROR_WRONGCURRYINGTYPE = "operator." + NS + ".error.wrong_currying_type";
    // 0: operator, 1: given-output, 2: expected-output
    public static final String OPERATOR_ERROR_WRONGPREDICATE = "operator." + NS + ".error.wrong_predicate";
    // 0: expected-output, 1:given-output, 2:given-operator
    public static final String OPERATOR_ERROR_ILLEGALPROPERY = "operator." + NS + ".error.illegal_property";

}
