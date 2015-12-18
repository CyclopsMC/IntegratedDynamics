package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Sets;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.api.block.IEnergyBattery;
import org.cyclops.integrateddynamics.api.block.IEnergyBatteryFacade;
import org.cyclops.integrateddynamics.api.block.IEnergyContainerBlock;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.path.ICablePathElement;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.block.cable.NetworkElementProviderComponent;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.network.EnergyBatteryNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileEnergyBattery;

import java.util.Collection;

/**
 * A block that holds energy.
 *
 * @author rubensworks
 */
public abstract class BlockEnergyBatteryBase extends ConfigurableBlockContainer implements ICableNetwork<IPartNetwork, ICablePathElement>,
        INetworkElementProvider<IPartNetwork>, IEnergyBatteryFacade, IEnergyContainerBlock {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    //@Delegate <- Lombok can't handle delegations with generics, so we'll have to do it manually...
    private CableNetworkComponent<BlockEnergyBatteryBase> cableNetworkComponent = new CableNetworkComponent<>(this);
    private NetworkElementProviderComponent<IPartNetwork> networkElementProviderComponent = new NetworkElementProviderComponent<>(this);

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockEnergyBatteryBase(ExtendedConfig eConfig) {
        super(eConfig, Material.anvil, TileEnergyBattery.class);

        setHardness(5.0F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && WrenchHelpers.isWrench(player, pos) && player.isSneaking()) {
            destroyBlock(world, pos, true);
            return true;
        }
        return super.onBlockActivated(world, pos, state, player , side, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        cableNetworkComponent.addToNetwork(world, pos);
        TileHelpers.getSafeTile(world, pos, TileEnergyBattery.class).updateBlockState();
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        networkElementProviderComponent.onPreBlockDestroyed(getNetwork(world, pos), world, pos);
        cableNetworkComponent.onPreBlockDestroyed(world, pos);
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    protected void onPostBlockDestroyed(World world, BlockPos pos) {
        super.onPostBlockDestroyed(world, pos);
        cableNetworkComponent.onPostBlockDestroyed(world, pos);
    }

    @Override
    public Collection<INetworkElement<IPartNetwork>> createNetworkElements(World world, BlockPos blockPos) {
        INetworkElement element = new EnergyBatteryNetworkElement(DimPos.of(world, blockPos));
        return Sets.<INetworkElement<IPartNetwork>>newHashSet(element);
    }

    @Override
    public IEnergyBattery getEnergyBattery(World world, BlockPos pos) {
        return TileHelpers.getSafeTile(world, pos, TileEnergyBattery.class);
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
    }

    /* --------------- Delegate to ICableNetwork<CablePathElement> --------------- */

    @Override
    public void initNetwork(World world, BlockPos pos) {
        cableNetworkComponent.initNetwork(world, pos);
    }

    @Override
    public boolean canConnect(World world, BlockPos selfPosition, ICable connector, EnumFacing side) {
        return cableNetworkComponent.canConnect(world, selfPosition, connector, side);
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        cableNetworkComponent.updateConnections(world, pos);
    }

    @Override
    public boolean isConnected(World world, BlockPos pos, EnumFacing side) {
        return cableNetworkComponent.isConnected(world, pos, side);
    }

    @Override
    public void disconnect(World world, BlockPos pos, EnumFacing side) {
        cableNetworkComponent.disconnect(world, pos, side);
    }

    @Override
    public void reconnect(World world, BlockPos pos, EnumFacing side) {
        cableNetworkComponent.reconnect(world, pos, side);
    }

    @Override
    public void resetCurrentNetwork(World world, BlockPos pos) {
        cableNetworkComponent.resetCurrentNetwork(world, pos);
    }

    @Override
    public void setNetwork(IPartNetwork network, World world, BlockPos pos) {
        cableNetworkComponent.setNetwork(network, world, pos);
    }

    @Override
    public IPartNetwork getNetwork(World world, BlockPos pos) {
        return cableNetworkComponent.getNetwork(world, pos);
    }

    @Override
    public CablePathElement createPathElement(World world, BlockPos blockPos) {
        return cableNetworkComponent.createPathElement(world, blockPos);
    }

    @Override
    public String getEneryContainerNBTName() {
        return "energy";
    }

    public abstract boolean isCreative();

}
