package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import net.minecraftforge.event.world.NoteBlockEvent;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.part.aspect.write.audio.AspectWriteIntegerAudioInstrument;

/**
 * An audio writer part.
 * @author rubensworks
 */
public class PartTypeAudioWriter extends PartTypeWriteBase<PartTypeAudioWriter, PartStateWriterBase<PartTypeAudioWriter>> {

    public PartTypeAudioWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                new AspectWriteIntegerAudioInstrument(NoteBlockEvent.Instrument.PIANO),
                new AspectWriteIntegerAudioInstrument(NoteBlockEvent.Instrument.BASSDRUM),
                new AspectWriteIntegerAudioInstrument(NoteBlockEvent.Instrument.SNARE),
                new AspectWriteIntegerAudioInstrument(NoteBlockEvent.Instrument.CLICKS),
                new AspectWriteIntegerAudioInstrument(NoteBlockEvent.Instrument.BASSGUITAR)
        ));
    }

    @Override
    public PartStateWriterBase<PartTypeAudioWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeAudioWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

}
