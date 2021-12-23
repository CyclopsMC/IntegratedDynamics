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
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.event.PartVariableDrivenVariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.event.PartWriterAspectEvent;

import javax.annotation.Nullable;

/**
 * Triggers when a part writer aspect is set.
 * @author rubensworks
 */
public class PartWriterAspectTrigger extends AbstractCriterionTrigger<PartWriterAspectTrigger.Instance> {
    private final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "part_writer_aspect");

    public PartWriterAspectTrigger() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        return new Instance(getId(), entityPredicate,
                JsonDeserializers.deserializePartType(json),
                JsonDeserializers.deserializeAspect(json),
                VariablePredicate.deserialize(json.get("variable")));
    }

    public void test(ServerPlayerEntity player, PartWriterAspectEvent event) {
        this.trigger(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onEvent(PartWriterAspectEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof ServerPlayerEntity) {
            this.test((ServerPlayerEntity) event.getEntityPlayer(), event);
        }
    }

    public static class Instance extends CriterionInstance implements ICriterionInstanceTestable<PartWriterAspectEvent> {
        private final IPartType partType;
        private final IAspect aspect;
        private final VariablePredicate variablePredicate;

        public Instance(ResourceLocation criterionIn, EntityPredicate.AndPredicate player,
                        @Nullable IPartType partType, @Nullable IAspect aspect,
                        VariablePredicate variablePredicate) {
            super(criterionIn, player);
            this.partType = partType;
            this.aspect = aspect;
            this.variablePredicate = variablePredicate;
        }

        public boolean test(ServerPlayerEntity player, PartWriterAspectEvent event) {
            return (partType == null || event.getPartType() == partType)
                    && (aspect == null || event.getAspect() == aspect)
                    && variablePredicate.test(((IPartStateWriter) event.getPartState()).getVariable(event.getNetwork(), event.getPartNetwork()));
        }
    }

}
