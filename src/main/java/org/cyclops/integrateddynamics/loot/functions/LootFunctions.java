package org.cyclops.integrateddynamics.loot.functions;

import net.minecraft.world.storage.loot.functions.LootFunctionManager;

/**
 * Loot function-related logic.
 * @author rubensworks
 */
public class LootFunctions {

    public static void load() {
        LootFunctionManager.registerFunction(new LootFunctionCopyEnergyBatteryData.Serializer());
        LootFunctionManager.registerFunction(new LootFunctionCopyMechanicalDryingBasinTanks.Serializer());
        LootFunctionManager.registerFunction(new LootFunctionCopyMechanicalMachineEnergy.Serializer());
        LootFunctionManager.registerFunction(new LootFunctionCopyMechanicalSqueezerTank.Serializer());
        LootFunctionManager.registerFunction(new LootFunctionCopyProxyId.Serializer());
    }

}
