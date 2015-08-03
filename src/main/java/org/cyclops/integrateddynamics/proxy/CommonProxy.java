package org.cyclops.integrateddynamics.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.network.packet.ActionLabelPacket;
import org.cyclops.integrateddynamics.core.network.packet.AllLabelsPacket;
import org.cyclops.integrateddynamics.network.packet.ItemStackRenamePacket;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerActivateOperatorPacket;

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
        packetHandler.register(LogicProgrammerActivateOperatorPacket.class);
        packetHandler.register(ActionLabelPacket.class);
        packetHandler.register(AllLabelsPacket.class);
        packetHandler.register(ItemStackRenamePacket.class);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
