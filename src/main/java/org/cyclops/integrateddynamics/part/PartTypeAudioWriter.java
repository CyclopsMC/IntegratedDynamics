package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Sets;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An audio writer part.
 * @author rubensworks
 */
public class PartTypeAudioWriter extends PartTypeWriteBase<PartTypeAudioWriter, PartStateWriterBase<PartTypeAudioWriter>> {

    public PartTypeAudioWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Sets.<IAspect>newHashSet(
                Aspects.Write.Audio.INTEGER_PIANO_NOTE,
                Aspects.Write.Audio.INTEGER_BASSDRUM_NOTE,
                Aspects.Write.Audio.INTEGER_SNARE_NOTE,
                Aspects.Write.Audio.INTEGER_CLICKS_NOTE,
                Aspects.Write.Audio.NOTE_INTEGER_BASSGUITAR
        ));
    }

    @Override
    public PartStateWriterBase<PartTypeAudioWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeAudioWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

}
