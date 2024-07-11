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
        packetHandler.register(LogicProgrammerActivateElementPacket.ID, LogicProgrammerActivateElementPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeStringValueChangedPacket.ID, LogicProgrammerValueTypeStringValueChangedPacket.CODEC);
        packetHandler.register(ActionLabelPacket.TYPE, ActionLabelPacket.CODEC);
        packetHandler.register(AllLabelsPacket.ID, AllLabelsPacket.CODEC);
        packetHandler.register(ItemStackRenamePacket.ID, ItemStackRenamePacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeListValueChangedPacket.ID, LogicProgrammerValueTypeListValueChangedPacket.CODEC);
        packetHandler.register(LogicProgrammerLabelPacket.ID, LogicProgrammerLabelPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeOperatorValueChangedPacket.ID, LogicProgrammerValueTypeOperatorValueChangedPacket.CODEC);
        packetHandler.register(NetworkDiagnosticsSubscribePacket.ID, NetworkDiagnosticsSubscribePacket.CODEC);
        packetHandler.register(NetworkDiagnosticsNetworkPacket.ID, NetworkDiagnosticsNetworkPacket.CODEC);
        packetHandler.register(NetworkDiagnosticsTriggerClient.ID, NetworkDiagnosticsTriggerClient.CODEC);
        packetHandler.register(PlayerTeleportPacket.ID, PlayerTeleportPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeSlottedValueChangedPacket.ID, LogicProgrammerValueTypeSlottedValueChangedPacket.CODEC);
        packetHandler.register(LogicProgrammerSetElementInventory.ID, LogicProgrammerSetElementInventory.CODEC);
        packetHandler.register(LogicProgrammerValueTypeIngredientsValueChangedPacket.ID, LogicProgrammerValueTypeIngredientsValueChangedPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeRecipeValueChangedPacket.ID, LogicProgrammerValueTypeRecipeValueChangedPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket.ID, LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket.CODEC);
        packetHandler.register(SpeakTextPacket.ID, SpeakTextPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeBooleanValueChangedPacket.ID, LogicProgrammerValueTypeBooleanValueChangedPacket.CODEC);
        packetHandler.register(PartOffsetsSubscribePacket.ID, PartOffsetsSubscribePacket.CODEC);
        packetHandler.register(PartOffsetsDataPacket.ID, PartOffsetsDataPacket.CODEC);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
