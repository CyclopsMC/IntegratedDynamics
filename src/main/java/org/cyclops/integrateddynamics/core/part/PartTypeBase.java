package org.cyclops.integrateddynamics.core.part;

import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.gui.GuiPartReader;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.network.INetworkElement;
import org.cyclops.integrateddynamics.core.network.PartNetworkElement;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartReader;

import java.util.List;
import java.util.Set;

/**
 * An abstract {@link org.cyclops.integrateddynamics.core.part.IPartType} with a default implementation for creating
 * network elements.
 * @author rubensworks
 */
public abstract class PartTypeBase<P extends IPartType<P, S>, S extends IPartState<P>> implements IPartType<P, S>,
        IGuiContainerProvider {

    @Getter
    private Item item = null;
    @Getter
    private final int guiID;
    @Getter
    private final String name;

    public PartTypeBase(String name) {
        if(hasGui()) {
            this.guiID = Helpers.getNewId(getMod(), Helpers.IDType.GUI);
            getMod().getGuiHandler().registerGUI(this, ExtendedGuiHandler.PART);
        } else {
            this.guiID = -1;
        }
        this.name = name;
    }

    @Override
    public String getUnlocalizedName() {
        return "parttype.parttypes." + Reference.MOD_ID + "." + getName();
    }

    @Override
    public Set<IAspect> getAspects() {
        return AspectRegistry.getInstance().getAspects(this);
    }

    @Override
    public INetworkElement createNetworkElement(IPartContainerFacade partContainerFacade, DimPos pos, EnumFacing side) {
        return new PartNetworkElement(this, partContainerFacade, PartTarget.fromCenter(pos, side));
    }

    @Override
    public ItemStack getItemStack(S state) {
        NBTTagCompound tag = new NBTTagCompound();
        toNBT(tag, state);
        ItemStack itemStack = new ItemStack(getItem());
        itemStack.setTagCompound(tag);
        return itemStack;
    }

    @Override
    public boolean isUpdate(S state) {
        return !getAspects().isEmpty();
    }

    @Override
    public void update(PartTarget target, S state) {
        for(IAspect aspect : getAspects()) {
            IAspectVariable variable = getVariable(target, state, aspect);
            if(variable.requiresUpdate()) {
                variable.update();
            }
        }
    }

    @Override
    public S getState(ItemStack itemStack) {
        S partState = null;
        if(itemStack != null && itemStack.getTagCompound() != null) {
            partState = fromNBT(itemStack.getTagCompound());
        }
        if(partState == null) {
            partState = getDefaultState();
        }
        return partState;
    }

    @Override
    public void addDrops(S state, List<ItemStack> itemStacks) {
        itemStacks.add(getItemStack(state));
    }

    public void setItem(Item item) {
        if(this.item != null) {
            throw new IllegalStateException(String.format("Could not set the new item %s in %s with the already set " +
                    "item %s.", this.item, this, item));
        }
        this.item = item;
    }

    @Override
    public IAspectVariable getVariable(PartTarget target, S partState, IAspect aspect) {
        if(!getAspects().contains(aspect)) {
            throw new IllegalArgumentException("Tried to get the variable for an aspect that did not exist within a " +
                    "part type.");
        }
        IAspectVariable variable = partState.getVariable(aspect);
        if(variable == null) {
            variable = aspect.createNewVariable(target);
            partState.setVariable(aspect, variable);
        }
        return variable;
    }

    @Override
    public void toNBT(NBTTagCompound tag, S partState) {
        partState.writeToNBT(tag);
    }

    @Override
    public S fromNBT(NBTTagCompound tag) {
        S partState = constructDefaultState();
        partState.readFromNBT(tag);
        return partState;
    }

    /**
     * @return Constructor call for a new default state for this part type.
     */
    protected abstract S constructDefaultState();

    @Override
    public final S getDefaultState() {
        S defaultState = constructDefaultState();
        defaultState.generateId();
        return defaultState;
    }

    @Override
    public void beforeNetworkKill(S state) {
        System.out.println("killing " + state);
    }

    @Override
    public void afterNetworkAlive(S state) {
        System.out.println("alive " + state);
    }

    protected boolean hasGui() {
        return true;
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPartReader.class;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Class<? extends GuiScreen> getGui() {
        return GuiPartReader.class;
    }

    @Override
    public ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    @Override
    public boolean onPartActivated(World world, BlockPos pos, IBlockState state, S partState, EntityPlayer player,
                                   EnumFacing side, float hitX, float hitY, float hitZ) {
        // Drop through if the player is sneaking
        if(player.isSneaking()) {
            return false;
        }

        getMod().getGuiHandler().setTemporaryData(ExtendedGuiHandler.PART, side); // Pass the side as extra data to the gui
        if(!world.isRemote && hasGui()) {
            player.openGui(getMod().getModId(), getGuiID(), world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

}
