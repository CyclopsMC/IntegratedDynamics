package org.cyclops.integrateddynamics.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.core.helper.Codecs;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.event.PartReaderAspectEvent;

import java.util.Optional;

/**
 * Triggers when a part reader aspect is set.
 * @author rubensworks
 */
public class PartReaderAspectTrigger extends SimpleCriterionTrigger<PartReaderAspectTrigger.Instance> {

    public static final Codec<PartReaderAspectTrigger.Instance> CODEC = RecordCodecBuilder.create(
            p_311401_ -> p_311401_.group(
                            ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PartReaderAspectTrigger.Instance::player),
                            ExtraCodecs.strictOptionalField(Codecs.PART_TYPE, "parttype").forGetter(PartReaderAspectTrigger.Instance::partType),
                            ExtraCodecs.strictOptionalField(Codecs.ASPECT, "aspect").forGetter(PartReaderAspectTrigger.Instance::aspect)
                    )
                    .apply(p_311401_, PartReaderAspectTrigger.Instance::new)
    );

    public PartReaderAspectTrigger() {
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    public void test(ServerPlayer player, PartReaderAspectEvent event) {
        this.trigger(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onEvent(PartReaderAspectEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof ServerPlayer) {
            this.test((ServerPlayer) event.getEntityPlayer(), event);
        }
    }

    public static record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<IPartType> partType,
            Optional<IAspect> aspect
    ) implements SimpleCriterionTrigger.SimpleInstance, ICriterionInstanceTestable<PartReaderAspectEvent> {
        @Override
        public boolean test(ServerPlayer player, PartReaderAspectEvent event) {
            return (partType.isEmpty() || event.getPartType() == partType.get())
                    && (aspect.isEmpty() || event.getAspect() == aspect.get());
        }
    }

}
