package org.cyclops.integrateddynamics.loot.functions;

/**
 * Loot function-related logic.
 * @author rubensworks
 */
public class LootFunctions {

    public static void load() {
        LootFunctionCopyEnergyBatteryData.load();
        LootFunctionCopyMechanicalDryingBasinTanks.load();
        LootFunctionCopyMechanicalMachineEnergy.load();
        LootFunctionCopyMechanicalSqueezerTank.load();
        LootFunctionCopyProxyId.load();
    }

}
