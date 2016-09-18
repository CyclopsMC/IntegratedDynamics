package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import lombok.Getter;
import lombok.Setter;
import mcmultipart.MCMultiPartMod;
import mcmultipart.client.multipart.AdvancedParticleManager;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.cyclopscore.block.property.ExtendedBlockStateBuilder;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.IDynamicLight;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.capability.cable.CableConfig;
import org.cyclops.integrateddynamics.capability.cable.CablePartCable;
import org.cyclops.integrateddynamics.capability.dynamiclight.DynamicLightConfig;
import org.cyclops.integrateddynamics.capability.dynamicredstone.DynamicRedstoneConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierConfig;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderPartContainer;
import org.cyclops.integrateddynamics.capability.partcontainer.PartContainerConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementConfig;
import org.cyclops.integrateddynamics.capability.path.PathElementPart;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import java.util.EnumSet;
import java.util.List;

/**
 * A part for cables.
 * @author rubensworks
 */
public class PartCable extends MultipartBase implements ITickable {

    @NBTPersist
    @Getter
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();
    @NBTPersist
    @Getter
    private EnumFacingMap<Boolean> forceDisconnected;
    @NBTPersist
    @Getter
    @Setter
    private int lightLevel = 0;
    @NBTPersist
    @Getter
    @Setter
    private int redstoneLevel = 0;
    @NBTPersist
    @Getter
    @Setter
    private boolean allowsRedstone = false;
    @Getter
    private final PartContainerPartCable partContainer;
    private final ICable cable;
    @Getter
    private final INetworkCarrier networkCarrier;
    private final EnumFacingMap<IDynamicLight> dynamicLights;

    private boolean addSilent = false;
    private boolean sendFurtherUpdates = true;

    public PartCable() {
        this(EnumFacingMap.<PartHelpers.PartStateHolder<?, ?>>newMap(), EnumFacingMap.<Boolean>newMap());
    }

    public PartCable(EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> partData, EnumFacingMap<Boolean> forceDisconnected) {
        partContainer = new PartContainerPartCable(this);
        partContainer.setPartData(partData);
        addCapabilityInternal(PartContainerConfig.CAPABILITY, partContainer);
        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderPartContainer(partContainer));
        cable = new CablePartCable(this);
        addCapabilityInternal(CableConfig.CAPABILITY, cable);
        networkCarrier = new NetworkCarrierDefault();
        addCapabilityInternal(NetworkCarrierConfig.CAPABILITY, networkCarrier);
        addCapabilityInternal(PathElementConfig.CAPABILITY, new PathElementPart(this, cable));
        this.forceDisconnected = forceDisconnected;
        dynamicLights = EnumFacingMap.newMap();
        for (EnumFacing facing : EnumFacing.VALUES) {
            IDynamicLight dynamicLight = new DynamicLightPart(this);
            dynamicLights.put(facing, dynamicLight);
            addCapabilitySided(DynamicLightConfig.CAPABILITY, facing, dynamicLight);
            addCapabilitySided(DynamicRedstoneConfig.CAPABILITY, facing, new DynamicRedstonePart(this));
        }
    }

    /**
     * @return The raw part data.
     */
    public EnumFacingMap<PartHelpers.PartStateHolder<?, ?>> getPartData() {
        return partContainer.getPartData();
    }

    @Override
    protected ItemStack getItemStack() {
        return new ItemStack(BlockCable.getInstance());
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        ExtendedBlockStateBuilder builder = ExtendedBlockStateBuilder.builder((IExtendedBlockState) state);
        for(EnumFacing side : EnumFacing.VALUES) {
            builder.withProperty(BlockCable.CONNECTED[side.ordinal()], cable.isConnected(side));
            boolean hasPart = hasPart(side);
            if(hasPart) {
                builder.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()], getPart(side).getPartRenderPosition());
            } else {
                builder.withProperty(BlockCable.PART_RENDERPOSITIONS[side.ordinal()], PartRenderPosition.NONE);
            }
        }
        return builder.build();
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new ExtendedBlockState(MCMultiPartMod.multipart, new IProperty[0],
                ArrayUtils.addAll(BlockCable.PART_RENDERPOSITIONS, BlockCable.CONNECTED));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return BlockCable.getInstance().getCableBoundingBox(null);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(AdvancedParticleManager advancedParticleManager) {
        advancedParticleManager.addBlockDestroyEffects(getPos(), BlockCable.getInstance().texture);
        return true;
    }

    @Override
    public void addOcclusionBoxes(List<AxisAlignedBB> list) {
        list.add(BlockCable.getInstance().getCableBoundingBox(null));
    }

    @Override
    public void addSelectionBoxes(List<AxisAlignedBB> list) {
        list.add(BlockCable.getInstance().getCableBoundingBox(null));
        for(EnumFacing side : EnumFacing.VALUES) {
            if(cable.isConnected(side)) {
                list.add(BlockCable.getInstance().getCableBoundingBox(side));
            } else if(hasPart(side)) {
                list.add(getPart(side).getPartRenderPosition().getSidedCableBoundingBox(side));
            }
        }
    }

    protected void addCollisionBoxConditional(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, EnumFacing side) {
        AxisAlignedBB box = BlockCable.getInstance().getCableBoundingBox(side);
        if(box.intersectsWith(mask)) {
            list.add(box);
        }
    }

    protected void addCollisionBoxWithPartConditional(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, EnumFacing side) {
        AxisAlignedBB box = getPart(side).getPartRenderPosition().getSidedCableBoundingBox(side);
        if(box.intersectsWith(mask)) {
            list.add(box);
        }
    }

    protected EnumFacing getSubHitSide(int subHit) {
        if(subHit == -1) return null;
        int i = 0;
        for(EnumFacing side : EnumFacing.VALUES) {
            if(cable.isConnected(side)) {
                if(i == subHit) {
                    return side;
                }
                i++;
            }
            if(hasPart(side)) {
                if(i == subHit) {
                    return side;
                }
                i++;
            }
        }
        return null;
    }

    @Override
    public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        addCollisionBoxConditional(mask, list, collidingEntity, null);
        for(EnumFacing side : EnumFacing.VALUES) {
            if(cable.isConnected(side)) {
                addCollisionBoxConditional(mask, list, collidingEntity, side);
            } else if(hasPart(side)) {
                addCollisionBoxWithPartConditional(mask, list, collidingEntity, side);
            }
        }
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public ResourceLocation getModelPath() {
        return new ResourceLocation(IntegratedDynamics._instance.getModId(), BlockCableConfig._instance.getNamedId());
    }

    @Override
    public EnumSet<PartSlot> getSlotMask() {
        return EnumSet.of(PartSlot.CENTER);
    }

    public void detectPresentParts() {
        IMultipartContainer container = getContainer();
        for(IMultipart multipart : container.getParts()) {
            if(multipart instanceof PartPartType) {
                final PartPartType partPart = (PartPartType) multipart;
                PartHelpers.setPart(getNetwork(), getWorld(), getPos(), partPart.getFacing(), partPart.getPartType(), partPart.getPartType().getDefaultState(), new PartHelpers.IPartStateHolderCallback() {
                    @Override
                    public void onSet(PartHelpers.PartStateHolder<?, ?> partStateHolder) {
                        partContainer.getPartData().put(partPart.getFacing(), partStateHolder);
                        sendUpdate();
                    }
                });
            }
        }
    }

    @Override
    public void onAdded() {
        super.onAdded();
        if(!isAddSilent()) {
            CableHelpers.onCableAdded(getWorld(), getPos());
            detectPresentParts();
        }
    }

    @Override
    public void harvest(EntityPlayer player, PartMOP hit) {
        CableHelpers.onCableRemoving(getWorld(), getPos(), false);
        super.harvest(player, hit);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        CableHelpers.onCableRemoved(getWorld(), getPos());
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit) {
        EnumFacing cableConnectionHit = getSubHitSide(hit.subHit);
        if (!getWorld().isRemote) {
            return CableHelpers.onCableActivated(getWorld(), getPos(), BlockCable.getInstance().getDefaultState(),
                    player, player.getHeldItem(hand), hit.sideHit, cableConnectionHit);
        }
        return super.onActivated(player, hand, stack, hit);
    }

    @Override
    public void onNeighborBlockChange(Block neighborBlock) {
        super.onNeighborBlockChange(neighborBlock);
        World world = getWorld();
        BlockPos pos = getPos();
        cable.updateConnections();
        NetworkHelpers.onElementProviderBlockNeighborChange(world, pos, neighborBlock);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setTag("partContainer", partContainer.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("parts", MinecraftHelpers.NBTTag_Types.NBTTagList.ordinal())
                && !tag.hasKey("partContainer", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            // Backwards compatibility with old part saving.
            // TODO: remove in next major MC update.
            PartHelpers.readPartsFromNBT(getNetwork(), getPos(), tag, partContainer.getPartData(), getWorld());
        } else {
            partContainer.deserializeNBT(tag.getCompoundTag("partContainer"));
        }
        super.readFromNBT(tag);
    }

    protected INetwork getNetwork() {
        return networkCarrier.getNetwork();
    }

    protected boolean hasPart(EnumFacing side) {
        return partContainer.hasPart(side);
    }

    protected IPartType getPart(EnumFacing side) {
        return partContainer.getPart(side);
    }

    @Override
    public void onPartChanged(IMultipart part) {
        super.onPartChanged(part);
        if(sendFurtherUpdates) {
            cable.updateConnections();
        }
    }

    @Override
    public void update() {
        if (connected.isEmpty()) {
            cable.updateConnections();
        }
        partContainer.update();
    }

    public void setAddSilent(boolean addSilent) {
        this.addSilent = addSilent;
    }

    public boolean isAddSilent() {
        return this.addSilent;
    }

    public void setSendFurtherUpdates(boolean sendFurtherUpdates) {
        this.sendFurtherUpdates = sendFurtherUpdates;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return super.hasCapability(capability, facing) || partContainer.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        T value = super.getCapability(capability, facing);
        if (value != null) {
            return value;
        }
        return partContainer.getCapability(capability, facing);
    }

    /* --------------- Start IDynamicRedstone --------------- */

    // Not supported by MCMP...

    /* --------------- Start IDynamicLight --------------- */

    @Override
    public int getLightValue() {
        int light = 0;
        for(EnumFacing side : EnumFacing.values()) {
            IDynamicLight dynamicLight = dynamicLights.get(side);
            if (dynamicLight != null) {
                light = Math.max(light, dynamicLight.getLightLevel());
            }
        }
        return light;
    }
}
