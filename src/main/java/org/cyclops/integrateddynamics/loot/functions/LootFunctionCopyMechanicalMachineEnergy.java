package org.cyclops.integrateddynamics.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.block.BlockMechanicalMachine;
import org.cyclops.integrateddynamics.core.tileentity.TileMechanicalMachine;

/**
 * Copies mechanical machine energy.
 * @author rubensworks
 */
public class LootFunctionCopyMechanicalMachineEnergy implements ILootFunction {

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        TileEntity tile = lootContext.get(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileMechanicalMachine) {
            itemStack.getOrCreateTag().putInt(BlockMechanicalMachine.NBT_ENERGY, ((TileMechanicalMachine) tile).getEnergy());
        }
        return itemStack;
    }

    public static class Serializer extends ILootFunction.Serializer<LootFunctionCopyMechanicalMachineEnergy> {

        public Serializer() {
            super(new ResourceLocation(Reference.MOD_ID, "copy_mechanical_machine_energy"), LootFunctionCopyMechanicalMachineEnergy.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LootFunctionCopyMechanicalMachineEnergy lootFunctionCopyId, JsonSerializationContext jsonSerializationContext) {

        }

        @Override
        public LootFunctionCopyMechanicalMachineEnergy deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new LootFunctionCopyMechanicalMachineEnergy();
        }
    }

}
