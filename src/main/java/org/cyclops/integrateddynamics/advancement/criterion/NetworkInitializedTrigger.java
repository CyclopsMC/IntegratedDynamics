package org.cyclops.integrateddynamics.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.cyclopscore.advancement.criterion.BaseCriterionTrigger;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.network.event.NetworkInitializedEvent;

/**
 * Triggers when a network is initialized.
 * @author rubensworks
 */
public class NetworkInitializedTrigger extends BaseCriterionTrigger<NetworkInitializedEvent, NetworkInitializedTrigger.Instance> {
    public NetworkInitializedTrigger() {
        super(new ResourceLocation(Reference.MOD_ID, "network_initialized"));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(getId(), json.get("min_cables").getAsInt());
    }

    @SubscribeEvent
    public void onCrafted(NetworkInitializedEvent event) {
        if (event.getPlacer() != null && event.getPlacer() instanceof ServerPlayerEntity) {
            this.trigger((ServerPlayerEntity) event.getPlacer(), event);
        }
    }

    public static class Instance extends CriterionInstance implements ICriterionInstanceTestable<NetworkInitializedEvent> {
        private final int minCablesCount;

        public Instance(ResourceLocation criterionIn, int minCablesCount) {
            super(criterionIn);
            this.minCablesCount = minCablesCount;
        }

        public boolean test(ServerPlayerEntity player, NetworkInitializedEvent networkEvent) {
            return networkEvent.getNetwork().getCablesCount() >= minCablesCount;
        }
    }

}
