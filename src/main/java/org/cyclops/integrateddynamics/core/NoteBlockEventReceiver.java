package org.cyclops.integrateddynamics.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Captures note block events for one tick.
 * @author rubensworks
 */
public final class NoteBlockEventReceiver {

    private static NoteBlockEventReceiver INSTANCE;
    private Multimap<NoteBlockInstrument, NoteBlockEvent.Play> previousEvents = HashMultimap.create();
    private Multimap<NoteBlockInstrument, NoteBlockEvent.Play> currentEvents = HashMultimap.create();

    private NoteBlockEventReceiver() {

    }

    public static NoteBlockEventReceiver getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new NoteBlockEventReceiver();
        }
        return INSTANCE;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onNoteEvent(NoteBlockEvent.Play event) {
        if(!event.isCanceled()) {
            currentEvents.put(event.getInstrument(), event);
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent.Pre event) {
        Multimap<NoteBlockInstrument, NoteBlockEvent.Play> tmp = previousEvents;
        previousEvents.clear();
        previousEvents = currentEvents;
        currentEvents = tmp;
    }

    public Multimap<NoteBlockInstrument, NoteBlockEvent.Play> getEvents() {
        return previousEvents;
    }
}
