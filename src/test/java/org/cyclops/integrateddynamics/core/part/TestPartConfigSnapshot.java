package org.cyclops.integrateddynamics.core.part;

import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the serialization of part configuration snapshots.
 *
 * Variable cards are not covered here, as those require a full registry access,
 * they are covered by the game tests instead.
 *
 * @author rubensworks
 */
public class TestPartConfigSnapshot {

    private static final ResourceLocation PART_TYPE = ResourceLocation.parse("integrateddynamics:redstone_writer");
    private static final ResourceLocation ASPECT = ResourceLocation.parse("integrateddynamics:write_boolean_redstone");
    private static final String PROPERTY = "aspect.aspecttypes.integrateddynamics.integer.channel";

    protected static PartConfigSnapshot roundTrip(PartConfigSnapshot snapshot) {
        CompoundTag tag = snapshot.toNBT(RegistryAccess.EMPTY);
        return PartConfigSnapshot.fromNBT(RegistryAccess.EMPTY, tag).orElse(null);
    }

    // In the shape that AspectProperties serializes into, as the entries are read straight from it
    protected static CompoundTag aspectPropertiesTag() {
        CompoundTag property = new CompoundTag();
        property.putString("key", "integrateddynamics:integer");
        property.putString("label", PROPERTY);
        property.putInt("value", 7);
        ListTag map = new ListTag();
        map.add(property);
        CompoundTag tag = new CompoundTag();
        tag.put("map", map);
        return tag;
    }

    @Test
    public void testRoundTripAllSections() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.of(20), Optional.of(3), Optional.of(7),
                        Optional.of(Direction.NORTH), Optional.of(new Vec3i(1, -2, 3)), Optional.of(8))),
                Map.of(ASPECT, aspectPropertiesTag()),
                List.of(), Map.of(PartConfigSection.ASPECT, aspectPropertiesTag()));

        assertThat(roundTrip(snapshot), is(snapshot));
    }

    @Test
    public void testRoundTripWithoutPartSettings() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(ASPECT, aspectPropertiesTag()), List.of(), Map.of());

        assertThat(roundTrip(snapshot), is(snapshot));
    }

    @Test
    public void testRoundTripWithoutAspectProperties() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.of(1), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty())),
                Map.of(), List.of(), Map.of());

        assertThat(roundTrip(snapshot), is(snapshot));
    }

    @Test
    public void testRoundTripEmpty() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(), List.of(), Map.of());

        assertThat(roundTrip(snapshot), is(snapshot));
        assertThat(snapshot.isEmpty(), is(true));
    }

    @Test
    public void testPartSettingsEmptyWhenEverythingIsDefault() {
        assertThat(new PartConfigSnapshot.PartSettings(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()).isEmpty(), is(true));
        assertThat(new PartConfigSnapshot.PartSettings(Optional.of(1), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty()).isEmpty(), is(false));
        assertThat(new PartConfigSnapshot.PartSettings(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(4)).isEmpty(), is(false));
    }

    @Test
    public void testRequiredBlankVariables() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(), List.of(), Map.of());

        assertThat(snapshot.getRequiredBlankVariables(PartConfigSection.ALL), is(0));
    }

    @Test
    public void testRequiredMaxOffset() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.of(8))),
                Map.of(), List.of(), Map.of());

        assertThat(snapshot.getRequiredMaxOffset(PartConfigSection.ALL), is(8));
        // The maximum offset is part of the part settings, so the aspect sections alone do not need enhancements
        assertThat(snapshot.getRequiredMaxOffset(Set.of(PartConfigSection.ASPECT)), is(0));
    }

    @Test
    public void testRequiredMaxOffsetWithoutEnhancements() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(), List.of(), Map.of());

        assertThat(snapshot.getRequiredMaxOffset(PartConfigSection.ALL), is(0));
    }

    @Test
    public void testRoundTripWithExtraData() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(), List.of(),
                Map.of(PartConfigSection.PART_SETTINGS, aspectPropertiesTag()));

        assertThat(roundTrip(snapshot), is(snapshot));
    }

    @Test
    public void testExtraDataMakesASectionPresent() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(), List.of(),
                Map.of(PartConfigSection.PART_SETTINGS, aspectPropertiesTag()));

        assertThat(snapshot.isEmpty(), is(false));
        assertThat(snapshot.hasSection(PartConfigSection.PART_SETTINGS), is(true));
        assertThat(snapshot.hasSection(PartConfigSection.ASPECT), is(false));
        assertThat(snapshot.getExtraData(PartConfigSection.PART_SETTINGS), is(aspectPropertiesTag()));
        assertThat(snapshot.getExtraData(PartConfigSection.ASPECT).isEmpty(), is(true));
    }

    @Test
    public void testDisabledEntriesRoundTrip() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.of(1), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty())),
                Map.of(), List.of(), Map.of(),
                List.of(PartConfigEntry.idPartSetting(PartConfigSnapshot.SETTING_UPDATE_INTERVAL)));

        assertThat(roundTrip(snapshot), is(snapshot));
    }

    @Test
    public void testDisabledEntryIsNotHeldForItsSection() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.of(1), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty())),
                Map.of(), List.of(), Map.of());

        assertThat(snapshot.hasSection(PartConfigSection.PART_SETTINGS), is(true));
        assertThat(snapshot.isEmpty(), is(false));

        PartConfigSnapshot disabled = snapshot.withEntryEnabled(
                PartConfigEntry.idPartSetting(PartConfigSnapshot.SETTING_UPDATE_INTERVAL), false);

        assertThat(disabled.isEnabled(PartConfigEntry.idPartSetting(PartConfigSnapshot.SETTING_UPDATE_INTERVAL)),
                is(false));
        assertThat(disabled.hasSection(PartConfigSection.PART_SETTINGS), is(false));
        // The value itself is kept, so that switching it back on does not need another copy
        assertThat(disabled.partSettings().get().updateInterval(), is(Optional.of(1)));
        assertThat(disabled.isEmpty(), is(true));
    }

    @Test
    public void testDisabledAspectPropertyIsNotHeldForItsSection() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(ASPECT, aspectPropertiesTag()), List.of(), Map.of());

        assertThat(snapshot.hasSection(PartConfigSection.ASPECT), is(true));

        PartConfigSnapshot disabled = snapshot.withEntryEnabled(
                PartConfigEntry.idAspectProperty(ASPECT, PROPERTY), false);

        assertThat(disabled.hasSection(PartConfigSection.ASPECT), is(false));
    }

    @Test
    public void testDisabledVariableCardIsNotRequiredAnymore() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(), List.of(), Map.of(),
                List.of(PartConfigEntry.idVariableCard(PartConfigSnapshot.INVENTORY_NAME_ACTIVE, 0)));

        assertThat(snapshot.isEnabled(PartConfigEntry.idVariableCard(PartConfigSnapshot.INVENTORY_NAME_ACTIVE, 0)),
                is(false));
        assertThat(snapshot.getRequiredBlankVariables(PartConfigSection.ALL), is(0));
    }

    @Test
    public void testDisabledMaxOffsetIsNotRequiredAnymore() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.of(8))),
                Map.of(), List.of(), Map.of());

        assertThat(snapshot.getRequiredMaxOffset(PartConfigSection.ALL), is(8));
        assertThat(snapshot.withEntryEnabled(PartConfigEntry.idPartSetting(PartConfigSnapshot.SETTING_MAX_OFFSET), false)
                .getRequiredMaxOffset(PartConfigSection.ALL), is(0));
    }

    @Test
    public void testSwitchingAnEntryBackOn() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.of(1), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty())),
                Map.of(), List.of(), Map.of());
        String id = PartConfigEntry.idPartSetting(PartConfigSnapshot.SETTING_UPDATE_INTERVAL);

        assertThat(snapshot.withEntryEnabled(id, false).withEntryEnabled(id, true), is(snapshot));
        // Switching on what is already on changes nothing
        assertThat(snapshot.withEntryEnabled(id, true), is(snapshot));
    }

    @Test
    public void testVariableInventoriesBelongToSections() {
        // The active variable and the aspect setting variables are aspect state
        assertThat(PartConfigSection.forInventoryName(PartConfigSnapshot.INVENTORY_NAME_ACTIVE),
                is(PartConfigSection.ASPECT));
        assertThat(PartConfigSection.forInventoryName("aspectVariables_integrateddynamics:write_boolean_redstone"),
                is(PartConfigSection.ASPECT));
        // While the offset variables are part settings
        assertThat(PartConfigSection.forInventoryName("offsetVariablesInventory"),
                is(PartConfigSection.PART_SETTINGS));
    }

    @Test
    public void testSections() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.of(1), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty())),
                Map.of(ASPECT, aspectPropertiesTag()), List.of(), Map.of());

        assertThat(snapshot.hasSection(PartConfigSection.PART_SETTINGS), is(true));
        assertThat(snapshot.hasSection(PartConfigSection.ASPECT), is(true));
        assertThat(snapshot.getSections(), is(PartConfigSection.ALL));
    }

}
