package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsMenrilBerries {

    public static final String TEMPLATE_EMPTY = "empty10";

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMenrilBerriesInBerryTag(GameTestHelper helper) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "menril_berries")));
        helper.assertTrue(stack.is(Tags.Items.FOODS_BERRY), "Menril berries are not in the c:foods/berry tag");
        helper.assertTrue(stack.is(Tags.Items.FOODS), "Menril berries are not in the c:foods tag");
        helper.succeed();
    }

}
