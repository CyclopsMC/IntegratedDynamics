package org.cyclops.integrateddynamics.core.persist.world;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.persist.world.WorldStorage;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.network.packet.ActionLabelPacket;
import org.cyclops.integrateddynamics.network.packet.AllLabelsPacket;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * World NBT storage for variable labels.
 * Available client- and serverside and correctly synced.
 * @author rubensworks
 */
public class LabelsWorldStorage extends WorldStorage<LabelsWorldStorage> {

    private final Map<Integer, String> labels;

    public LabelsWorldStorage(Map<Integer, String> labels) {
        this.labels = Maps.newHashMap(labels);
        NeoForge.EVENT_BUS.register(this);
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
        setDirty();
    }

    /**
     * Remove a onLabelPacket mapping by variable id.
     * Should only be called from within packets.
     * @param variableId The variable id.
     */
    public synchronized void removeUnsafe(int variableId) {
        labels.remove(variableId);
        setDirty();
    }

    /**
     * Put a onLabelPacket mapping for a variable id getting a onLabelPacket.
     * @param variableId The variable id.
     * @param label The onLabelPacket
     */
    public void put(int variableId, @Nonnull String label) {
        if(IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
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
        if(IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
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
        if (variableId < 0) {
            return null;
        }
        return labels.get(variableId);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if(!IModHelpers.get().getMinecraftHelpers().isClientSideThread()) {
            try {
                IntegratedDynamics._instance.getPacketHandler().sendToPlayer(new AllLabelsPacket(this.labels), (ServerPlayer) event.getEntity());
            } catch (Exception e) {
                // Ignore if packet cannot be sent (e.g., mock players in game tests have no connection)
            }
        }
    }

    @Override
    public void afterLoad() {
        super.afterLoad();
        // Fix all null labels (#1038)
        // This should not be able to occur, but it does, no idea why...
        int sizeBefore = labels.size();
        labels.entrySet().removeIf(integerStringEntry -> integerStringEntry.getValue() == null);
        if (sizeBefore != labels.size()) {
            setDirty();
        }
    }

    public void clear() {
        labels.clear();
    }

    public static class Access extends WorldStorage.Access<LabelsWorldStorage> {

        private static LabelsWorldStorage.Access INSTANCE = null;
        private LabelsWorldStorage clientInstance;

        public static LabelsWorldStorage.Access getInstance(ModBaseNeoForge mod) {
            if(INSTANCE == null) {
                INSTANCE = new LabelsWorldStorage.Access(mod);
            }
            return INSTANCE;
        }

        public Access(ModBaseNeoForge<?> mod) {
            super(new SavedDataType<>(
                    mod.getModId() + "_labels",
                    (ctx) -> new LabelsWorldStorage(Maps.newHashMap()),
                    ctx -> RecordCodecBuilder.create(instance -> instance.group(
                            RecordCodecBuilder.point(ctx.getLevel()),
                            NeoForgeExtraCodecs.unboundedMapAsList("k", Codec.INT, "v", Codec.STRING).fieldOf("counters").forGetter(data -> data.labels)
                    ).apply(instance, (level, labels) -> new LabelsWorldStorage(labels)))
            ), mod);
        }

        @Override
        public LabelsWorldStorage get() {
            if (IModHelpers.get().getMinecraftHelpers().isClientSide()) {
                if (clientInstance == null) {
                    clientInstance = new LabelsWorldStorage(Maps.newHashMap());
                }
                return clientInstance;
            }
            return super.get();
        }
    }

}
