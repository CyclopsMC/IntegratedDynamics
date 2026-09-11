package org.cyclops.integrateddynamics.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.inventory.ItemLocation;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.core.helper.PartConfigHelpers;
import org.cyclops.integrateddynamics.core.part.PartConfigEntry;
import org.cyclops.integrateddynamics.core.part.PartConfigSnapshot;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Container for looking at the configuration inside a Wrench,
 * and switching off the parts of it that should not be pasted.
 * @author rubensworks
 */
public class ContainerWrenchConfig extends ScrollingInventoryContainer<PartConfigEntry> {

    public static final String BUTTON_TOGGLE_ALL = "button_toggle_all";
    public static final int PAGE_SIZE = 6;

    private final ItemLocation itemLocation;

    public ContainerWrenchConfig(int id, Inventory inventory, FriendlyByteBuf packetBuffer) {
        this(id, inventory, ItemLocation.readFromPacketBuffer(packetBuffer));
    }

    public ContainerWrenchConfig(int id, Inventory inventory, ItemLocation itemLocation) {
        super(RegistryEntries.CONTAINER_WRENCH_CONFIG.get(), id, inventory, new SimpleContainer(0),
                getEntries(inventory.player, itemLocation),
                // The search string is lowercased before it becomes a pattern, so this has to be too
                (entry, pattern) -> pattern.matcher(getSearchableText(entry)).matches());
        this.itemLocation = itemLocation;
        this.addPlayerInventory(inventory, 9, 140);

        // One action per entry, as the entries only change when the player picks up another Wrench, which closes this
        for (PartConfigEntry entry : getUnfilteredItems()) {
            putButtonAction(entry.id(), (buttonId, container) -> toggleEntry(entry.id()));
        }
        putButtonAction(BUTTON_TOGGLE_ALL, (buttonId, container) -> toggleAll());
    }

    /**
     * @param entry An entry that the Wrench holds.
     * @return Everything about that entry that a player can search for, including its section,
     *         as the gui itself has no room to show that.
     */
    protected static String getSearchableText(PartConfigEntry entry) {
        return (Component.translatable(entry.section().getTranslationKey()).getString() + " "
                + entry.group().getString() + " " + entry.label().getString()).toLowerCase(Locale.ENGLISH);
    }

    protected static List<PartConfigEntry> getEntries(Player player, ItemLocation itemLocation) {
        return PartConfigHelpers.getSnapshot(player.level().registryAccess(), itemLocation.getItemStack(player))
                .map(snapshot -> snapshot.getEntries(ValueDeseralizationContext.of(player.level())))
                .orElseGet(List::of);
    }

    /**
     * @return Everything that the Wrench holds, whether it is switched on or not.
     */
    public List<PartConfigEntry> getEntries() {
        return getUnfilteredItems();
    }

    public ItemStack getItemStack() {
        return this.itemLocation.getItemStack(this.player);
    }

    /**
     * @return The configuration that the Wrench holds, if it still holds one.
     */
    public Optional<PartConfigSnapshot> getWrenchSnapshot() {
        return PartConfigHelpers.getSnapshot(getHolderLookupProvider(), getItemStack());
    }

    /**
     * @param id The identifier of an entry.
     * @return If that entry is going to be pasted.
     */
    public boolean isEntryEnabled(String id) {
        return getWrenchSnapshot().map(snapshot -> snapshot.isEnabled(id)).orElse(true);
    }

    protected void toggleEntry(String id) {
        getWrenchSnapshot().ifPresent(snapshot -> setSnapshot(snapshot.withEntryEnabled(id, !snapshot.isEnabled(id))));
    }

    protected void toggleAll() {
        getWrenchSnapshot().ifPresent(snapshot -> {
            // Switching everything back on is what a player wants when anything at all is switched off
            boolean enabled = !snapshot.disabledEntries().isEmpty();
            PartConfigSnapshot updated = snapshot;
            for (PartConfigEntry entry : getUnfilteredItems()) {
                updated = updated.withEntryEnabled(entry.id(), enabled);
            }
            setSnapshot(updated);
        });
    }

    protected void setSnapshot(PartConfigSnapshot snapshot) {
        PartConfigHelpers.setSnapshot(getHolderLookupProvider(), getItemStack(), snapshot);
    }

    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public boolean stillValid(Player player) {
        return !getItemStack().isEmpty() && getItemStack().is(RegistryEntries.ITEM_WRENCH.get());
    }

}
