package org.cyclops.integrateddynamics.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the infobook.
 * @author rubensworks
 */
public class ItemOnTheDynamicsOfIntegrationConfig extends ItemConfig {

    @ConfigurableProperty(category = "item", comment = "If the info book can give item rewards for tutorial completion.")
    public static boolean bookRewards = true;

    @ConfigurableProperty(category = "item", comment = "If the info book should automatically obtained when the player first spawns.")
    public static boolean obtainOnSpawn = true;

    public ItemOnTheDynamicsOfIntegrationConfig() {
        super(
                IntegratedDynamics._instance,
                "on_the_dynamics_of_integration",
                eConfig -> new ItemOnTheDynamicsOfIntegration(new Item.Properties()
                        .stacksTo(1)
                        .rarity(Rarity.UNCOMMON))
        );
    }

}
