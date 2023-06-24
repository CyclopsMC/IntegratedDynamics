package org.cyclops.integrateddynamics.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.JsonDeserializers;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.event.PartReaderAspectEvent;

import javax.annotation.Nullable;

/**
 * Triggers when a part reader aspect is set.
 * @author rubensworks
 */
public class PartReaderAspectTrigger extends SimpleCriterionTrigger<PartReaderAspectTrigger.Instance> {
    private final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "part_reader_aspect");

    public PartReaderAspectTrigger() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate entityPredicate, DeserializationContext conditionsParser) {
        return new Instance(getId(), entityPredicate, JsonDeserializers.deserializePartType(json), JsonDeserializers.deserializeAspect(json));
    }

    public void test(ServerPlayer player, PartReaderAspectEvent event) {
        this.trigger(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onEvent(PartReaderAspectEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof ServerPlayer) {
            this.test((ServerPlayer) event.getEntityPlayer(), event);
        }
    }

    public static class Instance extends AbstractCriterionTriggerInstance implements ICriterionInstanceTestable<PartReaderAspectEvent> {
        private final IPartType partType;
        private final IAspect aspect;

        public Instance(ResourceLocation criterionIn, ContextAwarePredicate player, @Nullable IPartType partType, @Nullable IAspect aspect) {
            super(criterionIn, player);
            this.partType = partType;
            this.aspect = aspect;
        }

        public boolean test(ServerPlayer player, PartReaderAspectEvent event) {
            return (partType == null || event.getPartType() == partType)
                    && (aspect == null || event.getAspect() == aspect);
        }
    }

}
