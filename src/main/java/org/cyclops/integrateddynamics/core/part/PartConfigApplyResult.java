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
     * @return All messages describing this outcome, which can be shown to the player.
     */
    public List<MutableComponent> getMessages() {
        List<MutableComponent> messages = Lists.newArrayList();
        messages.add(Component.translatable("item.integrateddynamics.wrench.mode.config.pasted",
                this.appliedProperties, this.cardsPasted));
        if (this.offsetFailed) {
            messages.add(Component.translatable("item.integrateddynamics.wrench.mode.offset.fail"));
        }
        if (this.cardsSkipped > 0) {
            messages.add(Component.translatable("item.integrateddynamics.wrench.mode.config.cards_skipped",
                    this.cardsSkipped, this.missingBlanks));
        }
        return messages;
    }

    /**
     * @return All messages describing this outcome, joined into a single line.
     */
    public MutableComponent getMessage() {
        MutableComponent message = Component.empty();
        boolean first = true;
        for (MutableComponent part : getMessages()) {
            if (!first) {
                message.append(" ");
            }
            first = false;
            message.append(part);
        }
        return message;
    }

}
