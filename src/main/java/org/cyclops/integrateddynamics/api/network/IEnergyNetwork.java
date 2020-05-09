package org.cyclops.integrateddynamics.api.network;

/**
 * A network capability that holds energy.
 * @author rubensworks
 */
public interface IEnergyNetwork extends IPositionedAddonsNetworkIngredients<Integer, Boolean> {

    /**
     * @return The current network consumption rate.
     */
    public int getConsumptionRate();

}
