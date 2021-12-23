package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.functions.ILootFunction;
import org.cyclops.cyclopscore.helper.LootHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.block.BlockMechanicalMachine;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;

/**
 * Copies mechanical machine energy.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalMachineEnergy extends LootFunction {
    public static final LootFunctionType TYPE = LootHelpers.registerFunction(new ResourceLocation(Reference.MOD_ID, "copy_mechanical_machine_energy"), new LootFunctionCopyMechanicalMachineEnergy.Serializer());

    protected LootFunctionCopyMechanicalMachineEnergy(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        TileEntity tile = lootContext.getParamOrNull(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileMechanicalMachine) {
            itemStack.getOrCreateTag().putInt(BlockMechanicalMachine.NBT_ENERGY, ((TileMechanicalMachine) tile).getEnergy());
        }
        return itemStack;
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }

    public static void load() {
        // Dummy call, to enforce class loading
    }

    public static class Serializer extends LootFunction.Serializer<LootFunctionCopyMechanicalMachineEnergy> {
        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyMechanicalMachineEnergy lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyMechanicalMachineEnergy deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, ILootCondition[] conditionsIn) {
            return new LootFunctionCopyMechanicalMachineEnergy(conditionsIn);
        }
    }

}
