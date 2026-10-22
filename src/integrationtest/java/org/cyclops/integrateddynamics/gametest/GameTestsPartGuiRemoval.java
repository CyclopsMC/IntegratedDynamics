package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerAspectSettings;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipartAspects;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartOffset;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartSettings;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.function.Function;

/**
 * Tests that part guis close, instead of crashing the server, when their part is removed while open.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsPartGuiRemoval {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    protected void testGuiClosesOnPartRemoval(GameTestHelper helper, IPartType<?, ?> partType,
                                              Function<PartPos, MenuProvider> menuProvider, Class<? extends AbstractContainerMenu> menuClass,
                                              boolean removeCable) {
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        // A second part keeps the part container alive after removing the first part
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.EAST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, partType, new ItemStack(partType.getItem()));

        // Open the menu server-side like ServerPlayer#openMenu, without sending the screen packet to the mock client
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        AbstractContainerMenu menu = menuProvider.apply(partPos(helper)).createMenu(1, player.getInventory(), player);
        helper.assertTrue(menuClass.isInstance(menu), "The gui was not opened");
        player.containerMenu = menu;

        helper.runAfterDelay(2, () -> {
            if (removeCable) {
                helper.destroyBlock(POS);
            } else {
                PartHelpers.getPartContainerChecked(helper.getLevel(), helper.absolutePos(POS), Direction.WEST)
                        .removePart(Direction.WEST, null, false, false);
            }
            helper.assertFalse(menu.stillValid(player), "The gui is still valid after removing its part");
            // The player keeps ticking, which must not crash
            helper.succeedWhen(() -> helper.assertTrue(player.containerMenu == player.inventoryMenu, "The gui was not closed"));
        });
    }

    protected PartPos partPos(GameTestHelper helper) {
        return PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartGuiRemovalAspectSettings(GameTestHelper helper) {
        testGuiClosesOnPartRemoval(helper, PartTypes.INVENTORY_READER,
                Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT::getPropertiesContainerProvider,
                ContainerAspectSettings.class, false);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartGuiRemovalAspectSettingsCable(GameTestHelper helper) {
        testGuiClosesOnPartRemoval(helper, PartTypes.INVENTORY_READER,
                Aspects.Read.Inventory.OBJECT_ITEM_STACK_SLOT::getPropertiesContainerProvider,
                ContainerAspectSettings.class, true);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartGuiRemovalReader(GameTestHelper helper) {
        testGuiClosesOnPartRemoval(helper, PartTypes.INVENTORY_READER,
                pos -> PartTypes.INVENTORY_READER.getContainerProvider(pos).get(),
                ContainerMultipartAspects.class, false);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartGuiRemovalWriter(GameTestHelper helper) {
        testGuiClosesOnPartRemoval(helper, PartTypes.REDSTONE_WRITER,
                pos -> PartTypes.REDSTONE_WRITER.getContainerProvider(pos).get(),
                ContainerMultipartAspects.class, false);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartGuiRemovalPanel(GameTestHelper helper) {
        testGuiClosesOnPartRemoval(helper, PartTypes.DISPLAY_PANEL,
                pos -> PartTypes.DISPLAY_PANEL.getContainerProvider(pos).get(),
                ContainerMultipart.class, false);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartGuiRemovalSettings(GameTestHelper helper) {
        testGuiClosesOnPartRemoval(helper, PartTypes.INVENTORY_READER,
                pos -> PartTypes.INVENTORY_READER.getContainerProviderSettings(pos).get(),
                ContainerPartSettings.class, false);
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testPartGuiRemovalOffsets(GameTestHelper helper) {
        testGuiClosesOnPartRemoval(helper, PartTypes.INVENTORY_READER,
                pos -> PartTypes.INVENTORY_READER.getContainerProviderOffsets(pos).get(),
                ContainerPartOffset.class, false);
    }

}
