package org.cyclops.integrateddynamics.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.config.configurable.ConfigurableItem;
import org.cyclops.cyclopscore.config.extendedconfig.ExtendedConfig;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.core.part.aspect.IAspect;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.List;

/**
 * Item for storing variable references.
 * @author rubensworks
 */
public class ItemVariable extends ConfigurableItem {

    private static ItemVariable _instance = null;

    /**
     * Get the unique instance.
     * @return The instance.
     */
    public static ItemVariable getInstance() {
        return _instance;
    }

    /**
     * Make a new item instance.
     *
     * @param eConfig Config for this blockState.
     */
    public ItemVariable(ExtendedConfig eConfig) {
        super(eConfig);
    }

    @SuppressWarnings("rawtypes")
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        Pair<Integer, IAspect> aspectInfo = Aspects.REGISTRY.readAspect(itemStack);
        if(aspectInfo != null) {
            list.add(L10NHelpers.localize("item.items.integrateddynamics.variable.type",
                    aspectInfo.getRight().getValueType().getTypeName()));
            list.add(L10NHelpers.localize("item.items.integrateddynamics.variable.aspectName",
                    L10NHelpers.localize(aspectInfo.getRight().getUnlocalizedName())));
            list.add(L10NHelpers.localize("item.items.integrateddynamics.variable.partId",
                    aspectInfo.getLeft()));
        }
        super.addInformation(itemStack, entityPlayer, list, par4);
    }
}
