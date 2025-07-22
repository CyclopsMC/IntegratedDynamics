package org.cyclops.integrateddynamics.infobook.pageelement;

import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.infobook.IInfoBook;
import org.cyclops.cyclopscore.infobook.InfoBookParser;
import org.cyclops.cyclopscore.infobook.InfoSection;
import org.cyclops.cyclopscore.infobook.pageelement.SectionAppendix;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

/**
 * Aspect appendix.
 * @author rubensworks
 */
public class AspectAppendix extends SectionAppendix<AspectAppendixClient> {

    private final IAspect<?, ?> aspect;
    private ItemStack itemStack;

    public AspectAppendix(IInfoBook infoBook, IAspect<?, ?> aspect) throws InfoBookParser.InvalidAppendixException {
        super(infoBook);
        this.aspect = aspect;
    }

    public IAspect<?, ?> getAspect() {
        return aspect;
    }

    public ItemStack getItemStack() {
        return itemStack;
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
        return 30 + (aspect.hasProperties() ? 10 * (aspect.getPropertyTypes().size() + 1) : 0);
    }

    @Override
    public AspectAppendixClient constructSectionAppendixClient() {
        return new AspectAppendixClient(this);
    }

    @Override
    public void preBakeElement(InfoSection infoSection) {

    }

    @Override
    public void bakeElement(InfoSection infoSection) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        itemStack = registry.writeVariableFacadeItem(new ItemStack(RegistryEntries.ITEM_VARIABLE),
                new AspectVariableFacade(false, 0, aspect), Aspects.REGISTRY);
    }

}
