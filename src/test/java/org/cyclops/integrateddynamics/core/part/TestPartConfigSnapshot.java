package org.cyclops.integrateddynamics.core.part;

import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    protected static PartConfigSnapshot roundTrip(PartConfigSnapshot snapshot) {
        CompoundTag tag = snapshot.toNBT(RegistryAccess.EMPTY);
        return PartConfigSnapshot.fromNBT(RegistryAccess.EMPTY, tag).orElse(null);
    }

    protected static CompoundTag aspectPropertiesTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dummy", "value");
        return tag;
    }

    @Test
    public void testRoundTripAllSections() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.of(20), Optional.of(3), Optional.of(7),
                        Optional.of(Direction.NORTH), Optional.of(new Vec3i(1, -2, 3)))),
                Map.of(ASPECT, aspectPropertiesTag()),
                List.of());

        assertThat(roundTrip(snapshot), is(snapshot));
    }

    @Test
    public void testRoundTripWithoutPartSettings() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(ASPECT, aspectPropertiesTag()), List.of());

        assertThat(roundTrip(snapshot), is(snapshot));
    }

    @Test
    public void testRoundTripWithoutAspectProperties() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.of(new PartConfigSnapshot.PartSettings(Optional.of(1), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty())),
                Map.of(), List.of());

        assertThat(roundTrip(snapshot), is(snapshot));
    }

    @Test
    public void testRoundTripEmpty() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(), List.of());

        assertThat(roundTrip(snapshot), is(snapshot));
        assertThat(snapshot.isEmpty(), is(true));
    }

    @Test
    public void testPartSettingsEmptyWhenEverythingIsDefault() {
        assertThat(new PartConfigSnapshot.PartSettings(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty()).isEmpty(), is(true));
        assertThat(new PartConfigSnapshot.PartSettings(Optional.of(1), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty()).isEmpty(), is(false));
    }

    @Test
    public void testRequiredBlankVariables() {
        PartConfigSnapshot snapshot = new PartConfigSnapshot(PartConfigSnapshot.VERSION, PART_TYPE,
                Optional.empty(), Map.of(), List.of());

        assertThat(snapshot.getRequiredBlankVariables(PartConfigSection.ALL), is(0));
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
                        Optional.empty(), Optional.empty())),
                Map.of(ASPECT, aspectPropertiesTag()), List.of());

        assertThat(snapshot.hasSection(PartConfigSection.PART_SETTINGS), is(true));
        assertThat(snapshot.hasSection(PartConfigSection.ASPECT), is(true));
        assertThat(snapshot.getSections(), is(PartConfigSection.ALL));
    }

}
