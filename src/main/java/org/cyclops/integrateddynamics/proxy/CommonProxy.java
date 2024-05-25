package org.cyclops.integrateddynamics.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
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
        packetHandler.register(LogicProgrammerActivateElementPacket.ID, LogicProgrammerActivateElementPacket::new);
        packetHandler.register(LogicProgrammerValueTypeStringValueChangedPacket.ID, LogicProgrammerValueTypeStringValueChangedPacket::new);
        packetHandler.register(ActionLabelPacket.ID, ActionLabelPacket::new);
        packetHandler.register(AllLabelsPacket.ID, AllLabelsPacket::new);
        packetHandler.register(ItemStackRenamePacket.ID, ItemStackRenamePacket::new);
        packetHandler.register(LogicProgrammerValueTypeListValueChangedPacket.ID, LogicProgrammerValueTypeListValueChangedPacket::new);
        packetHandler.register(LogicProgrammerLabelPacket.ID, LogicProgrammerLabelPacket::new);
        packetHandler.register(LogicProgrammerValueTypeOperatorValueChangedPacket.ID, LogicProgrammerValueTypeOperatorValueChangedPacket::new);
        packetHandler.register(NetworkDiagnosticsSubscribePacket.ID, NetworkDiagnosticsSubscribePacket::new);
        packetHandler.register(NetworkDiagnosticsNetworkPacket.ID, NetworkDiagnosticsNetworkPacket::new);
        packetHandler.register(NetworkDiagnosticsTriggerClient.ID, NetworkDiagnosticsTriggerClient::new);
        packetHandler.register(PlayerTeleportPacket.ID, PlayerTeleportPacket::new);
        packetHandler.register(LogicProgrammerValueTypeSlottedValueChangedPacket.ID, LogicProgrammerValueTypeSlottedValueChangedPacket::new);
        packetHandler.register(LogicProgrammerSetElementInventory.ID, LogicProgrammerSetElementInventory::new);
        packetHandler.register(LogicProgrammerValueTypeIngredientsValueChangedPacket.ID, LogicProgrammerValueTypeIngredientsValueChangedPacket::new);
        packetHandler.register(LogicProgrammerValueTypeRecipeValueChangedPacket.ID, LogicProgrammerValueTypeRecipeValueChangedPacket::new);
        packetHandler.register(LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket.ID, LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket::new);
        packetHandler.register(SpeakTextPacket.ID, SpeakTextPacket::new);
        packetHandler.register(LogicProgrammerValueTypeBooleanValueChangedPacket.ID, LogicProgrammerValueTypeBooleanValueChangedPacket::new);
        packetHandler.register(PartOffsetsSubscribePacket.ID, PartOffsetsSubscribePacket::new);
        packetHandler.register(PartOffsetsDataPacket.ID, PartOffsetsDataPacket::new);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
