package org.cyclops.integrateddynamics;

import org.cyclops.cyclopscore.helper.AdvancementHelpers;
import org.cyclops.integrateddynamics.advancement.criterion.*;

/**
 * Advancement-related logic.
 * @author rubensworks
 */
public class Advancements {

    public static final NetworkInitializedTrigger NETWORK_INITIALIZED = AdvancementHelpers
            .registerCriteriaTrigger(new NetworkInitializedTrigger());
    public static final PartReaderAspectTrigger PART_READER_ASPECT = AdvancementHelpers
            .registerCriteriaTrigger(new PartReaderAspectTrigger());
    public static final PartVariableDrivenAspectTrigger PART_VARIABLE_DRIVEN_ASPECT_TRIGGER = AdvancementHelpers
            .registerCriteriaTrigger(new PartVariableDrivenAspectTrigger());
    public static final PartWriterAspectTrigger PART_WRITER_ASPECT = AdvancementHelpers
            .registerCriteriaTrigger(new PartWriterAspectTrigger());
    public static final VariableCreatedTrigger VARIABLE_CREATED_TRIGGER = AdvancementHelpers
            .registerCriteriaTrigger(new VariableCreatedTrigger());

    public static void load() {}

}
