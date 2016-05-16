package org.cyclops.integrateddynamics.item;

import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItemFood;
import org.cyclops.cyclopscore.config.configurable.IConfigurable;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Berries.
 * @author rubensworks
 *
 */
public class ItemMenrilBerriesConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemMenrilBerriesConfig _instance;

    /**
     * If the berries should give the night vision effect when eaten.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.ITEM, comment = "If the berries should give the night vision effect when eaten.", requiresMcRestart = true)
    public static boolean nightVision = true;

    /**
     * Make a new instance.
     */
    public ItemMenrilBerriesConfig() {
        super(
                IntegratedDynamics._instance,
                true,
                "menrilBerries",
                null,
                null
        );
    }

    @Override
    protected IConfigurable initSubInstance() {
        ConfigurableItemFood food = new ConfigurableItemFood(this, 4, 0.3F, false) {
            @Override
            public int getMaxItemUseDuration(ItemStack stack) {
                return 10;
            }
        };
        if(nightVision) {
            food = (ConfigurableItemFood) food.setPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 20, 1), 1);
        }
        return food;
    }
    
}
