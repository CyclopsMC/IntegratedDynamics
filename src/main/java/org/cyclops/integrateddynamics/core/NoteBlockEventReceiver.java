package org.cyclops.integrateddynamics.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Captures note block events for one tick.
 * @author rubensworks
 */
public final class NoteBlockEventReceiver {

    private static NoteBlockEventReceiver INSTANCE;
    private Multimap<NoteBlockEvent.Instrument, NoteBlockEvent.Play> previousEvents = HashMultimap.create();
    private Multimap<NoteBlockEvent.Instrument, NoteBlockEvent.Play> currentEvents = HashMultimap.create();

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
            currentEvents.put(event.instrument, event);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if(event.type == TickEvent.Type.SERVER && event.phase == TickEvent.Phase.START) {
            Multimap<NoteBlockEvent.Instrument, NoteBlockEvent.Play> tmp = previousEvents;
            previousEvents.clear();
            previousEvents = currentEvents;
            currentEvents = tmp;
        }
    }

    public Multimap<NoteBlockEvent.Instrument, NoteBlockEvent.Play> getEvents() {
        return previousEvents;
    }
}
