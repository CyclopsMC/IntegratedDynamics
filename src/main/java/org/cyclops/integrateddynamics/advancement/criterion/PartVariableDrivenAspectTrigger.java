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
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.JsonDeserializers;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;

/**
 * Triggers when a variable-driven part aspect is changed.
 * @author rubensworks
 */
public class PartVariableDrivenAspectTrigger extends AbstractCriterionTrigger<PartVariableDrivenAspectTrigger.Instance> {
    private final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "part_variable_driven");

    public PartVariableDrivenAspectTrigger() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance deserializeTrigger(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return new Instance(getId(), entityPredicate,
                JsonDeserializers.deserializePartType(json), VariablePredicate.deserialize(json.get("variable")));
    }

    public void test(ServerPlayerEntity player, PartVariableDrivenVariableContentsUpdatedEvent event) {
        this.triggerListeners(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onEvent(PartVariableDrivenVariableContentsUpdatedEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof ServerPlayerEntity) {
            this.test((ServerPlayerEntity) event.getEntityPlayer(), event);
        }
    }

    public static class Instance extends CriterionInstance implements ICriterionInstanceTestable<PartVariableDrivenVariableContentsUpdatedEvent> {
        private final IPartType partType;
        private final VariablePredicate variablePredicate;

        public Instance(ResourceLocation criterionIn, EntityPredicate.AndPredicate player,
                        IPartType partType, VariablePredicate variablePredicate) {
            super(criterionIn, player);
            this.partType = partType;
            this.variablePredicate = variablePredicate;
        }

        public boolean test(ServerPlayerEntity player, PartVariableDrivenVariableContentsUpdatedEvent event) {
            return (partType == null || event.getPartType() == partType) && variablePredicate.test(event.getVariable());
        }
    }

}
