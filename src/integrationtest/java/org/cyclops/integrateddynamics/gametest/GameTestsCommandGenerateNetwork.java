package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.command.CommandGenerateNetwork;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;

import java.util.Objects;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsCommandGenerateNetwork {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);
    public static final int SIZE = 3;

    @GameTest(template = TEMPLATE_EMPTY)
    public void testGenerateNetworkDisplayPanels(GameTestHelper helper) {
        CommandGenerateNetwork.NetworkGenerationHelper.generateDisplayPanelNetwork(helper.getLevel(), helper.absolutePos(POS), SIZE);

        helper.succeedWhen(() -> {
            int i = 0;
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    PartPos partPos = PartPos.of(helper.getLevel(), helper.absolutePos(POS.offset(x, y, 0)), Direction.NORTH);
                    PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
                    helper.assertTrue(partStateHolder != null, "Display panel is missing");
                    PartTypePanelDisplay.State state = (PartTypePanelDisplay.State) partStateHolder.getState();
                    helper.assertTrue(Objects.equals(state.getDisplayValue(), ValueTypeInteger.ValueInteger.of(i)), "Display panel value is incorrect");
                    i++;
                }
            }
        });
    }

}
