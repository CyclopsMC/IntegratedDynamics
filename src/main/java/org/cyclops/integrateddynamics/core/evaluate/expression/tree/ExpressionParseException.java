package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import net.minecraft.network.chat.MutableComponent;

/**
 * An error in the text of an expression.
 * @author rubensworks
 */
public class ExpressionParseException extends Exception {

    private final int position;
    private final MutableComponent component;

    /**
     * @param position The character offset in the text where the error was found.
     * @param component A human-readable message.
     */
    public ExpressionParseException(int position, MutableComponent component) {
        super(component.getString());
        this.position = position;
        this.component = component;
    }

    public int getPosition() {
        return position;
    }

    public MutableComponent getComponent() {
        return component;
    }

}
