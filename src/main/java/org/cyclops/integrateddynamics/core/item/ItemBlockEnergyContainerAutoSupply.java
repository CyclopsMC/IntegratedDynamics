package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @author rubensworks
 */
public class ItemBlockEnergyContainerAutoSupply extends ItemBlockEnergyContainer {

    public ItemBlockEnergyContainerAutoSupply(Block block, Properties builder) {
        super(block, builder);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(itemStack, context, tooltipDisplay, tooltipAdder, flag);
        IModHelpers.get().getL10NHelpers().addStatusInfo(tooltipAdder, isActivated(itemStack), getDescriptionId() + ".info.auto_supply");
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        return InteractionResult.SUCCESS.heldItemTransformedTo(toggleActivation(player.getItemInHand(hand), world, player));
    }

    public static void autofill(EnergyHandler source, Level world, Entity entity) {
        if(entity instanceof Player && !world.isClientSide()) {
            int tickAmount;
            try (var tx = Transaction.openRoot()) {
                tickAmount = source.extract(Integer.MAX_VALUE, tx);
            }
            if(tickAmount > 0) {
                Player player = (Player) entity;
                for (InteractionHand hand : InteractionHand.values()) {
                    tryFillContainerForPlayer(source, ItemAccess.forPlayerInteraction(player, hand), tickAmount);
                }
            }
        }
    }

    public static boolean tryFillContainerForPlayer(EnergyHandler source, ItemAccess held, int tickAmount) {
        EnergyHandler target = held.getCapability(Capabilities.Energy.ITEM);
        if (target != null) {
            int moved;
            try (var tx = Transaction.openRoot()) {
                moved = EnergyHandlerUtil.move(source, target, tickAmount, tx);
                tx.commit();
            }
            if (moved > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        if (isActivated(itemStack)) {
            EnergyHandler energyStorage = itemStack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(itemStack));
            if (energyStorage != null) {
                autofill(energyStorage, world, entity);
            }
        }
        super.inventoryTick(itemStack, world, entity, slot);
    }

    public ItemStack toggleActivation(ItemStack itemStack, Level world, Player player) {
        if(player.isSecondaryUseActive()) {
            if(!world.isClientSide()) {
                ItemStack activated = itemStack.copy();
                activated.set(RegistryEntries.DATACOMPONENT_ACTIVATED, !activated.getOrDefault(RegistryEntries.DATACOMPONENT_ACTIVATED, false));
                return activated;
            }
            return itemStack;
        }
        return itemStack;
    }

    public boolean isActivated(ItemStack itemStack) {
        return itemStack.getOrDefault(RegistryEntries.DATACOMPONENT_ACTIVATED, false);
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return isActivated(itemStack);
    }
}
