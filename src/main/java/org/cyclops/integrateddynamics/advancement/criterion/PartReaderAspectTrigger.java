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
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.event.PartReaderAspectEvent;

import javax.annotation.Nullable;

/**
 * Triggers when a part reader aspect is set.
 * @author rubensworks
 */
public class PartReaderAspectTrigger extends BaseCriterionTrigger<PartReaderAspectEvent, PartReaderAspectTrigger.Instance> {
    public PartReaderAspectTrigger() {
        super(new ResourceLocation(Reference.MOD_ID, "part_reader_aspect"));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public Instance deserializeInstance(JsonObject jsonObject, JsonDeserializationContext context) {
        return new Instance(getId(), JsonDeserializers.deserializePartType(jsonObject), JsonDeserializers.deserializeAspect(jsonObject));
    }

    @SubscribeEvent
    public void onEvent(PartReaderAspectEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof ServerPlayerEntity) {
            this.trigger((ServerPlayerEntity) event.getEntityPlayer(), event);
        }
    }

    public static class Instance extends CriterionInstance implements ICriterionInstanceTestable<PartReaderAspectEvent> {
        private final IPartType partType;
        private final IAspect aspect;

        public Instance(ResourceLocation criterionIn, @Nullable IPartType partType, @Nullable IAspect aspect) {
            super(criterionIn);
            this.partType = partType;
            this.aspect = aspect;
        }

        public boolean test(ServerPlayerEntity player, PartReaderAspectEvent event) {
            return (partType == null || event.getPartType() == partType)
                    && (aspect == null || event.getAspect() == aspect);
        }
    }

}
