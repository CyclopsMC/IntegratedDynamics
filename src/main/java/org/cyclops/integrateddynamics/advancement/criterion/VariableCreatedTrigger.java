package org.cyclops.integrateddynamics.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.core.helper.Codecs;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.core.logicprogrammer.event.LogicProgrammerVariableFacadeCreatedEvent;

import java.util.Optional;

/**
 * Triggers when a variable is created.
 * @author rubensworks
 */
public class VariableCreatedTrigger extends SimpleCriterionTrigger<VariableCreatedTrigger.Instance> {

    public static final Codec<VariableCreatedTrigger.Instance> CODEC = RecordCodecBuilder.create(
            p_311401_ -> p_311401_.group(
                            ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(VariableCreatedTrigger.Instance::player),
                            ExtraCodecs.strictOptionalField(BuiltInRegistries.BLOCK.byNameCodec(), "block").forGetter(VariableCreatedTrigger.Instance::block),
                            ExtraCodecs.strictOptionalField(Codecs.VARIABLE_FACADE, "variable_facade").forGetter(VariableCreatedTrigger.Instance::variableFacadePredicate)
                    )
                    .apply(p_311401_, VariableCreatedTrigger.Instance::new)
    );

    public VariableCreatedTrigger() {
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public Codec<Instance> codec() {
        return CODEC;
    }

    public void test(ServerPlayer player, LogicProgrammerVariableFacadeCreatedEvent event) {
        this.trigger(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onEvent(LogicProgrammerVariableFacadeCreatedEvent event) {
        if (event.getPlayer() != null && event.getPlayer() instanceof ServerPlayer) {
            this.test((ServerPlayer) event.getPlayer(), event);
        }
    }

    public static record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<Block> block,
            Optional<VariableFacadePredicate> variableFacadePredicate
    ) implements SimpleCriterionTrigger.SimpleInstance, ICriterionInstanceTestable<LogicProgrammerVariableFacadeCreatedEvent> {
        @Override
        public boolean test(ServerPlayer player, LogicProgrammerVariableFacadeCreatedEvent event) {
            return (block.isEmpty() || event.getBlockState().getBlock() == block.get())
                    && variableFacadePredicate.orElse(VariableFacadePredicate.ANY).test(event.getVariableFacade());
        }
    }

}
