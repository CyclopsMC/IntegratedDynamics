package org.cyclops.integrateddynamics.core.helper;

import org.cyclops.integrateddynamics.api.APIReference;

/**
 * Collection of generic L10N entries.
 * @author rubensworks
 */
public class L10NValues {

    public static final String NS = APIReference.API_OWNER;

    public static final String GENERAL_ENERGY_UNIT = "general." + NS + ".energy_unit";
    public static final String GENERAL_ERROR_NONETWORK = "general." + NS + ".error.no_network";
    public static final String GENERAL_ITEM_ID = "item." + NS + ".general.id";
    public static final String GENERAL_TRUE = "general." + NS + ".true";
    public static final String GENERAL_FALSE = "general." + NS + ".false";

    public static final String PART_PANEL_ERROR_INVALIDTYPE = "parttype." + NS + ".data_driven_panel.error.invalid_type";
    public static final String PART_ERROR_LOWENERGY = "parttype." + NS + ".error.low_energy";
    public static final String PART_TOOLTIP_DISABLED = "parttype." + NS + ".tooltip.disabled";
    public static final String PART_TOOLTIP_INACTIVE = "parttype." + NS + ".tooltip.inactive";
    public static final String PART_TOOLTIP_ERRORS = "parttype." + NS + ".tooltip.errors";
    public static final String PART_TOOLTIP_WRITER_ACTIVEASPECT = "parttype." + NS + ".tooltip.writer.active_aspect";
    public static final String PART_TOOLTIP_DISPLAY_ACTIVEVALUE = "parttype." + NS + ".tooltip.display.active_value";
    public static final String PART_TOOLTIP_MONODIRECTIONALCONNECTOR_GROUP = "parttype." + NS + ".tooltip.monodirectionalconnector.group";
    public static final String PART_TOOLTIP_NOASPECTS = "parttype." + NS + ".tooltip.noaspects";
    public static final String PART_TOOLTIP_MAXOFFSET = "parttype." + NS + ".tooltip.maxoffset";

    public static final String GUI_RENAME = "gui." + NS + ".button.rename";
    public static final String GUI_LOGICPROGRAMMER_FILTER = "gui." + NS + ".logicprogrammer.filter";
    public static final String GUI_INPUT = "gui." + NS + ".input";
    public static final String GUI_OUTPUT = "gui." + NS + ".output";
    public static final String GUI_RECIPE_STRICTNBT = "gui." + NS + ".recipe.strictnbt";
    public static final String GUI_RECIPE_TAGVARIANTS = "gui." + NS + ".recipe.tagvariants";
    public static final String GUI_RECIPE_REUSABLE = "gui." + NS + ".recipe.reusable";
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
    // 0: value, 1: error
    public static final String VALUETYPE_ERROR_DESERIALIZE = "valuetype." + NS + ".error.deserialize";
    // 0: list value type, 1: given type
    public static final String VALUETYPE_ERROR_INVALIDLISTVALUETYPE = "valuetype." + NS + ".error.invalid_list_value_type";
    public static final String VALUETYPE_ERROR_INVALIDOPERATOROPERATOR = "valuetype." + NS + ".error.invalid_operator_operator";
    public static final String VALUETYPE_ERROR_INVALIDOPERATORSIGNATURE = "valuetype." + NS + ".error.invalid_operator_signature";
    public static final String VALUETYPE_OBJECT_BLOCK_ERROR_NOBLOCK = "valuetype." + NS + ".error.block.no_block";
    public static final String VALUETYPE_OBJECT_FLUID_ERROR_NOFLUID = "valuetype." + NS + ".error.fluid.no_fluid";
    // 0: value type
    public static final String VALUETYPE_ERROR_NOLIGHTCALCULATOR = "valuetype." + NS + ".error.no_light_calculator";

    public static final String ASPECT_TOOLTIP_PARTID = "aspect." + NS + ".tooltip.part_id";
    public static final String ASPECT_TOOLTIP_ASPECTNAME = "aspect." + NS + ".tooltip.aspect_name";
    public static final String ASPECT_TOOLTIP_VALUETYPENAME = "aspect." + NS + ".tooltip.value_type_name";
    public static final String ASPECT_ERROR_PARTNOTINNETWORK = "variable." + NS + ".error.part_not_in_network";
    public static final String ASPECT_ERROR_INVALIDTYPE = "aspect." + NS + ".error.invalid_type";
    public static final String ASPECT_ERROR_NOVALUEINTERFACE = "aspect." + NS + ".error.no_value_interface";
    public static final String ASPECT_ERROR_NOVALUEINTERFACEVALUE = "aspect." + NS + ".error.no_value_interface_value";
    public static final String ASPECT_ERROR_RECURSION = "aspect." + NS + ".error.recursion";

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
    // 0: operator, 1: given-length, 2: actual-length
    public static final String OPERATOR_ERROR_WRONGINPUTLENGTH = "operator." + NS + ".error.wrong_input_length";
    // 0: required-input-lengt,; 1: operator, 2: actual-input-length
    public static final String OPERATOR_ERROR_OPERATORPARAMWRONGINPUTLENGTH = "operator." + NS + ".error.operator_param_wrong_input_length";
    public static final String OPERATOR_ERROR_NULLTYPE = "operator." + NS + ".error.null_type";
    // 0: operator, 1: given-input, 2: position, 3: expected-input
    public static final String OPERATOR_ERROR_WRONGTYPE = "operator." + NS + ".error.wrong_type";
    // 0: operator, 1: returned-input, 3: expected-output
    public static final String OPERATOR_ERROR_WRONGTYPEOUTPUT = "operator." + NS + ".error.wrong_type_output";
    public static final String OPERATOR_ERROR_WRONGCURRYINGTYPE = "operator." + NS + ".error.wrong_currying_type";
    // 0: operator, 1: operator-inputs, 2: actual-inputs, 3: operator-output
    public static final String OPERATOR_ERROR_CURRYINGOVERFLOW = "operator." + NS + ".error.currying_overflow";
    // 0: operator, 1: given-output, 2: expected-output
    public static final String OPERATOR_ERROR_WRONGPREDICATE = "operator." + NS + ".error.wrong_predicate";
    // 0: expected-output, 1:given-output, 2:given-operator
    public static final String OPERATOR_ERROR_ILLEGALPROPERY = "operator." + NS + ".error.illegal_property";
    // 0: operator, 1: inner-operator, 2: given-length, 3: actual-length
    public static final String OPERATOR_ERROR_WRONGINPUTLENGTHVIRTIUAL = "operator." + NS + ".error.wrong_input_length_virtual";
    // 0: limit, 1: operator
    public static final String OPERATOR_ERROR_RECURSIONLIMIT = "operator." + NS + ".error.operator_recursion_limit";
    // 0: expression, 1: message
    public static final String OPERATOR_ERROR_NBT_PATH_EXPRESSION = "operator." + NS + ".error.operator_nbt_path_expression";
    // 0: value-type-from, 1: value-type-to
    public static final String OPERATOR_ERROR_CAST_NOMAPPING = "operator." + NS + ".error.cast.no_mapping";
    // 0: value-type-from, 1: value-type-to, 2: value
    public static final String OPERATOR_ERROR_CAST_ILLEGAL = "operator." + NS + ".error.cast.illegal";
    // 0: value-type-from-actual, 1: value-type-from-expected, 2: value-type-to
    public static final String OPERATOR_ERROR_CAST_UNEXPECTED = "operator." + NS + ".error.cast.unexpected";
    public static final String OPERATOR_ERROR_DIVIDEBYZERO = "operator." + NS + ".error.divide_by_zero";
    // 0: string-value, 1: value-type
    public static final String OPERATOR_ERROR_PARSE = "operator." + NS + ".error.parse";
    // 0: value
    public static final String OPERATOR_ERROR_NO_DESERIALIZER = "operator." + NS + ".error.no_derserializer";
    // 0: regex-value
    public static final String OPERATOR_ERROR_REGEX_INVALID = "operator." + NS + ".error.regex.invalid";
    public static final String OPERATOR_ERROR_SUBSTRING_TOGREATERTHANFROM = "operator." + NS + ".error.substring.to_greater_than_from";
    public static final String OPERATOR_ERROR_SUBSTRING_INDEXNEGATIVE = "operator." + NS + ".error.substring.index_negative";
    public static final String OPERATOR_ERROR_SUBSTRING_LONGERTHANSTRING = "operator." + NS + ".error.substring.longer_than_string";
    public static final String OPERATOR_ERROR_GROUP_INDEXNEGATIVE = "operator." + NS + ".error.group.index_negative";
    // 0: regex-value, 1: value
    public static final String OPERATOR_ERROR_GROUP_NOMATCH = "operator." + NS + ".error.group.no_match";
    // 0: regex-value, 1: value, 2: group
    public static final String OPERATOR_ERROR_GROUP_NOMATCHGROUP = "operator." + NS + ".error.group.no_match_group";
    public static final String OPERATOR_ERROR_REGEXSCAN_INDEXNEGATIVE = "operator." + NS + ".error.regex_scan.index_negative";
    // 0: regex-value, 1: value, 2: group
    public static final String OPERATOR_ERROR_REGEXSCAN_NOMATCHGROUP = "operator." + NS + ".error.regex_scan.no_match_group";
    // 0: operation
    public static final String OPERATOR_ERROR_INFINITELIST_ILLEGAL = "operator." + NS + ".error.infinite_list.illegal";
    // 0: index, 1: length
    public static final String OPERATOR_ERROR_INDEXOUTOFBOUNDS = "operator." + NS + ".error.index_out_of_bounds";
    public static final String OPERATOR_ERROR_SLICE_TOGREATERTHANFROM = "operator." + NS + ".error.slice.to_greater_than_from";
    public static final String OPERATOR_ERROR_SLICE_INDEXNEGATIVE = "operator." + NS + ".error.slice.index_negative";
    // 0: operator
    public static final String OPERATOR_ERROR_OPERATORNOTFOUND = "operator." + NS + ".error.operator_not_found";
    public static final String OPERATOR_ERROR_REDUCE_EMPTY = "operator." + NS + ".error.reduce.empty";

}
