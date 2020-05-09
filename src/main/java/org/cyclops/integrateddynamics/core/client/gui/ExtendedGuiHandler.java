package org.cyclops.integrateddynamics.core.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.client.gui.GuiHandler;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.part.PartTypeBase;

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
                    Triple<IPartContainer, PartTypeBase, PartTarget> data = getPartConstructionData(world,
                            new BlockPos(x, y, z), side);
                    if(data == null) return null;
                    Constructor<? extends Container> containerConstructor;
                    try {
                        containerConstructor = containerClass.getConstructor(
                                EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                data.getMiddle().getPartTypeClass());
                    } catch(NoSuchMethodException e ) {
                        containerConstructor = containerClass.getConstructor(
                                EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                IPartType.class);
                    }
                    return containerConstructor.newInstance(player, data.getRight(), data.getLeft(), data.getMiddle());
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
                        Triple<IPartContainer, PartTypeBase, PartTarget> data = getPartConstructionData(world,
                                new BlockPos(x, y, z), side);
                        if(data == null) return null;
                        Constructor<? extends GuiScreen> guiConstructor;
                        try {
                            guiConstructor = guiClass.getConstructor(
                                    EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                    data.getMiddle().getPartTypeClass());
                        } catch (NoSuchMethodException e) {
                            guiConstructor = guiClass.getConstructor(
                                    EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                    IPartType.class);
                        }
                        return guiConstructor.newInstance(player, data.getRight(), data.getLeft(), data.getMiddle());
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
                    Triple<IPartContainer, PartTypeBase, PartTarget> data = getPartConstructionData(world,
                            new BlockPos(x, y, z), dataIn.getLeft());
                    if(data == null) return null;
                    Constructor<? extends Container> containerConstructor = containerClass.getConstructor(
                                EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                IPartType.class, IAspect.class);
                    return containerConstructor.newInstance(player, data.getRight(), data.getLeft(), data.getMiddle(), dataIn.getRight());
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
                        Triple<IPartContainer, PartTypeBase, PartTarget> data = getPartConstructionData(world,
                                new BlockPos(x, y, z), dataIn.getLeft());
                        if(data == null) return null;
                        Constructor<? extends GuiScreen> guiConstructor = guiClass.getConstructor(
                                    EntityPlayer.class, PartTarget.class, IPartContainer.class,
                                    IPartType.class, IAspect.class);
                        return guiConstructor.newInstance(player, data.getRight(), data.getLeft(), data.getMiddle(), dataIn.getRight());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        }
    }

    public static Triple<IPartContainer, PartTypeBase, PartTarget> getPartConstructionData(World world, BlockPos pos, EnumFacing side) {
        IPartContainer partContainer = PartHelpers.getPartContainer(world, pos, side);
        if(partContainer == null) {
            IntegratedDynamics.clog(Level.WARN, String.format("The tile at %s is not a valid part container.", pos));
            return null;
        }
        IPartType partType = partContainer.getPart(side);
        if(partType == null || !(partType instanceof PartTypeBase)) {
            IntegratedDynamics.clog(Level.WARN, String.format("The part container at %s side %s does not " +
                            "have a valid part.", pos, side));
            return null;
        }
        PartTarget target = partType.getTarget(PartPos.of(DimPos.of(world, pos), side), partContainer.getPartState(side));
        return Triple.of(partContainer, (PartTypeBase) partType, target);
    }

    public ExtendedGuiHandler(ModBase mod) {
        super(mod);
    }
}
