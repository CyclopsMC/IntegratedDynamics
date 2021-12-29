package org.cyclops.integrateddynamics.part;

import com.google.common.collect.Lists;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.part.read.PartStateReaderBase;
import org.cyclops.integrateddynamics.core.part.read.PartTypeReadBase;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * An audio reader part.
 * @author rubensworks
 */
public class PartTypeAudioReader extends PartTypeReadBase<PartTypeAudioReader, PartStateReaderBase<PartTypeAudioReader>> {

    public PartTypeAudioReader(String name) {
        super(name);
        AspectRegistry.getInstance().register(this, Lists.<IAspect>newArrayList(
                Aspects.Read.Audio.INTEGER_HARP_NOTE,
                Aspects.Read.Audio.INTEGER_BASEDRUM_NOTE,
                Aspects.Read.Audio.INTEGER_SNARE_NOTE,
                Aspects.Read.Audio.INTEGER_HAT_NOTE,
                Aspects.Read.Audio.INTEGER_BASS_NOTE,
                Aspects.Read.Audio.INTEGER_FLUTE_NOTE,
                Aspects.Read.Audio.INTEGER_BELL_NOTE,
                Aspects.Read.Audio.INTEGER_GUITAR_NOTE,
                Aspects.Read.Audio.INTEGER_CHIME_NOTE,
                Aspects.Read.Audio.INTEGER_XYLOPHONE_NOTE,
                Aspects.Read.Audio.INTEGER_IRON_XYLOPHONE_NOTE,
                Aspects.Read.Audio.INTEGER_COW_BELL_NOTE,
                Aspects.Read.Audio.INTEGER_DIDGERIDOO_NOTE,
                Aspects.Read.Audio.INTEGER_BIT_NOTE,
                Aspects.Read.Audio.INTEGER_BANJO_NOTE,
                Aspects.Read.Audio.INTEGER_PLING_NOTE
        ));
    }

    @Override
    public PartStateReaderBase<PartTypeAudioReader> constructDefaultState() {
        return new PartStateReaderBase<PartTypeAudioReader>();
    }

    @Override
    public int getConsumptionRate(PartStateReaderBase<PartTypeAudioReader> state) {
        return GeneralConfig.audioReaderBaseConsumption;
    }

}
