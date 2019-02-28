package org.cyclops.integrateddynamics.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.cyclopscore.advancement.criterion.BaseCriterionTrigger;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.JsonDeserializers;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariablePredicate;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.core.part.event.PartWriterAspectEvent;

import javax.annotation.Nullable;

/**
 * Triggers when a part writer aspect is set.
 * @author rubensworks
 */
public class PartWriterAspectTrigger extends BaseCriterionTrigger<PartWriterAspectEvent, PartWriterAspectTrigger.Instance> {
    public PartWriterAspectTrigger() {
        super(new ResourceLocation(Reference.MOD_ID, "part_writer_aspect"));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(getId(),
                JsonDeserializers.deserializePartType(json),
                JsonDeserializers.deserializeAspect(json),
                VariablePredicate.deserialize(json.get("variable")));
    }

    @SubscribeEvent
    public void onEvent(PartWriterAspectEvent event) {
        if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof EntityPlayerMP) {
            this.trigger((EntityPlayerMP) event.getEntityPlayer(), event);
        }
    }

    public static class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable<PartWriterAspectEvent> {
        private final IPartType partType;
        private final IAspect aspect;
        private final VariablePredicate variablePredicate;

        public Instance(ResourceLocation criterionIn, @Nullable IPartType partType, @Nullable IAspect aspect,
                        VariablePredicate variablePredicate) {
            super(criterionIn);
            this.partType = partType;
            this.aspect = aspect;
            this.variablePredicate = variablePredicate;
        }

        public boolean test(EntityPlayerMP player, PartWriterAspectEvent event) {
            return (partType == null || event.getPartType() == partType)
                    && (aspect == null || event.getAspect() == aspect)
                    && variablePredicate.test(((IPartStateWriter) event.getPartState()).getVariable(event.getTarget(), event.getPartNetwork()));
        }
    }

}
