package org.cyclops.integrateddynamics.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.network.packet.ActionLabelPacket;
import org.cyclops.integrateddynamics.core.network.packet.AllLabelsPacket;
import org.cyclops.integrateddynamics.network.packet.ItemStackRenamePacket;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerActivateElementPacket;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeListValueChangedPacket;
import org.cyclops.integrateddynamics.network.packet.LogicProgrammerValueTypeValueChangedPacket;

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
        packetHandler.register(LogicProgrammerValueTypeValueChangedPacket.class);
        packetHandler.register(ActionLabelPacket.class);
        packetHandler.register(AllLabelsPacket.class);
        packetHandler.register(ItemStackRenamePacket.class);
        packetHandler.register(LogicProgrammerValueTypeListValueChangedPacket.class);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
