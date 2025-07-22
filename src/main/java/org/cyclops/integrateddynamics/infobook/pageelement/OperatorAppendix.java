package org.cyclops.integrateddynamics.infobook.pageelement;

import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoBookParser;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendix;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;

/**
 * Operator appendix.
 * @author rubensworks
 */
public class OperatorAppendix extends SectionAppendix<OperatorAppendixClient> {

    private final IOperator operator;

    public OperatorAppendix(IInfoBook infoBook, IOperator operator) throws InfoBookParser.InvalidAppendixException {
        super(infoBook);
        this.operator = operator;
    }

    public IOperator getOperator() {
        return operator;
    }

    @Override
    protected int getOffsetY() {
        return 5;
    }

    @Override
    protected int getWidth() {
        return 100;
    }

    @Override
    protected int getHeight() {
        return 46 + (operator.getInputTypes().length) * 8;
    }

    @Override
    public OperatorAppendixClient constructSectionAppendixClient() {
        return new OperatorAppendixClient(this);
    }

    @Override
    public void preBakeElement(InfoSection infoSection) {

    }

    @Override
    public void bakeElement(InfoSection infoSection) {

    }

}
