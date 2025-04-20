package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import static org.cyclops.integrateddynamics.gametest.GameTestHelpersIntegratedDynamics.testReadAspect;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspectsReadAudio {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioHarp(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.HARP);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_HARP_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioBassdrum(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.BASEDRUM);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_BASEDRUM_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioSnare(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.SNARE);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_SNARE_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioHat(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.HAT);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_HAT_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioBass(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.BASS);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_BASS_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioFlute(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.FLUTE);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_FLUTE_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioBell(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.BELL);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_BELL_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioGuitar(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.GUITAR);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_GUITAR_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioChime(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.CHIME);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_CHIME_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioXylophone(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.XYLOPHONE);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_XYLOPHONE_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioIronXylophone(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.IRON_XYLOPHONE);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_IRON_XYLOPHONE_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioCowBell(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.COW_BELL);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_COW_BELL_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioDidgeridoo(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.DIDGERIDOO);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_DIDGERIDOO_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioBit(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.BIT);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_BIT_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioBanjo(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.BANJO);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_BANJO_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioPling(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.PLING);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_PLING_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioZombie(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.ZOMBIE);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_ZOMBIE_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioSkeleton(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.SKELETON);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_SKELETON_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioCreeper(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.CREEPER);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_CREEPER_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioDragon(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.DRAGON);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_DRAGON_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioWitherSkeleton(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.WITHER_SKELETON);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_WITHER_SKELETON_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioPiglin(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.PIGLIN);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_PIGLIN_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsReadAudioCustomHead(GameTestHelper helper) {
        playNote(helper, NoteBlockInstrument.CUSTOM_HEAD);
        testReadAspect(POS, helper, PartTypes.AUDIO_READER, Aspects.Read.Audio.INTEGER_CUSTOM_HEAD_NOTE, ValueTypeInteger.ValueInteger.of(15));
    }

    public static void playNote(GameTestHelper helper, NoteBlockInstrument instrument) {
        helper.setBlock(POS.west(), Blocks.NOTE_BLOCK.defaultBlockState()
                .setValue(NoteBlock.INSTRUMENT, instrument)
                .setValue(NoteBlock.NOTE, 15));
        helper.getBlockState(POS.west()).triggerEvent(helper.getLevel(), helper.absolutePos(POS), 0, 0);
    }

}
