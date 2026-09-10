package org.cyclops.integrateddynamics.core.part;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * The outcome of pasting a {@link PartConfigSnapshot} onto a part.
 * @author rubensworks
 */
public class PartConfigApplyResult {

    private boolean partSettingsApplied = false;
    private boolean offsetFailed = false;
    private int appliedProperties = 0;
    private int skippedProperties = 0;
    private int cardsPasted = 0;
    private int cardsSkipped = 0;
    private int missingBlanks = 0;
    private int appliedMaxOffset = 0;
    private int missingMaxOffset = 0;
    private final List<Component> extraApplied = Lists.newArrayList();
    private final List<Component> extraWarnings = Lists.newArrayList();

    public boolean isPartSettingsApplied() {
        return this.partSettingsApplied;
    }

    public void setPartSettingsApplied(boolean partSettingsApplied) {
        this.partSettingsApplied = partSettingsApplied;
    }

    public boolean isOffsetFailed() {
        return this.offsetFailed;
    }

    public void setOffsetFailed(boolean offsetFailed) {
        this.offsetFailed = offsetFailed;
    }

    public int getAppliedProperties() {
        return this.appliedProperties;
    }

    public void addAppliedProperties(int amount) {
        this.appliedProperties += amount;
    }

    public int getSkippedProperties() {
        return this.skippedProperties;
    }

    public void addSkippedProperties(int amount) {
        this.skippedProperties += amount;
    }

    public int getCardsPasted() {
        return this.cardsPasted;
    }

    public void addCardsPasted(int amount) {
        this.cardsPasted += amount;
    }

    public int getCardsSkipped() {
        return this.cardsSkipped;
    }

    public void addCardsSkipped(int amount) {
        this.cardsSkipped += amount;
    }

    public int getMissingBlanks() {
        return this.missingBlanks;
    }

    public void setMissingBlanks(int missingBlanks) {
        this.missingBlanks = missingBlanks;
    }

    /**
     * @return By how much the maximum offset of the part was increased.
     */
    public int getAppliedMaxOffset() {
        return this.appliedMaxOffset;
    }

    public void setAppliedMaxOffset(int appliedMaxOffset) {
        this.appliedMaxOffset = appliedMaxOffset;
    }

    /**
     * @return The offset enhancement value that the player was short of.
     */
    public int getMissingMaxOffset() {
        return this.missingMaxOffset;
    }

    public void setMissingMaxOffset(int missingMaxOffset) {
        this.missingMaxOffset = missingMaxOffset;
    }

    /**
     * Report something that a part type pasted itself, to be mentioned alongside the rest.
     * @param applied What was pasted, phrased to fit in a comma separated list.
     */
    public void addApplied(Component applied) {
        this.extraApplied.add(applied);
    }

    /**
     * Report something that a part type could not paste.
     * @param warning What did not go as the player intended.
     */
    public void addWarning(Component warning) {
        this.extraWarnings.add(warning);
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
        if (this.appliedMaxOffset > 0) {
            applied.add(Component.translatable("item.integrateddynamics.wrench.mode.config.pasted.max_offset",
                    this.appliedMaxOffset));
        }
        applied.addAll(this.extraApplied);
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
        if (this.missingMaxOffset > 0) {
            warnings.add(Component.translatable("item.integrateddynamics.wrench.mode.config.enhancements_missing",
                    this.missingMaxOffset));
        }
        if (this.cardsSkipped > 0) {
            warnings.add(Component.translatable("item.integrateddynamics.wrench.mode.config.cards_skipped",
                    this.cardsSkipped, this.missingBlanks));
        }
        this.extraWarnings.forEach(warning -> warnings.add(warning.copy()));
        return warnings;
    }

}
