package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import com.google.common.collect.Lists;
import mcmultipart.client.multipart.AdvancedParticleManager;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.ItemStackHelpers;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.item.ItemBlockCable;

import java.util.EnumSet;
import java.util.List;

/**
 * An McMultiPart part for a {@link org.cyclops.integrateddynamics.api.part.IPartType}.
 * @author rubensworks
 */
public class PartPartType extends MultipartBase {

    private EnumFacing facing;
    private IPartType partType;

    public PartPartType() {

    }

    public PartPartType(EnumFacing facing, IPartType partType) {
        this();
        init(facing, partType);
    }

    public void init(EnumFacing facing, IPartType partType) {
        this.facing = facing;
        this.partType = partType;
    }

    public static ResourceLocation getType(IPartType partType) {
        return partType.getBlockModelPath();
    }

    @Override
    public ResourceLocation getType() {
        return getType(getPartType());
    }

    @Override
    protected ItemStack getItemStack() {
        return new ItemStack(getPartType().getItem());
    }

    @Override
    public List<ItemStack> getDrops() {
        List<ItemStack> drops = Lists.newLinkedList();
        IPartState partState = getDelegatedPartState();
        // partstate can be null if there is not cable in this block
        if(partState != null) {
            getPartType().addDrops(getPartTarget(), partState, drops, true);
        } else {
            drops.add(getItemStack());
        }
        return drops;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if(!tag.getKeySet().isEmpty()) {
            Pair<EnumFacing, IPartType> part = PartHelpers.readPartTypeFromNBT(getNetwork(), getPos(), tag.getCompoundTag("part"));
            if (part != null) {
                this.facing = part.getKey();
                this.partType = part.getValue();
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        NBTTagCompound partTag = new NBTTagCompound();
        PartHelpers.writePartTypeToNBT(partTag, getFacing(), getPartType());
        tag.setTag("part", partTag);
        return tag;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state) {
        return getPartType().getBlockState(getPartContainer(), getFacing());
    }

    @Override
    public BlockStateContainer createBlockState() {
        return getPartType().getBaseBlockState();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getPartType().getRenderPosition().getBoundingBox(getFacing());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(AdvancedParticleManager advancedParticleManager) {
        advancedParticleManager.addBlockDestroyEffects(getPos(), BlockCable.getInstance().texture);
        return true;
    }

    @Override
    public void addOcclusionBoxes(List<AxisAlignedBB> list) {
        list.add(getRenderBoundingBox());
    }

    @Override
    public void addSelectionBoxes(List<AxisAlignedBB> list) {
        list.add(getRenderBoundingBox());
    }

    @Override
    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        AxisAlignedBB boundingBox = getRenderBoundingBox();
        if(mask.intersectsWith(boundingBox)) {
            list.add(boundingBox);
        }
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public ResourceLocation getModelPath() {
        return getPartType().getBlockModelPath();
    }

    @Override
    public EnumSet<PartSlot> getSlotMask() {
        return EnumSet.of(PartSlot.getFaceSlot(getFacing()));
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        CableNetworkComponent.removePartFromNetwork(getWorld(), getPos(), getNetwork(), getFacing(), getPartType());
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
        World world = player.worldObj;
        BlockPos pos = hit.getBlockPos();
        if(!world.isRemote && WrenchHelpers.isWrench(player, heldItem, pos)) {
            // Remove part from cable
            if(player.isSneaking()) {
                PartCable cable = getPartCable();
                if(cable == null) {
                    for(ItemStack itemStack : getDrops()) {
                        ItemStackHelpers.spawnItemStackToPlayer(world, pos, itemStack, player);
                    }
                    getContainer().removePart(this);
                } else {
                    PartHelpers.removePart(world, pos, getFacing(), player, false);
                }
                ItemBlockCable.playBreakSound(world, pos, BlockCable.getInstance().getDefaultState());
            }
            return true;
        } else {
            IPartState partState = getDelegatedPartState();
            if(partState != null) {
                return getPartType().onPartActivated(getWorld(), getPos(), partState,
                        player, hand, heldItem, getFacing(), (float) hit.hitVec.xCoord, (float) hit.hitVec.yCoord, (float) hit.hitVec.zCoord)
                        || super.onActivated(player, hand, heldItem, hit);
            }
        }
        return false;
    }

    public EnumFacing getFacing() {
        return this.facing;
    }

    protected PartTarget getPartTarget() {
        return PartTarget.fromCenter(DimPos.of(getWorld(), getPos()), getFacing());
    }

    protected PartCable getPartCable() {
        IMultipartContainer multipartContainer = getContainer();
        if(multipartContainer != null) {
            IMultipart centerPart = multipartContainer.getPartInSlot(PartSlot.CENTER);
            if (centerPart instanceof PartCable) {
                return (PartCable) centerPart;
            }
        }
        return null;
    }

    public IPartContainer getPartContainer() {
        PartCable partCable = getPartCable();
        if(partCable != null) {
            return partCable.getPartContainer(getWorld(), getPos());
        }
        return null;
    }

    public IPartType getPartType() {
        return this.partType;
    }

    public IPartState getDelegatedPartState() {
        PartCable partCable = getPartCable();
        if(partCable != null) {
            return partCable.getPartContainer().getPartState(getFacing());
        }
        return null;
    }

    public IPartNetwork getNetwork() {
        PartCable partCable = getPartCable();
        if(partCable != null) {
            return partCable.getNetwork();
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        IPartState partState = getDelegatedPartState();
        if (partState != null && getFacing() == facing && partState.hasCapability(capability)) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        IPartState partState = getDelegatedPartState();
        if (partState != null && getFacing() == facing && partState.hasCapability(capability)) {
            return (T) partState.getCapability(capability);
        }
        return super.getCapability(capability, facing);
    }
}
