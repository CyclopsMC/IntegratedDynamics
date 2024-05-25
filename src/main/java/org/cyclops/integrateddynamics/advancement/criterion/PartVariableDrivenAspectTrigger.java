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
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;

import java.util.Optional;

/**
 * Triggers when a variable-driven part aspect is changed.
 * @author rubensworks
 */
public class PartVariableDrivenAspectTrigger extends SimpleCriterionTrigger<PartVariableDrivenAspectTrigger.Instance> {

    public static final Codec<PartVariableDrivenAspectTrigger.Instance> CODEC = RecordCodecBuilder.create(
            p_311401_ -> p_311401_.group(
                            ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PartVariableDrivenAspectTrigger.Instance::player),
                            ExtraCodecs.strictOptionalField(Codecs.PART_TYPE, "part_type").forGetter(PartVariableDrivenAspectTrigger.Instance::partType),
                            ExtraCodecs.strictOptionalField(Codecs.VARIABLE, "variable").forGetter(PartVariableDrivenAspectTrigger.Instance::variablePredicate)
                    )
                    .apply(p_311401_, PartVariableDrivenAspectTrigger.Instance::new)
    );

    public PartVariableDrivenAspectTrigger() {
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    public void test(ServerPlayer player, PartVariableDrivenVariableContentsUpdatedEvent event) {
        this.trigger(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onEvent(PartVariableDrivenVariableContentsUpdatedEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof ServerPlayer) {
            this.test((ServerPlayer) event.getEntityPlayer(), event);
        }
    }

    public static record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<IPartType> partType,
            Optional<VariablePredicate> variablePredicate
    ) implements SimpleCriterionTrigger.SimpleInstance, ICriterionInstanceTestable<PartVariableDrivenVariableContentsUpdatedEvent> {
        @Override
        public boolean test(ServerPlayer player, PartVariableDrivenVariableContentsUpdatedEvent event) {
            return (partType.isEmpty() || event.getPartType() == partType.get())
                    && variablePredicate.orElse(VariablePredicate.ANY).test(event.getVariable());
        }
    }

}
