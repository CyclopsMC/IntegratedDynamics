package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import net.minecraft.util.EnumParticleTypes;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An effect writer part.
 * @author rubensworks
 */
public class PartTypeEffectWriter extends PartTypeWriteBase<PartTypeEffectWriter, PartStateWriterBase<PartTypeEffectWriter>> {

    public PartTypeEffectWriter(String name) {
        super(name);
        Aspects.REGISTRY.register(this, Lists.<IAspect>newArrayList(
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.FIREWORKS_SPARK),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.WATER_BUBBLE),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.WATER_SPLASH),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.WATER_WAKE),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SUSPENDED),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SUSPENDED_DEPTH),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.CRIT),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.CRIT_MAGIC),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SMOKE_NORMAL),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SMOKE_LARGE),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SPELL),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SPELL_INSTANT),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SPELL_MOB),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SPELL_MOB_AMBIENT),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SPELL_WITCH),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.DRIP_WATER),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.DRIP_LAVA),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.VILLAGER_ANGRY),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.VILLAGER_HAPPY),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.TOWN_AURA),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.NOTE),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.PORTAL),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.ENCHANTMENT_TABLE),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.FLAME),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.LAVA),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.FOOTSTEP),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.CLOUD),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.REDSTONE),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SNOWBALL),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SNOW_SHOVEL),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.SLIME),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.HEART),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.BARRIER),
                Aspects.Write.Effect.createForParticle(EnumParticleTypes.WATER_DROP)
        ));
    }

    @Override
    public PartStateWriterBase<PartTypeEffectWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeEffectWriter>(Aspects.REGISTRY.getAspects(this).size());
    }
    
    @Override
    public int getConsumptionRate(PartStateWriterBase<PartTypeEffectWriter> state) {
        return GeneralConfig.effectWriterBaseConsumption;
    }

}
