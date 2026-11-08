package org.cyclops.integrateddynamics.core.evaluate.expression.tree;

import net.minecraft.network.chat.MutableComponent;

/**
 * An error in the text of an expression.
 * @author rubensworks
 */
public class ExpressionParseException extends Exception {

    private final int position;
    private final int end;
    private final MutableComponent component;

    /**
     * @param position The character offset in the text where the error was found.
     * @param component A human-readable message.
     */
    public ExpressionParseException(int position, MutableComponent component) {
        this(position, position, component);
    }

    /**
     * @param position The character offset in the text where the error was found.
     * @param end The offset after the part of the text with the error, such as an unknown name.
     * @param component A human-readable message.
     */
    public ExpressionParseException(int position, int end, MutableComponent component) {
        super(component.getString());
        this.position = position;
        this.end = Math.max(position, end);
        this.component = component;
    }

    public int getPosition() {
        return position;
    }

    /**
     * @return The offset after the part of the text with the error, or the position if it is not known.
     */
    public int getEnd() {
        return end;
    }

    public MutableComponent getComponent() {
        return component;
    }

}
