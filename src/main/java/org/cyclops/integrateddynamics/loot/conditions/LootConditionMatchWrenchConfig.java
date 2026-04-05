package org.cyclops.integrateddynamics.loot.conditions;

import org.cyclops.cyclopscore.config.extendedconfig.LootConditionConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * @author rubensworks
 */
public class LootConditionMatchWrenchConfig extends LootConditionConfigCommon<IntegratedDynamics> {
    public LootConditionMatchWrenchConfig() {
        super(IntegratedDynamics._instance, "match_wrench", LootConditionMatchWrench.CODEC);
    }
}
