package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.write.PartStateWriterBase;
import org.cyclops.integrateddynamics.core.part.write.PartTypeWriteBase;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An audio writer part.
 * @author rubensworks
 */
public class PartTypeAudioWriter extends PartTypeWriteBase<PartTypeAudioWriter, PartStateWriterBase<PartTypeAudioWriter>> {

    public PartTypeAudioWriter(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Write.Audio.INTEGER_HARP_NOTE,
                Aspects.Write.Audio.INTEGER_BASEDRUM_NOTE,
                Aspects.Write.Audio.INTEGER_SNARE_NOTE,
                Aspects.Write.Audio.INTEGER_HAT_NOTE,
                Aspects.Write.Audio.INTEGER_BASS_NOTE,
                Aspects.Write.Audio.INTEGER_FLUTE_NOTE,
                Aspects.Write.Audio.INTEGER_BELL_NOTE,
                Aspects.Write.Audio.INTEGER_GUITAR_NOTE,
                Aspects.Write.Audio.INTEGER_CHIME_NOTE,
                Aspects.Write.Audio.INTEGER_XYLOPHONE_NOTE,
                Aspects.Write.Audio.INTEGER_IRON_XYLOPHONE_NOTE,
                Aspects.Write.Audio.INTEGER_COW_BELL_NOTE,
                Aspects.Write.Audio.INTEGER_DIDGERIDOO_NOTE,
                Aspects.Write.Audio.INTEGER_BIT_NOTE,
                Aspects.Write.Audio.INTEGER_BANJO_NOTE,
                Aspects.Write.Audio.INTEGER_PLING_NOTE,
                Aspects.Write.Audio.STRING_SOUND,
                Aspects.Write.Audio.STRING_TEXT
        ));
    }

    @Override
    public PartStateWriterBase<PartTypeAudioWriter> constructDefaultState() {
        return new PartStateWriterBase<PartTypeAudioWriter>(Aspects.REGISTRY.getAspects(this).size());
    }
    
    @Override
    public int getConsumptionRate(PartStateWriterBase<PartTypeAudioWriter> state) {
        return GeneralConfig.audioWriterBaseConsumption;
    }

}
