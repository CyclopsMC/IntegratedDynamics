package org.cyclops.integrateddynamics.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.cyclops.cyclopscore.advancement.criterion.ICriterionInstanceTestable;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.advancement.criterion.VariableFacadePredicate;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.logicprogrammer.event.LogicProgrammerVariableFacadeCreatedEvent;

import javax.annotation.Nullable;

/**
 * Triggers when a variable is created.
 * @author rubensworks
 */
public class VariableCreatedTrigger extends SimpleCriterionTrigger<VariableCreatedTrigger.Instance> {
    private final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "variable_created");

    public VariableCreatedTrigger() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, EntityPredicate.Composite entityPredicate, DeserializationContext conditionsParser) {
        JsonElement blockElement = json.get("block");
        Block block = null;
        if (blockElement != null && !blockElement.isJsonNull()) {
            block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(json.get("block").getAsString()));
            if (block == null) {
                throw new JsonSyntaxException("No block found with name: " + json.get("block").getAsString());
            }
        }
        return new Instance(getId(), entityPredicate, block, VariableFacadePredicate.deserialize(ValueDeseralizationContext.ofAllEnabled(), json.get("variable_facade")));
    }

    public void test(ServerPlayer player, LogicProgrammerVariableFacadeCreatedEvent event) {
        this.trigger(player, (instance) -> instance.test(player, event));
    }

    @SubscribeEvent
    public void onEvent(LogicProgrammerVariableFacadeCreatedEvent event) {
        if (event.getPlayer() != null && event.getPlayer() instanceof ServerPlayer) {
            this.test((ServerPlayer) event.getPlayer(), event);
        }
    }

    public static class Instance extends AbstractCriterionTriggerInstance implements ICriterionInstanceTestable<LogicProgrammerVariableFacadeCreatedEvent> {
        private final Block block;
        private final VariableFacadePredicate variableFacadePredicate;

        public Instance(ResourceLocation criterionIn, EntityPredicate.Composite player,
                        @Nullable Block block, VariableFacadePredicate variableFacadePredicate) {
            super(criterionIn, player);
            this.block = block;
            this.variableFacadePredicate = variableFacadePredicate;
        }

        public boolean test(ServerPlayer player, LogicProgrammerVariableFacadeCreatedEvent event) {
            return (block == null || event.getBlockState().getBlock() == block) && variableFacadePredicate.test(event.getVariableFacade());
        }
    }

}
