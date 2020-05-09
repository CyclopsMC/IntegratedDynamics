package org.cyclops.integrateddynamics.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.cyclopscore.item.ItemGui;
import org.cyclops.integrateddynamics.Configs;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.gui.GuiOnTheDynamicsOfIntegration;
import org.cyclops.integrateddynamics.entity.item.EntityItemTargetted;

/**
 * On the Dynamics of Integration book.
 * @author rubensworks
 */
public class ItemOnTheDynamicsOfIntegration extends ItemGui {

    private static final int SPAWN_RANGE = 25;

    private static ItemOnTheDynamicsOfIntegration _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemOnTheDynamicsOfIntegration getInstance() {
        return _instance;
    }

    public ItemOnTheDynamicsOfIntegration(ExtendedConfig<ItemConfig> eConfig) {
        super(eConfig);
        this.setMaxStackSize(1);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public Class<? extends Container> getContainer() {
        // We don't set a container, since this book does not require any server component.
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiOnTheDynamicsOfIntegration.class;
    }

    @Override
    protected boolean isClientSideOnlyGui() {
        return true;
    }

    private static final String NBT_INFOBOOK_SPAWNED = Reference.MOD_ID + ":infoBookSpawned";
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ItemOnTheDynamicsOfIntegrationConfig.obtainOnSpawn
                && Configs.isEnabled(ItemOnTheDynamicsOfIntegrationConfig.class)) {
            NBTTagCompound tag = event.player.getEntityData();
            if (!tag.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
                tag.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
            }
            NBTTagCompound playerTag = tag.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            if (!playerTag.hasKey(NBT_INFOBOOK_SPAWNED)) {
                playerTag.setBoolean(NBT_INFOBOOK_SPAWNED, true);

                World world = event.player.getEntityWorld();
                EntityPlayer player = event.player;
                ItemStack itemStack = new ItemStack(ItemOnTheDynamicsOfIntegration.getInstance());
                EntityItemTargetted entity = new EntityItemTargetted(world,
                        player.getPosition().getX() + SPAWN_RANGE - 2 * SPAWN_RANGE * world.rand.nextFloat(),
                        player.getPosition().getY() + SPAWN_RANGE * world.rand.nextFloat(),
                        player.getPosition().getZ() + SPAWN_RANGE - 2 * SPAWN_RANGE * world.rand.nextFloat()
                );

                entity.setItem(itemStack);
                entity.setTarget(player);
                world.spawnEntity(entity);
            }
        }
    }
}
