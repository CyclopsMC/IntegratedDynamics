package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.collect.Lists;
import lombok.experimental.Delegate;
import mcmultipart.client.multipart.AdvancedEffectRenderer;
import mcmultipart.multipart.INormallyOccludingPart;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.persist.nbt.INBTProvider;
import org.cyclops.cyclopscore.persist.nbt.NBTProviderComponent;
import org.cyclops.integrateddynamics.block.BlockCable;

import java.io.IOException;
import java.util.List;

/**
 * Base implementation of a part.
 * @author rubensworks
 */
public abstract class MultipartBase extends Multipart implements ISlottedPart, INormallyOccludingPart, INBTProvider {

    @Delegate
    private final INBTProvider nbtProvider = new NBTProviderComponent(this);

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
    public boolean addHitEffects(PartMOP partMOP, AdvancedEffectRenderer advancedEffectRenderer) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        nbtProvider.readGeneratedFieldsFromNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        nbtProvider.writeGeneratedFieldsToNBT(tag);
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

    protected void sendUpdate() {
        sendUpdatePacket();
    }

}
