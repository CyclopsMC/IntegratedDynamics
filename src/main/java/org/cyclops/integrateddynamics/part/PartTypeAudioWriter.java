package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
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
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Write.Audio.INTEGER_PIANO_NOTE,
                Aspects.Write.Audio.INTEGER_BASSDRUM_NOTE,
                Aspects.Write.Audio.INTEGER_SNARE_NOTE,
                Aspects.Write.Audio.INTEGER_CLICKS_NOTE,
                Aspects.Write.Audio.INTEGER_BASSGUITAR_NOTE,
                Aspects.Write.Audio.STRING_SOUND,
                Aspects.Write.Audio.STRING_TEXT
        ));
    }

    @Override
    public PartStateWriterBase<PartTypeAudioWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeAudioWriter>(Aspects.REGISTRY.getAspects(this).size());
    }

}
