package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import net.minecraft.particles.ParticleTypes;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An effect writer part.
 * @author rubensworks
 */
public class PartTypeEffectWriter extends PartTypeWriteBase<PartTypeEffectWriter, PartStateWriterBase<PartTypeEffectWriter>> {

    public PartTypeEffectWriter(String name) {
        super(name);
        Aspects.REGISTRY.register(this, Lists.<IAspect>newArrayList(
                Aspects.Write.Effect.createForParticle(ParticleTypes.AMBIENT_ENTITY_EFFECT),
                Aspects.Write.Effect.createForParticle(ParticleTypes.ANGRY_VILLAGER),
                Aspects.Write.Effect.createForParticle(ParticleTypes.BARRIER),
                Aspects.Write.Effect.createForParticle(ParticleTypes.BUBBLE),
                Aspects.Write.Effect.createForParticle(ParticleTypes.CLOUD),
                Aspects.Write.Effect.createForParticle(ParticleTypes.CRIT),
                Aspects.Write.Effect.createForParticle(ParticleTypes.DAMAGE_INDICATOR),
                Aspects.Write.Effect.createForParticle(ParticleTypes.DRAGON_BREATH),
                Aspects.Write.Effect.createForParticle(ParticleTypes.DRIPPING_LAVA),
                Aspects.Write.Effect.createForParticle(ParticleTypes.FALLING_LAVA),
                Aspects.Write.Effect.createForParticle(ParticleTypes.LANDING_LAVA),
                Aspects.Write.Effect.createForParticle(ParticleTypes.DRIPPING_WATER),
                Aspects.Write.Effect.createForParticle(ParticleTypes.FALLING_WATER),
                Aspects.Write.Effect.createForParticle(ParticleTypes.EFFECT),
                Aspects.Write.Effect.createForParticle(ParticleTypes.ELDER_GUARDIAN),
                Aspects.Write.Effect.createForParticle(ParticleTypes.ENCHANTED_HIT),
                Aspects.Write.Effect.createForParticle(ParticleTypes.ENCHANT),
                Aspects.Write.Effect.createForParticle(ParticleTypes.END_ROD),
                Aspects.Write.Effect.createForParticle(ParticleTypes.ENTITY_EFFECT),
                Aspects.Write.Effect.createForParticle(ParticleTypes.EXPLOSION_EMITTER),
                Aspects.Write.Effect.createForParticle(ParticleTypes.EXPLOSION),
                Aspects.Write.Effect.createForParticle(ParticleTypes.FIREWORK),
                Aspects.Write.Effect.createForParticle(ParticleTypes.FISHING),
                Aspects.Write.Effect.createForParticle(ParticleTypes.FLAME),
                Aspects.Write.Effect.createForParticle(ParticleTypes.FLASH),
                Aspects.Write.Effect.createForParticle(ParticleTypes.HAPPY_VILLAGER),
                Aspects.Write.Effect.createForParticle(ParticleTypes.COMPOSTER),
                Aspects.Write.Effect.createForParticle(ParticleTypes.HEART),
                Aspects.Write.Effect.createForParticle(ParticleTypes.INSTANT_EFFECT),
                Aspects.Write.Effect.createForParticle(ParticleTypes.ITEM_SLIME),
                Aspects.Write.Effect.createForParticle(ParticleTypes.ITEM_SNOWBALL),
                Aspects.Write.Effect.createForParticle(ParticleTypes.LARGE_SMOKE),
                Aspects.Write.Effect.createForParticle(ParticleTypes.LAVA),
                Aspects.Write.Effect.createForParticle(ParticleTypes.MYCELIUM),
                Aspects.Write.Effect.createForParticle(ParticleTypes.NOTE),
                Aspects.Write.Effect.createForParticle(ParticleTypes.POOF),
                Aspects.Write.Effect.createForParticle(ParticleTypes.PORTAL),
                Aspects.Write.Effect.createForParticle(ParticleTypes.RAIN),
                Aspects.Write.Effect.createForParticle(ParticleTypes.SMOKE),
                Aspects.Write.Effect.createForParticle(ParticleTypes.SNEEZE),
                Aspects.Write.Effect.createForParticle(ParticleTypes.SPIT),
                Aspects.Write.Effect.createForParticle(ParticleTypes.SQUID_INK),
                Aspects.Write.Effect.createForParticle(ParticleTypes.SWEEP_ATTACK),
                Aspects.Write.Effect.createForParticle(ParticleTypes.TOTEM_OF_UNDYING),
                Aspects.Write.Effect.createForParticle(ParticleTypes.UNDERWATER),
                Aspects.Write.Effect.createForParticle(ParticleTypes.SPLASH),
                Aspects.Write.Effect.createForParticle(ParticleTypes.WITCH),
                Aspects.Write.Effect.createForParticle(ParticleTypes.BUBBLE_POP),
                Aspects.Write.Effect.createForParticle(ParticleTypes.CURRENT_DOWN),
                Aspects.Write.Effect.createForParticle(ParticleTypes.BUBBLE_COLUMN_UP),
                Aspects.Write.Effect.createForParticle(ParticleTypes.NAUTILUS),
                Aspects.Write.Effect.createForParticle(ParticleTypes.DOLPHIN),
                Aspects.Write.Effect.createForParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE),
                Aspects.Write.Effect.createForParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE)
        ));
    }

    @Override
    public PartStateWriterBase<PartTypeEffectWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeEffectWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

}
