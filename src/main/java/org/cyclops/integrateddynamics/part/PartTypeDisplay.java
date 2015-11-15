package org.cyclops.integrateddynamics.part;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.client.gui.GuiPartDisplay;
import org.cyclops.integrateddynamics.core.block.IgnoredBlock;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integrateddynamics.core.network.Network;
import org.cyclops.integrateddynamics.core.network.event.NetworkEvent;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartStateActiveVariableBase;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.inventory.container.ContainerPartDisplay;

import java.util.List;
import java.util.Map;

/**
 * A part that can display variables..
 * @author rubensworks
 */
public class PartTypeDisplay extends PartTypeBase<PartTypeDisplay, PartTypeDisplay.State> {

    public PartTypeDisplay(String name) {
        super(name, new RenderPosition(0.1875F, 0.625F, 0.625F));
    }

    @Override
    protected Map<Class<? extends NetworkEvent>, IEventAction> constructNetworkEventActions() {
        Map<Class<? extends NetworkEvent>, IEventAction> actions = super.constructNetworkEventActions();
        actions.put(VariableContentsUpdatedEvent.class, new IEventAction<PartTypeDisplay, PartTypeDisplay.State, VariableContentsUpdatedEvent>() {
            @Override
            public void onAction(Network network, PartTarget target, PartTypeDisplay.State state, VariableContentsUpdatedEvent event) {
                onVariableContentsUpdated(event.getNetwork(), target, state);
            }
        });
        return actions;
    }

    @Override
    public Class<? super PartTypeDisplay> getPartTypeClass() {
        return PartTypeDisplay.class;
    }

    @Override
    public void addDrops(PartTarget target, PartTypeDisplay.State state, List<ItemStack> itemStacks) {
        for(int i = 0; i < state.getInventory().getSizeInventory(); i++) {
            ItemStack itemStack = state.getInventory().getStackInSlot(i);
            if(itemStack != null) {
                itemStacks.add(itemStack);
            }
        }
        state.getInventory().clear();
        state.onVariableContentsUpdated(this, target);
        super.addDrops(target, state, itemStacks);
    }

    @Override
    public void beforeNetworkKill(Network network, PartTarget target, PartTypeDisplay.State state) {
        super.beforeNetworkKill(network, target, state);
        state.onVariableContentsUpdated(this, target);
    }

    @Override
    public void afterNetworkAlive(Network network, PartTarget target, PartTypeDisplay.State state) {
        super.afterNetworkAlive(network, target, state);
        state.onVariableContentsUpdated(this, target);
    }

    @Override
    protected Block createBlock(BlockConfig blockConfig) {
        return new IgnoredBlockStatus(blockConfig);
    }

    @Override
    public PartTypeDisplay.State constructDefaultState() {
        return new PartTypeDisplay.State(1);
    }

    @Override
    public Class<? extends Container> getContainer() {
        return ContainerPartDisplay.class;
    }

    @Override
    public Class<? extends GuiScreen> getGui() {
        return GuiPartDisplay.class;
    }

    @Override
    public boolean isUpdate(PartTypeDisplay.State state) {
        return true;
    }

    @Override
    public void update(Network network, PartTarget target, PartTypeDisplay.State state) {
        super.update(network, target, state);
        IValue lastValue = state.getDisplayValue();
        IValue newValue = null;
        if(state.hasVariable()) {
            try {
                IVariable variable = state.getVariable(network);
                if(variable != null) {
                    newValue = variable.getValue();

                }
            } catch (EvaluationException e) {
                state.addGlobalError(new L10NHelpers.UnlocalizedString(e.getLocalizedMessage()));
            }
        }
        if(!ValueHelpers.areValuesEqual(lastValue, newValue)) {
            state.setDisplayValue(newValue);
            state.sendUpdate();
        }
    }

    @Override
    public IBlockState getBlockState(TileMultipartTicking tile, double x, double y, double z, float partialTick,
                                     int destroyStage, EnumFacing side) {
        PartTypeDisplay.State state = (PartTypeDisplay.State) tile.getPartState(side);
        IgnoredBlockStatus.Status status = IgnoredBlockStatus.Status.INACTIVE;
        if(!state.getInventory().isEmpty()) {
            if(state.hasVariable()) {
                status = IgnoredBlockStatus.Status.ACTIVE;
            } else {
                status = IgnoredBlockStatus.Status.ERROR;
            }
        }
        return getBlock().getDefaultState().withProperty(IgnoredBlock.FACING, side).
                withProperty(IgnoredBlockStatus.STATUS, status);
    }

    protected void onVariableContentsUpdated(Network network, PartTarget target, PartTypeDisplay.State state) {
        state.onVariableContentsUpdated(this, target);
    }

    @Override
    public boolean onPartActivated(World world, BlockPos pos, IBlockState state, final State partState, EntityPlayer player,
                                   EnumFacing side, float hitX, float hitY, float hitZ) {
        if(WrenchHelpers.isWrench(player, pos)) {
            WrenchHelpers.wrench(player, pos, new WrenchHelpers.IWrenchAction<Void>() {
                @Override
                public void onWrench(EntityPlayer player, BlockPos pos, Void parameter) {
                    partState.setFacingRotation(partState.getFacingRotation().rotateAround(EnumFacing.Axis.Y));
                }
            });
            return true;
        }
        return super.onPartActivated(world, pos, state, partState, player, side, hitX, hitY, hitZ);
    }

    public static class State extends PartStateActiveVariableBase<PartTypeDisplay> {

        @Getter
        @Setter
        private IValue displayValue;
        @Getter
        @Setter
        private EnumFacing facingRotation = EnumFacing.NORTH;

        public State(int inventorySize) {
            super(inventorySize);
        }

        @Override
        public Class<? extends IPartState> getPartStateClass() {
            return PartTypeDisplay.State.class;
        }

        @Override
        public void writeToNBT(NBTTagCompound tag) {
            super.writeToNBT(tag);
            IValue value = getDisplayValue();
            if(value != null) {
                tag.setString("displayValueType", value.getType().getUnlocalizedName());
                tag.setString("displayValue", value.getType().serialize(value));
            }
            tag.setInteger("facingRotation", facingRotation.ordinal());
        }

        @Override
        public void readFromNBT(NBTTagCompound tag) {
            super.readFromNBT(tag);
            if(tag.hasKey("displayValueType", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())
                    && tag.hasKey("displayValue", MinecraftHelpers.NBTTag_Types.NBTTagString.ordinal())) {
                IValueType valueType = ValueTypes.REGISTRY.getValueType(tag.getString("displayValueType"));
                if(valueType != null) {
                    setDisplayValue(valueType.deserialize(tag.getString("displayValue")));
                } else {
                    IntegratedDynamics.clog(Level.ERROR,
                            String.format("Tried to deserialize the value \"%s\" for type \"%s\" which could not be found.",
                            tag.getString("displayValueType"), tag.getString("value")));
                }
            } else {
                setDisplayValue(null);
            }
            facingRotation = EnumFacing.values()[Math.max(2, tag.getInteger("facingRotation"))];
        }
    }

}
