package org.cyclops.integrateddynamics.loot.conditions;

import org.cyclops.cyclopscore.config.extendedconfig.LootConditionConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootConditionMatchWrenchConfig extends LootConditionConfig {
    public LootConditionMatchWrenchConfig() {
        super(IntegratedDynamics._instance, "match_wrench", LootConditionMatchWrench.TYPE);
    }
}
