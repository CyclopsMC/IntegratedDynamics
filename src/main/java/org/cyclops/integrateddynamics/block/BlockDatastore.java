package org.cyclops.integrateddynamics.block;

import com.google.common.collect.Sets;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.block.property.BlockProperty;
import org.cyclops.cyclopscore.config.configurable.ConfigurableBlockContainer;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.core.block.cable.CableNetworkComponent;
import org.cyclops.integrateddynamics.core.block.cable.ICable;
import org.cyclops.integrateddynamics.core.block.cable.ICableNetwork;
import org.cyclops.integrateddynamics.core.block.cable.NetworkElementProviderComponent;
import org.cyclops.integrateddynamics.core.network.INetworkElement;
import org.cyclops.integrateddynamics.core.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.path.CablePathElement;
import org.cyclops.integrateddynamics.network.DatastoreNetworkElement;
import org.cyclops.integrateddynamics.tileentity.TileDatastore;

import java.util.Collection;

/**
 * A block that can hold defined variables so that they can be referred to elsewhere in the network.
 *
 * @author rubensworks
 */
public class BlockDatastore extends ConfigurableBlockContainer implements ICableNetwork<CablePathElement>, INetworkElementProvider {

    @BlockProperty
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private static BlockDatastore _instance = null;

    //@Delegate <- Lombok can't handle delegations with generics, so we'll have to do it manually...
    private CableNetworkComponent<BlockDatastore> cableNetworkComponent = new CableNetworkComponent<>(this);
    private NetworkElementProviderComponent networkElementProviderComponent = new NetworkElementProviderComponent(this);

    /**
     * Get the unique instance.
     *
     * @return The instance.
     */
    public static BlockDatastore getInstance() {
        return _instance;
    }

    /**
     * Make a new block instance.
     *
     * @param eConfig Config for this block.
     */
    public BlockDatastore(ExtendedConfig eConfig) {
        super(eConfig, Material.glass, TileDatastore.class);

        setHardness(3.0F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
                                     int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
        cableNetworkComponent.addToNetwork(world, pos);
    }

    @Override
    public boolean saveNBTToDroppedItem() {
        return false;
    }

    @Override
    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        networkElementProviderComponent.onPreBlockDestroyed(world, pos);
        cableNetworkComponent.onPreBlockDestroyed(world, pos);
        super.onPreBlockDestroyed(world, pos);
    }

    @Override
    protected void onPostBlockDestroyed(World world, BlockPos pos) {
        super.onPostBlockDestroyed(world, pos);
        cableNetworkComponent.onPostBlockDestroyed(world, pos);
    }

    @Override
    public Collection<INetworkElement> createNetworkElements(World world, BlockPos blockPos) {
        return Sets.<INetworkElement>newHashSet(new DatastoreNetworkElement(DimPos.of(world, blockPos)));
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
    public void resetCurrentNetwork(World world, BlockPos pos) {
        cableNetworkComponent.resetCurrentNetwork(world, pos);
    }

    @Override
    public void setNetwork(Network network, World world, BlockPos pos) {
        cableNetworkComponent.setNetwork(network, world, pos);
    }

    @Override
    public Network getNetwork(World world, BlockPos pos) {
        return cableNetworkComponent.getNetwork(world, pos);
    }

    @Override
    public CablePathElement createPathElement(World world, BlockPos blockPos) {
        return cableNetworkComponent.createPathElement(world, blockPos);
    }

}
