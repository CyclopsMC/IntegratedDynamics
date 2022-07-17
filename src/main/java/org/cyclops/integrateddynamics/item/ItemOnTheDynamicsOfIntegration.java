package org.cyclops.integrateddynamics.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.inventory.container.NamedContainerProviderItem;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.entity.item.EntityItemTargetted;
import org.cyclops.integrateddynamics.inventory.container.ContainerOnTheDynamicsOfIntegration;

import javax.annotation.Nullable;

/**
 * On the Dynamics of Integration book.
 * @author rubensworks
 */
public class ItemOnTheDynamicsOfIntegration extends ItemGui {

    private static final int SPAWN_RANGE = 25;

    public ItemOnTheDynamicsOfIntegration(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
    }

    @Nullable
    @Override
    public MenuProvider getContainer(Level world, Player playerEntity, ItemLocation itemLocation) {
        return new NamedContainerProviderItem(itemLocation,
                Component.translatable("gui.cyclopscore.infobook"), ContainerOnTheDynamicsOfIntegration::new);
    }

    @Override
    public Class<? extends AbstractContainerMenu> getContainerClass(Level world, Player playerEntity, ItemStack itemStack) {
        return ContainerOnTheDynamicsOfIntegration.class;
    }

    private static final String NBT_INFOBOOK_SPAWNED = Reference.MOD_ID + ":infoBookSpawned";
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ItemOnTheDynamicsOfIntegrationConfig.obtainOnSpawn) {
            CompoundTag tag = event.getEntity().getPersistentData();
            if (!tag.contains(Player.PERSISTED_NBT_TAG)) {
                tag.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
            }
            CompoundTag playerTag = tag.getCompound(Player.PERSISTED_NBT_TAG);
            if (!playerTag.contains(NBT_INFOBOOK_SPAWNED)) {
                playerTag.putBoolean(NBT_INFOBOOK_SPAWNED, true);

                Level world = event.getEntity().getCommandSenderWorld();
                Player player = event.getEntity();
                ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_ON_THE_DYNAMICS_OF_INTEGRATION);
                EntityItemTargetted entity = new EntityItemTargetted(world,
                        player.blockPosition().getX() + SPAWN_RANGE - 2 * SPAWN_RANGE * world.random.nextFloat(),
                        player.blockPosition().getY() + SPAWN_RANGE * world.random.nextFloat(),
                        player.blockPosition().getZ() + SPAWN_RANGE - 2 * SPAWN_RANGE * world.random.nextFloat()
                );

                entity.setItem(itemStack);
                entity.setTarget(player);
                world.addFreshEntity(entity);
            }
        }
    }
}
