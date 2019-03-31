package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.persist.world.WorldStorage;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.network.packet.ActionLabelPacket;
import org.cyclops.integrateddynamics.core.network.packet.AllLabelsPacket;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * World NBT storage for variable labels.
 * Available client- and serverside and correctly synced.
 * @author rubensworks
 */
public class LabelsWorldStorage extends WorldStorage {

    private static LabelsWorldStorage INSTANCE = null;

    @NBTPersist
    private Map<Integer, String> labels = Maps.newHashMap();

    private LabelsWorldStorage(ModBase mod) {
        super(mod);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static LabelsWorldStorage getInstance(ModBase mod) {
        if(INSTANCE == null) {
            INSTANCE = new LabelsWorldStorage(mod);
        }
        return INSTANCE;
    }

    @Override
    public void reset() {
        labels.clear();
    }

    @Override
    protected String getDataId() {
        return "Labels";
    }

    /**
     * Put a onLabelPacket mapping for a variable id getting a onLabelPacket.
     * Should only be called from within packets.
     * @param variableId The variable id.
     * @param label The onLabelPacket
     */
    public synchronized void putUnsafe(int variableId, @Nonnull String label) {
        Objects.requireNonNull(label);
        labels.put(variableId, label);
    }

    /**
     * Remove a onLabelPacket mapping by variable id.
     * Should only be called from within packets.
     * @param variableId The variable id.
     */
    public synchronized void removeUnsafe(int variableId) {
        labels.remove(variableId);
    }

    /**
     * Put a onLabelPacket mapping for a variable id getting a onLabelPacket.
     * @param variableId The variable id.
     * @param label The onLabelPacket
     */
    public void put(int variableId, @Nonnull String label) {
        if(MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(new ActionLabelPacket(variableId, label));
        } else {
            putUnsafe(variableId, label);
            IntegratedDynamics._instance.getPacketHandler().sendToAll(new ActionLabelPacket(variableId, label));
        }
    }

    /**
     * Remove a onLabelPacket mapping by variable id.
     * @param variableId The variable id.
     */
    public void remove(int variableId) {
        if(MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getPacketHandler().sendToServer(new ActionLabelPacket(variableId, null));
        } else {
            removeUnsafe(variableId);
            IntegratedDynamics._instance.getPacketHandler().sendToAll(new ActionLabelPacket(variableId, null));
        }
    }

    /**
     * Get a onLabelPacket by variable id.
     * @param variableId The variable id.
     * @return The corresponding variable onLabelPacket or null.
     */
    public synchronized String getLabel(int variableId) {
        return labels.get(variableId);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if(!MinecraftHelpers.isClientSide()) {
            IntegratedDynamics._instance.getPacketHandler().sendToPlayer(new AllLabelsPacket(this.labels), (EntityPlayerMP) event.player);
        }
    }

    @Override
    public void afterLoad() {
        super.afterLoad();
        // Fix all null labels
        // TODO: remove in 1.13
        labels.entrySet().removeIf(integerStringEntry -> integerStringEntry.getValue() == null);
    }
}
