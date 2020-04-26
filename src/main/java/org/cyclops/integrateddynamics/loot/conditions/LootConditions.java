package org.cyclops.integrateddynamics.loot.conditions;

import net.minecraft.world.storage.loot.conditions.LootConditionManager;

/**
 * Loot condition-related logic.
 * @author rubensworks
 */
public class LootConditions {

    public static void load() {
        LootConditionManager.registerCondition(new LootConditionMatchWrench.Serializer());
    }

}
