package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.client.gui.GuiHandler;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.IPartType;
import org.cyclops.integrateddynamics.core.part.PartTarget;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * An extension of the default cyclops gui handler with support for some more gui types.
 * @author rubensworks
 */
public class ExtendedGuiHandler extends GuiHandler {

    /**
     * Gui type for parts
     */
    public static final GuiType<EnumFacing> PART = GuiType.create();
    static {
        PART.setContainerConstructor(new IContainerConstructor<EnumFacing>() {
            @Override
            public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z,
                                              Class<? extends Container> containerClass, EnumFacing side) {
                try {
                    Triple<IPartContainer, IPartType, IPartState> data = getPartConstructionData(world,
                            new BlockPos(x, y, z), side);
                    if(data == null) return null;
                    Constructor<? extends Container> containerConstructor = containerClass.getConstructor(
                            EntityPlayer.class, PartTarget.class, IPartContainer.class,
                            data.getMiddle().getPartTypeClass(), data.getRight().getPartStateClass());
                    return containerConstructor.newInstance(player,
                           PartTarget.fromCenter(world, new BlockPos(x, y, z), side), data.getLeft(), data.getMiddle(),
                           data.getRight());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        if(MinecraftHelpers.isClientSide()) {
            PART.setGuiConstructor(new IGuiConstructor<EnumFacing>() {
                @Override
                public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z,
                                                  Class<? extends GuiScreen> guiClass, EnumFacing side) {
                    try {
                        Triple<IPartContainer, IPartType, IPartState> data = getPartConstructionData(world,
                                new BlockPos(x, y, z), side);
                        if(data == null) return null;
                        Constructor<? extends GuiScreen> guiConstructor = guiClass.getConstructor(
                                EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                data.getMiddle().getPartTypeClass(), data.getRight().getPartStateClass());
                        return guiConstructor.newInstance(player,
                               PartTarget.fromCenter(world, new BlockPos(x, y, z), side), data.getLeft(),
                               data.getMiddle(), data.getRight());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        }
    }

    private static Triple<IPartContainer, IPartType, IPartState> getPartConstructionData(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if(!(tileEntity instanceof IPartContainer)) {
            IntegratedDynamics.clog(Level.WARN, String.format("The tile at %s is not a valid part container.", pos));
            return null;
        }
        IPartContainer partContainer = (IPartContainer) tileEntity;
        IPartType partType = partContainer.getPart(side);
        if(partType == null) {
            IntegratedDynamics.clog(Level.WARN, String.format("The part container at %s side %s does not " +
                            "have a valid part.", pos, side));
            return null;
        }
        IPartState partState = partContainer.getPartState(side);
        return Triple.of(partContainer, partType, partState);
    }

    public ExtendedGuiHandler(ModBase mod) {
        super(mod);
    }
}
