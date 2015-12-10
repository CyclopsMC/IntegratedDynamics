package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.client.gui.GuiHandler;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;

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
    public static final GuiType<EnumFacing> PART = GuiType.create(true);
    /**
     * Gui type for part aspects
     */
    public static final GuiType<Pair<EnumFacing, IAspect>> ASPECT = GuiType.create(true);
    static {
        PART.setContainerConstructor(new IContainerConstructor<EnumFacing>() {
            @Override
            public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z,
                                              Class<? extends Container> containerClass, EnumFacing side) {
                try {
                    Pair<IPartContainer, IPartType> data = getPartConstructionData(world,
                            new BlockPos(x, y, z), side);
                    if(data == null) return null;
                    Constructor<? extends Container> containerConstructor;
                    try {
                        containerConstructor = containerClass.getConstructor(
                                EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                data.getRight().getPartTypeClass());
                    } catch(NoSuchMethodException e ) {
                        containerConstructor = containerClass.getConstructor(
                                EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                IPartType.class);
                    }
                    return containerConstructor.newInstance(player,
                           PartTarget.fromCenter(world, new BlockPos(x, y, z), side), data.getLeft(), data.getRight());
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
                        Pair<IPartContainer, IPartType> data = getPartConstructionData(world,
                                new BlockPos(x, y, z), side);
                        if(data == null) return null;
                        Constructor<? extends GuiScreen> guiConstructor;
                        try {
                            guiConstructor = guiClass.getConstructor(
                                    EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                    data.getRight().getPartTypeClass());
                        } catch (NoSuchMethodException e) {
                            guiConstructor = guiClass.getConstructor(
                                    EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                    IPartType.class);
                        }
                        return guiConstructor.newInstance(player,
                               PartTarget.fromCenter(world, new BlockPos(x, y, z), side), data.getLeft(),
                               data.getRight());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        }
        ASPECT.setContainerConstructor(new IContainerConstructor<Pair<EnumFacing, IAspect>>() {
            @Override
            public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z,
                                              Class<? extends Container> containerClass, Pair<EnumFacing, IAspect> dataIn) {
                try {
                    Pair<IPartContainer, IPartType> data = getPartConstructionData(world,
                            new BlockPos(x, y, z), dataIn.getLeft());
                    if(data == null) return null;
                    Constructor<? extends Container> containerConstructor = containerClass.getConstructor(
                                EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                IPartType.class, IAspect.class);
                    return containerConstructor.newInstance(player,
                            PartTarget.fromCenter(world, new BlockPos(x, y, z), dataIn.getLeft()), data.getLeft(), data.getRight(), dataIn.getRight());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        if(MinecraftHelpers.isClientSide()) {
            ASPECT.setGuiConstructor(new IGuiConstructor<Pair<EnumFacing, IAspect>>() {
                @Override
                public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z,
                                                  Class<? extends GuiScreen> guiClass, Pair<EnumFacing, IAspect> dataIn) {
                    try {
                        Pair<IPartContainer, IPartType> data = getPartConstructionData(world,
                                new BlockPos(x, y, z), dataIn.getLeft());
                        if(data == null) return null;
                        Constructor<? extends GuiScreen> guiConstructor = guiClass.getConstructor(
                                    EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                    IPartType.class, IAspect.class);
                        return guiConstructor.newInstance(player,
                                PartTarget.fromCenter(world, new BlockPos(x, y, z), dataIn.getLeft()), data.getLeft(),
                                data.getRight(), dataIn.getRight());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        }
    }

    private static Pair<IPartContainer, IPartType> getPartConstructionData(World world, BlockPos pos, EnumFacing side) {
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
        return Pair.of(partContainer, partType);
    }

    public ExtendedGuiHandler(ModBase mod) {
        super(mod);
    }
}
