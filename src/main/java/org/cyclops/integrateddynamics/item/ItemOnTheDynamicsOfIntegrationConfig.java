package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Rarity;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the infobook.
 * @author rubensworks
 */
public class ItemOnTheDynamicsOfIntegrationConfig extends ItemConfigCommon<IModBase> {

    @ConfigurablePropertyCommon(category = "item", comment = "If the info book can give item rewards for tutorial completion.")
    public static boolean bookRewards = true;

    @ConfigurablePropertyCommon(category = "item", comment = "If the info book should automatically obtained when the player first spawns.")
    public static boolean obtainOnSpawn = true;

    public ItemOnTheDynamicsOfIntegrationConfig() {
        super(
                IntegratedDynamics._instance,
                "on_the_dynamics_of_integration",
                (eConfig, properties) -> new ItemOnTheDynamicsOfIntegration(properties
                        .stacksTo(1)
                        .rarity(Rarity.UNCOMMON))
        );
    }

}
