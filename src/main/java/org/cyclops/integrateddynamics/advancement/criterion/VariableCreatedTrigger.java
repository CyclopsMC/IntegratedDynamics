package org.cyclops.integrateddynamics.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.cyclopscore.advancement.criterion.BaseCriterionTrigger;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.core.logicprogrammer.event.LogicProgrammerVariableFacadeCreatedEvent;

import javax.annotation.Nullable;

/**
 * Triggers when a variable is created.
 * @author rubensworks
 */
public class VariableCreatedTrigger extends BaseCriterionTrigger<LogicProgrammerVariableFacadeCreatedEvent, VariableCreatedTrigger.Instance> {

    public VariableCreatedTrigger() {
        super(new ResourceLocation(Reference.MOD_ID, "variable_created"));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        JsonElement blockElement = json.get("block");
        Block block = null;
        if (blockElement != null && !blockElement.isJsonNull()) {
            block = Block.REGISTRY.getObject(new ResourceLocation(json.get("block").getAsString()));
            if (block == null) {
                throw new JsonSyntaxException("No block found with name: " + json.get("block").getAsString());
            }
        }
        return new Instance(getId(), block, VariableFacadePredicate.deserialize(json.get("variable_facade")));
    }

    @SubscribeEvent
    public void onEvent(LogicProgrammerVariableFacadeCreatedEvent event) {
        if (event.getPlayer() != null && event.getPlayer() instanceof EntityPlayerMP) {
            this.trigger((EntityPlayerMP) event.getPlayer(), event);
        }
    }

    public static class Instance extends AbstractCriterionInstance implements ICriterionInstanceTestable<LogicProgrammerVariableFacadeCreatedEvent> {
        private final Block block;
        private final VariableFacadePredicate variableFacadePredicate;

        public Instance(ResourceLocation criterionIn, @Nullable Block block, VariableFacadePredicate variableFacadePredicate) {
            super(criterionIn);
            this.block = block;
            this.variableFacadePredicate = variableFacadePredicate;
        }

        public boolean test(EntityPlayerMP player, LogicProgrammerVariableFacadeCreatedEvent event) {
            return (block == null || event.getBlock() == block) && variableFacadePredicate.test(event.getVariableFacade());
        }
    }

}
