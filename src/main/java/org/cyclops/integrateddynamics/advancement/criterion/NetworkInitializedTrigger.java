package org.cyclops.integrateddynamics.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.core.network.event.NetworkInitializedEvent;

import java.util.Optional;

/**
 * Triggers when a network is initialized.
 * @author rubensworks
 */
public class NetworkInitializedTrigger extends SimpleCriterionTrigger<NetworkInitializedTrigger.Instance> {

    public static final Codec<NetworkInitializedTrigger.Instance> CODEC = RecordCodecBuilder.create(
            p_311401_ -> p_311401_.group(
                            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(NetworkInitializedTrigger.Instance::player),
                            Codec.INT.optionalFieldOf("min_cables").forGetter(NetworkInitializedTrigger.Instance::minCables)
                    )
                    .apply(p_311401_, NetworkInitializedTrigger.Instance::new)
    );

    public NetworkInitializedTrigger() {
        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public Codec<NetworkInitializedTrigger.Instance> codec() {
        return CODEC;
    }

    public void test(ServerPlayer player, NetworkInitializedEvent event) {
        this.trigger(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onCrafted(NetworkInitializedEvent event) {
        if (event.getPlacer() != null && event.getPlacer() instanceof ServerPlayer) {
            this.test((ServerPlayer) event.getPlacer(), event);
        }
    }

    public static record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<Integer> minCables
    ) implements SimpleCriterionTrigger.SimpleInstance, ICriterionInstanceTestable<NetworkInitializedEvent> {
        @Override
        public boolean test(ServerPlayer player, NetworkInitializedEvent networkEvent) {
            return minCables
                    .map(minCablesCount -> networkEvent.getNetwork().getCablesCount() >= minCablesCount)
                    .orElse(true);
        }
    }
}
