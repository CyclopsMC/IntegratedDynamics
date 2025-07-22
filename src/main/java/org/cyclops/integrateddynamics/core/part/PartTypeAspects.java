package org.cyclops.integrateddynamics.core.part;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Set;
import java.util.function.Consumer;

/**
 * An abstract {@link IPartType} that can hold aspects.
 * @author rubensworks
 */
public abstract class PartTypeAspects<P extends IPartType<P, S>, S extends IPartState<P>> extends PartTypeConfigurable<P, S> {

    public PartTypeAspects(String name, PartRenderPosition partRenderPosition) {
        super(name, partRenderPosition);
    }

    /**
     * @return All possible aspects that can be used in this part type.
     */
    public Set<IAspect> getAspects() {
        return Aspects.REGISTRY.getAspects(this);
    }

    @Override
    public boolean isUpdate(S state) {
        return !getAspects().isEmpty() || super.isUpdate(state);
    }

    @Override
    public int getConsumptionRate(S state) {
        return 1;
    }

    @Override
    public void loadTooltip(ItemStack itemStack, Consumer<Component> tooltipAdder) {
        super.loadTooltip(itemStack, tooltipAdder);
        if (getAspects().isEmpty()) {
            tooltipAdder.accept(Component.translatable(L10NValues.PART_TOOLTIP_NOASPECTS)
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
