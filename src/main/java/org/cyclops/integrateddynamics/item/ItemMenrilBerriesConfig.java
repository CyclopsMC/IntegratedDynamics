package org.cyclops.integrateddynamics.item;

import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Berries.
 * @author rubensworks
 *
 */
public class ItemMenrilBerriesConfig extends ItemConfig {

    @ConfigurableProperty(category = "item", comment = "If the berries should give the night vision effect when eaten.", requiresMcRestart = true)
    public static boolean nightVision = true;

    public ItemMenrilBerriesConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_berries",
                eConfig -> new Item(new Item.Properties()
                        .food(createFood())
                        .group(IntegratedDynamics._instance.getDefaultItemGroup()))
        );
    }

    protected static Food createFood() {
        Food.Builder builder = new Food.Builder()
                .hunger(4)
                .saturation(0.3F)
                .fastToEat();
        if (nightVision) {
            builder = builder.effect(new EffectInstance(Effects.NIGHT_VISION, 20, 1), 1);
        }
        return builder.build();
    }
    
}
