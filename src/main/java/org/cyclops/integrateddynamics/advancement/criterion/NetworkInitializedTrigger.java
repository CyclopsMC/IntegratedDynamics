package org.cyclops.integrateddynamics.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.network.event.NetworkInitializedEvent;

/**
 * Triggers when a network is initialized.
 * @author rubensworks
 */
public class NetworkInitializedTrigger extends AbstractCriterionTrigger<NetworkInitializedTrigger.Instance> {
    private final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "network_initialized");

    public NetworkInitializedTrigger() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance deserializeTrigger(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return new Instance(getId(), entityPredicate, json.get("min_cables").getAsInt());
    }

    public void test(ServerPlayerEntity player, NetworkInitializedEvent event) {
        this.triggerListeners(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onCrafted(NetworkInitializedEvent event) {
        if (event.getPlacer() != null && event.getPlacer() instanceof ServerPlayerEntity) {
            this.test((ServerPlayerEntity) event.getPlacer(), event);
        }
    }

    public static class Instance extends CriterionInstance implements ICriterionInstanceTestable<NetworkInitializedEvent> {
        private final int minCablesCount;

        public Instance(ResourceLocation criterionIn, EntityPredicate.AndPredicate player, int minCablesCount) {
            super(criterionIn, player);
            this.minCablesCount = minCablesCount;
        }

        public boolean test(ServerPlayerEntity player, NetworkInitializedEvent networkEvent) {
            return networkEvent.getNetwork().getCablesCount() >= minCablesCount;
        }
    }

}
