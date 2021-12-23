package org.cyclops.integrateddynamics.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.cyclops.cyclopscore.inventory.container.NamedContainerProviderItem;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.entity.item.EntityItemTargetted;
import org.cyclops.integrateddynamics.inventory.container.ContainerOnTheDynamicsOfIntegration;

import javax.annotation.Nullable;

import net.minecraft.item.Item.Properties;

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
    public INamedContainerProvider getContainer(World world, PlayerEntity playerEntity, int itemIndex, Hand hand, ItemStack itemStack) {
        return new NamedContainerProviderItem(itemIndex, hand,
                new TranslationTextComponent("gui.cyclopscore.infobook"), ContainerOnTheDynamicsOfIntegration::new);
    }

    @Override
    public Class<? extends Container> getContainerClass(World world, PlayerEntity playerEntity, ItemStack itemStack) {
        return ContainerOnTheDynamicsOfIntegration.class;
    }

    private static final String NBT_INFOBOOK_SPAWNED = Reference.MOD_ID + ":infoBookSpawned";
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ItemOnTheDynamicsOfIntegrationConfig.obtainOnSpawn) {
            CompoundNBT tag = event.getPlayer().getPersistentData();
            if (!tag.contains(PlayerEntity.PERSISTED_NBT_TAG)) {
                tag.put(PlayerEntity.PERSISTED_NBT_TAG, new CompoundNBT());
            }
            CompoundNBT playerTag = tag.getCompound(PlayerEntity.PERSISTED_NBT_TAG);
            if (!playerTag.contains(NBT_INFOBOOK_SPAWNED)) {
                playerTag.putBoolean(NBT_INFOBOOK_SPAWNED, true);

                World world = event.getPlayer().getCommandSenderWorld();
                PlayerEntity player = event.getPlayer();
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
