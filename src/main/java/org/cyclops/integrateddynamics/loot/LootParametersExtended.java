package org.cyclops.integrateddynamics.loot;

import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootParameter;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;

/**
 * @author rubensworks
 */
public class LootParametersExtended {

    public static final LootParameter<IPartType<?, ?>> PARAM_PART_TYPE = new LootParameter<>(new ResourceLocation(Reference.MOD_ID, "part_type"));
    public static final LootParameter<IPartState<?>> PARAM_PART_STATE = new LootParameter<>(new ResourceLocation(Reference.MOD_ID, "part_tate"));
    public static final LootParameter<Direction> PARAM_SIDE = new LootParameter<>(new ResourceLocation(Reference.MOD_ID, "side"));

    public static final LootParameterSet SET_PART = LootParameterSets.register(Reference.MOD_ID + ":part", (builder) -> {
        builder
                .required(LootParametersExtended.PARAM_PART_TYPE)
                .required(LootParametersExtended.PARAM_PART_STATE)
                .required(LootParameters.POSITION)
                .required(LootParametersExtended.PARAM_SIDE)
                .required(LootParameters.TOOL)
                .optional(LootParameters.THIS_ENTITY)
                .optional(LootParameters.EXPLOSION_RADIUS);
    });

    public static void load() {

    }

}
