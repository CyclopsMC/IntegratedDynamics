package org.cyclops.integrateddynamics.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ComposterBlock;
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
                        .food(createFood()))
        );
    }

    protected static FoodProperties createFood() {
        FoodProperties.Builder builder = new FoodProperties.Builder()
                .nutrition(4)
                .saturationModifier(0.3F)
                .fast();
        if (nightVision) {
            builder = builder.effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20, 1), 1);
        }
        return builder.build();
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        ComposterBlock.COMPOSTABLES.put(getInstance(), 0.65F);
    }

}
