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
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.event.PartWriterAspectEvent;

import java.util.Optional;

/**
 * Triggers when a part writer aspect is set.
 * @author rubensworks
 */
public class PartWriterAspectTrigger extends SimpleCriterionTrigger<PartWriterAspectTrigger.Instance> {

    public static final Codec<PartWriterAspectTrigger.Instance> CODEC = RecordCodecBuilder.create(
            p_311401_ -> p_311401_.group(
                            ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PartWriterAspectTrigger.Instance::player),
                            ExtraCodecs.strictOptionalField(Codecs.PART_TYPE, "parttype").forGetter(PartWriterAspectTrigger.Instance::partType),
                            ExtraCodecs.strictOptionalField(Codecs.ASPECT, "aspect").forGetter(PartWriterAspectTrigger.Instance::aspect),
                            ExtraCodecs.strictOptionalField(Codecs.VARIABLE, "variable").forGetter(PartWriterAspectTrigger.Instance::variablePredicate)
                    )
                    .apply(p_311401_, PartWriterAspectTrigger.Instance::new)
    );

    public PartWriterAspectTrigger() {
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    public void test(ServerPlayer player, PartWriterAspectEvent event) {
        this.trigger(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onEvent(PartWriterAspectEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof ServerPlayer) {
            this.test((ServerPlayer) event.getEntityPlayer(), event);
        }
    }

    public static record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<IPartType> partType,
            Optional<IAspect> aspect,
            Optional<VariablePredicate> variablePredicate
    ) implements SimpleCriterionTrigger.SimpleInstance, ICriterionInstanceTestable<PartWriterAspectEvent> {
        @Override
        public boolean test(ServerPlayer player, PartWriterAspectEvent event) {
            return (partType.isEmpty() || event.getPartType() == partType.get())
                    && (aspect.isEmpty() || event.getAspect() == aspect.get())
                    && variablePredicate.orElse(VariablePredicate.ANY).test(((IPartStateWriter) event.getPartState()).getVariable(event.getNetwork(), event.getPartNetwork(), ValueDeseralizationContext.of(player.level())));
        }
    }

}
