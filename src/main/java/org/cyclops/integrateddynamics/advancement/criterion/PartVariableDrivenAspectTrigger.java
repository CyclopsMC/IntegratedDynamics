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
import org.cyclops.integrateddynamics.api.advancement.criterion.JsonDeserializers;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;

/**
 * Triggers when a variable-driven part aspect is changed.
 * @author rubensworks
 */
public class PartVariableDrivenAspectTrigger extends BaseCriterionTrigger<PartVariableDrivenVariableContentsUpdatedEvent, PartVariableDrivenAspectTrigger.Instance> {

    public PartVariableDrivenAspectTrigger() {
        super(new ResourceLocation(Reference.MOD_ID, "part_variable_driven"));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(getId(),
                JsonDeserializers.deserializePartType(json), VariablePredicate.deserialize(json.get("variable")));
    }

    @SubscribeEvent
    public void onEvent(PartVariableDrivenVariableContentsUpdatedEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof ServerPlayerEntity) {
            this.trigger((ServerPlayerEntity) event.getEntityPlayer(), event);
        }
    }

    public static class Instance extends CriterionInstance implements ICriterionInstanceTestable<PartVariableDrivenVariableContentsUpdatedEvent> {
        private final IPartType partType;
        private final VariablePredicate variablePredicate;

        public Instance(ResourceLocation criterionIn, IPartType partType, VariablePredicate variablePredicate) {
            super(criterionIn);
            this.partType = partType;
            this.variablePredicate = variablePredicate;
        }

        public boolean test(ServerPlayerEntity player, PartVariableDrivenVariableContentsUpdatedEvent event) {
            return (partType == null || event.getPartType() == partType) && variablePredicate.test(event.getVariable());
        }
    }

}
