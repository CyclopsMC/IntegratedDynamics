package org.cyclops.integrateddynamics.api.part;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.PartStateException;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * An interface for containers that can hold {@link IPartType}s.
 * @author rubensworks
 */
public interface IPartContainer extends ICapabilitySerializable<CompoundTag> {

    /**
     * Should be called every tick, updates parts.
     */
    public void update();

    /**
     * @return The position this container is at.
     */
    public DimPos getPosition();

    /**
     * @return The parts inside this container.
     */
    public Map<Direction, IPartType<?, ?>> getParts();

    /**
     * @return If this container has at least one part.
     */
    public boolean hasParts();

    /**
     * Set the part for a side.
     * @param side The side to place the part on.
     * @param part The part.
     * @param partState The state for this part.
     * @param <P> The type of part.
     * @param <S> The type of part state.
     */
    public <P extends IPartType<P, S>, S extends IPartState<P>> void setPart(Direction side, IPartType<P, S> part,
                                                                             IPartState<P> partState);

    /**
     * Check if the given part can be added at the given side.
     * @param side The side to place the part on.
     * @param part The part.
     * @param <P> The type of part.
     * @param <S> The type of part state.
     * @return If the part can be added.
     */
    public <P extends IPartType<P, S>, S extends IPartState<P>> boolean canAddPart(Direction side, IPartType<P, S> part);

    /**
     * Get the part of a side, can be null.
     * @param side The side.
     * @return The part or null.
     */
    public IPartType getPart(Direction side);

    /**
     * @param side The side.
     * @return If the given side has a part.
     */
    public boolean hasPart(Direction side);

    /**
     * Remove the part from a side, can return null if there was no part on that side.
     * @param side The side.
     * @param player The player removing the part.
     * @param dropMainElement If the main part element should be dropped.
     * @param saveState If the part state should be saved in the item.
     * @return The removed part or null.
     */
    public IPartType removePart(Direction side, @Nullable Player player, boolean dropMainElement, boolean saveState);

    /**dz
     * Set the state of a part.
     * @param side The side.
     * @param partState The part state.
     * @throws PartStateException If no part at the given position is available.
     */
    public void setPartState(Direction side, IPartState partState) throws PartStateException;

    /**
     * Get the state of a part.
     * @param side The side.
     * @return The part state.
     * @throws PartStateException If no part at the given position is available.
     */
    public IPartState getPartState(Direction side) throws PartStateException;

    /**
     * Get the part side the player is watching.
     * This is used to determine the part the player is looking at.
     * @param world The world.
     * @param pos The block position to perform a ray trace for.
     * @param player The player.
     * @return The side the player is watching or null.
     */
    public @Nullable
    Direction getWatchingSide(Level world, BlockPos pos, Player player);

}
