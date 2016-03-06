package org.cyclops.integrateddynamics.part.aspect.write.audio;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.world.NoteBlockEvent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteIntegerBase;

import java.util.List;
import java.util.Locale;

/**
 * Write an instrument sound.
 * @author rubensworks
 */
public class AspectWriteIntegerAudioInstrument extends AspectWriteIntegerBase {

    public static final IAspectPropertyTypeInstance<ValueTypeDouble, ValueTypeDouble.ValueDouble> PROP_VOLUME =
            new AspectPropertyTypeInstance<>(ValueTypes.DOUBLE, "aspect.aspecttypes.integrateddynamics.double.volume.name");
    public static final IAspectProperties PROPERTIES = new AspectProperties(Sets.<IAspectPropertyTypeInstance>newHashSet(
            PROP_VOLUME
    ));
    static {
        PROPERTIES.setValue(PROP_VOLUME, ValueTypeDouble.ValueDouble.of(3D));
    }

    private static final List<String> INSTRUMENTS = Lists.newArrayList(new String[]{"harp", "bd", "snare", "hat", "bassattack"});

    private final NoteBlockEvent.Instrument instrument;

    public AspectWriteIntegerAudioInstrument(NoteBlockEvent.Instrument instrument) {
        super(instrument.name().toLowerCase(Locale.ENGLISH), PROPERTIES);
        this.instrument = instrument;
    }

    @Override
    protected String getUnlocalizedIntegerType() {
        return "instrument." + unlocalizedTypeSuffix;
    }

    private String getInstrument(int id) {
        if (id < 0 || id >= INSTRUMENTS.size()) {
            id = 0;
        }
        return INSTRUMENTS.get(id);
    }

    @Override
    public <P extends IPartTypeWriter<P, S>, S extends IPartStateWriter<P>> void write(P partType, PartTarget target,
                                                                                       S state, IVariable<ValueTypeInteger.ValueInteger> variable)
            throws EvaluationException {
        IAspectProperties properties = getProperties(partType, target, state);
        BlockPos pos = target.getTarget().getPos().getBlockPos();

        int eventID = instrument.ordinal();
        int eventParam = MathHelper.clamp_int(variable.getValue().getRawValue(), 0, 24);
        float f = (float)Math.pow(2.0D, (double)(eventParam - 12) / 12.0D);
        float volume = (float) properties.getValue(PROP_VOLUME).getRawValue();
        IntegratedDynamics.proxy.playSoundMinecraft(
                (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D,
                "note." + this.getInstrument(eventID), volume, f);
    }

}
