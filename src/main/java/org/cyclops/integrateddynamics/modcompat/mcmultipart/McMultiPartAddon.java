package org.cyclops.integrateddynamics.modcompat.mcmultipart;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.multipart.IMultipartRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.part.PartTypes;

import java.util.function.Predicate;

/**
 * @author rubensworks
 */
@MCMPAddon
public class McMultiPartAddon implements IMCMPAddon {

    public void registerParts(IMultipartRegistry registry) {
        if (McMultiPartModCompat.ENABLED) {
            registry.registerPartWrapper(BlockCable.getInstance(), new PartBlockCable(BlockCable.getInstance()));
            registry.registerStackWrapper(Item.getItemFromBlock(BlockCable.getInstance()), new Predicate<ItemStack>() {
                @Override
                public boolean test(ItemStack itemStack) {
                    return true;
                }
            }, BlockCable.getInstance());

            for (IPartType partType : PartTypes.REGISTRY.getPartTypes()) {
                registry.registerPartWrapper(partType.getBlock(), new PartPartType(partType.getBlock(), partType));
                registry.registerStackWrapper(partType.getItem(), new Predicate<ItemStack>() {
                    @Override
                    public boolean test(ItemStack itemStack) {
                        return true;
                    }
                }, partType.getBlock());
            }

        }
    }

}
