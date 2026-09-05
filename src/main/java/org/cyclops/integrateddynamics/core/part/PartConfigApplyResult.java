package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * The outcome of pasting a {@link PartConfigSnapshot} onto a part.
 * @author rubensworks
 */
@Getter
public class PartConfigApplyResult {

    @Setter
    private boolean partSettingsApplied = false;
    @Setter
    private boolean offsetFailed = false;
    private int appliedProperties = 0;
    private int skippedProperties = 0;
    private int cardsPasted = 0;
    private int cardsSkipped = 0;
    @Setter
    private int missingBlanks = 0;

    public void addAppliedProperties(int amount) {
        this.appliedProperties += amount;
    }

    public void addSkippedProperties(int amount) {
        this.skippedProperties += amount;
    }

    public void addCardsPasted(int amount) {
        this.cardsPasted += amount;
    }

    public void addCardsSkipped(int amount) {
        this.cardsSkipped += amount;
    }

    /**
     * @return A single line summarising what was applied.
     */
    public MutableComponent getMessage() {
        // Only report what was actually applied, so that the counts never contradict what the player sees
        List<Component> applied = Lists.newArrayList();
        if (this.partSettingsApplied) {
            applied.add(Component.translatable("item.integrateddynamics.wrench.mode.config.pasted.part_settings"));
        }
        if (this.appliedProperties > 0) {
            applied.add(Component.translatable("item.integrateddynamics.wrench.mode.config.pasted.aspect_properties",
                    this.appliedProperties));
        }
        if (this.cardsPasted > 0) {
            applied.add(Component.translatable("item.integrateddynamics.wrench.mode.config.pasted.variable_cards",
                    this.cardsPasted));
        }
        if (applied.isEmpty()) {
            return Component.translatable("item.integrateddynamics.wrench.mode.config.pasted.nothing");
        }
        MutableComponent joined = Component.empty();
        for (int i = 0; i < applied.size(); i++) {
            if (i > 0) {
                joined.append(", ");
            }
            joined.append(applied.get(i));
        }
        return Component.translatable("item.integrateddynamics.wrench.mode.config.pasted", joined);
    }

    /**
     * These are kept apart from {@link #getMessage()},
     * as the two together are too long for the single line that the action bar has.
     * @return What did not go as the player intended, if anything.
     */
    public List<MutableComponent> getWarnings() {
        List<MutableComponent> warnings = Lists.newArrayList();
        if (this.offsetFailed) {
            warnings.add(Component.translatable("item.integrateddynamics.wrench.mode.offset.fail"));
        }
        if (this.cardsSkipped > 0) {
            warnings.add(Component.translatable("item.integrateddynamics.wrench.mode.config.cards_skipped",
                    this.cardsSkipped, this.missingBlanks));
        }
        return warnings;
    }

}
