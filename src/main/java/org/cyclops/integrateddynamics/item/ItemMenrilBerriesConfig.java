package org.cyclops.integrateddynamics.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.ComposterBlock;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * Config for the Menril Berries.
 * @author rubensworks
 *
 */
public class ItemMenrilBerriesConfig extends ItemConfigCommon<IModBase> {

    @ConfigurablePropertyCommon(category = "item", comment = "If the berries should give the night vision effect when eaten.", requiresMcRestart = true)
    public static boolean nightVision = true;

    public ItemMenrilBerriesConfig() {
        super(
                IntegratedDynamics._instance,
                "menril_berries",
                (eConfig, properties) -> new Item(properties
                        .food(createFood(), createConsumable()))
        );
    }

    protected static FoodProperties createFood() {
        return new FoodProperties.Builder()
                .nutrition(4)
                .saturationModifier(0.3F)
                .build();
    }

    protected static Consumable createConsumable() {
        Consumable.Builder builder = Consumables.defaultFood()
                .consumeSeconds(0.8F);
        if (nightVision) {
            builder = builder.onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20, 1), 1));
        }
        return builder.build();
    }

    @Override
    public void onRegistryRegistered() {
        super.onRegistryRegistered();
        ComposterBlock.COMPOSTABLES.put(getInstance(), 0.65F);
    }

}
