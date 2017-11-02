package org.cyclops.integrateddynamics.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.network.packet.ActionLabelPacket;
import org.cyclops.integrateddynamics.core.network.packet.AllLabelsPacket;
import org.cyclops.integrateddynamics.network.packet.*;

/**
 * Common proxy
 * @author rubensworks
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);

        // Register packets.
        packetHandler.register(LogicProgrammerActivateElementPacket.class);
        packetHandler.register(LogicProgrammerValueTypeStringValueChangedPacket.class);
        packetHandler.register(ActionLabelPacket.class);
        packetHandler.register(AllLabelsPacket.class);
        packetHandler.register(ItemStackRenamePacket.class);
        packetHandler.register(LogicProgrammerValueTypeListValueChangedPacket.class);
        packetHandler.register(LogicProgrammerLabelPacket.class);
        packetHandler.register(LogicProgrammerValueTypeOperatorValueChangedPacket.class);
        packetHandler.register(NetworkDiagnosticsSubscribePacket.class);
        packetHandler.register(NetworkDiagnosticsNetworkPacket.class);
        packetHandler.register(NetworkDiagnosticsOpenClient.class);
        packetHandler.register(PlayerTeleportPacket.class);
        packetHandler.register(LogicProgrammerValueTypeSlottedValueChangedPacket.class);
        packetHandler.register(LogicProgrammerSetElementInventory.class);
        packetHandler.register(LogicProgrammerValueTypeIngredientsValueChangedPacket.class);
        packetHandler.register(LogicProgrammerValueTypeRecipeValueChangedPacket.class);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
