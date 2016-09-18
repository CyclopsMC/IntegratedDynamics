package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.experimental.Delegate;
import mcmultipart.client.multipart.AdvancedParticleManager;
import mcmultipart.multipart.INormallyOccludingPart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTProviderComponent;
import org.cyclops.integrateddynamics.block.BlockCable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of a part.
 * @author rubensworks
 */
public abstract class MultipartBase extends Multipart implements ISlottedPart, INormallyOccludingPart, INBTProvider {

    @Delegate
    private final INBTProvider nbtProvider = new NBTProviderComponent(this);
    private Map<Pair<Capability<?>, EnumFacing>, Object> capabilities = Maps.newHashMap();

    protected abstract ItemStack getItemStack();

    @Override
    public ItemStack getPickBlock(EntityPlayer player, PartMOP hit) {
        return getItemStack();
    }

    @Override
    public List<ItemStack> getDrops() {
        return Lists.newArrayList(getItemStack());
    }

    @Override
    public float getHardness(PartMOP hit) {
        return BlockCable.BLOCK_HARDNESS;
    }

    @Override
    public Material getMaterial() {
        return BlockCable.BLOCK_MATERIAL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(PartMOP partMOP, AdvancedParticleManager advancedParticleManager) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        nbtProvider.readGeneratedFieldsFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        nbtProvider.writeGeneratedFieldsToNBT(tag);
        return tag;
    }

    @Override
    public void writeUpdatePacket(PacketBuffer buf) {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        buf.writeNBTTagCompoundToBuffer(tag);
    }

    @Override
    public void readUpdatePacket(PacketBuffer buf) {
        super.readUpdatePacket(buf);
        try {
            readFromNBT(buf.readNBTTagCompoundFromBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
    }

    public void sendUpdate() {
        sendUpdatePacket();
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if (capabilities instanceof HashMap) {
            capabilities = ImmutableMap.copyOf(capabilities);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return (capabilities != null && capabilities.containsKey(Pair.<Capability<?>, EnumFacing>of(capability, facing)))
                || (facing != null && capabilities != null && capabilities.containsKey(Pair.<Capability<?>, EnumFacing>of(capability, null)))
                || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capabilities != null) {
            Object value = capabilities.get(Pair.<Capability<?>, EnumFacing>of(capability, facing));
            if (value == null && facing != null) {
                value = capabilities.get(Pair.<Capability<?>, EnumFacing>of(capability, null));
            }
            if (value != null) {
                return (T) value;
            }
        }
        return super.getCapability(capability, facing);
    }

    /**
     * Add a sideless capability.
     * This can only be called at tile construction time!
     * @param capability The capability type.
     * @param value The capability.
     * @param <T> The capability type.
     */
    public <T> void addCapabilityInternal(Capability<T> capability, T value) {
        capabilities.put(Pair.<Capability<?>, EnumFacing>of(capability, null), value);
    }

    /**
     * Add a sided capability.
     * This can only be called at tile construction time!
     * @param capability The capability type.
     * @param facing The side for the capability.
     * @param value The capability.
     * @param <T> The capability type.
     */
    public <T> void addCapabilitySided(Capability<T> capability, EnumFacing facing, T value) {
        capabilities.put(Pair.<Capability<?>, EnumFacing>of(capability, facing), value);
    }

    protected Map<Pair<Capability<?>, EnumFacing>, Object> getCapabilities() {
        return capabilities;
    }
}
