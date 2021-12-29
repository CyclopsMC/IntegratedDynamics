package org.cyclops.integrateddynamics.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.network.event.NetworkInitializedEvent;

/**
 * Triggers when a network is initialized.
 * @author rubensworks
 */
public class NetworkInitializedTrigger extends SimpleCriterionTrigger<NetworkInitializedTrigger.Instance> {
    private final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "network_initialized");

    public NetworkInitializedTrigger() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, EntityPredicate.Composite entityPredicate, DeserializationContext conditionsParser) {
        return new Instance(getId(), entityPredicate, json.get("min_cables").getAsInt());
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

    public static class Instance extends AbstractCriterionTriggerInstance implements ICriterionInstanceTestable<NetworkInitializedEvent> {
        private final int minCablesCount;

        public Instance(ResourceLocation criterionIn, EntityPredicate.Composite player, int minCablesCount) {
            super(criterionIn, player);
            this.minCablesCount = minCablesCount;
        }

        public boolean test(ServerPlayer player, NetworkInitializedEvent networkEvent) {
            return networkEvent.getNetwork().getCablesCount() >= minCablesCount;
        }
    }

}
