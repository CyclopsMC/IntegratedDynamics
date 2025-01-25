package org.cyclops.integrateddynamics.proxy;

import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.network.IPacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.network.packet.*;

/**
 * Common proxy
 * @author rubensworks
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBaseNeoForge getMod() {
        return IntegratedDynamics._instance;
    }

    @Override
    public void registerPackets(IPacketHandler packetHandler) {
        super.registerPackets(packetHandler);

        // Register packets.
        packetHandler.register(LogicProgrammerActivateElementPacket.class, LogicProgrammerActivateElementPacket.ID, LogicProgrammerActivateElementPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeStringValueChangedPacket.class, LogicProgrammerValueTypeStringValueChangedPacket.ID, LogicProgrammerValueTypeStringValueChangedPacket.CODEC);
        packetHandler.register(ActionLabelPacket.class, ActionLabelPacket.TYPE, ActionLabelPacket.CODEC);
        packetHandler.register(AllLabelsPacket.class, AllLabelsPacket.ID, AllLabelsPacket.CODEC);
        packetHandler.register(ItemStackRenamePacket.class, ItemStackRenamePacket.ID, ItemStackRenamePacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeListValueChangedPacket.class, LogicProgrammerValueTypeListValueChangedPacket.ID, LogicProgrammerValueTypeListValueChangedPacket.CODEC);
        packetHandler.register(LogicProgrammerLabelPacket.class, LogicProgrammerLabelPacket.ID, LogicProgrammerLabelPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeOperatorValueChangedPacket.class, LogicProgrammerValueTypeOperatorValueChangedPacket.ID, LogicProgrammerValueTypeOperatorValueChangedPacket.CODEC);
        packetHandler.register(NetworkDiagnosticsSubscribePacket.class, NetworkDiagnosticsSubscribePacket.ID, NetworkDiagnosticsSubscribePacket.CODEC);
        packetHandler.register(NetworkDiagnosticsNetworkPacket.class, NetworkDiagnosticsNetworkPacket.ID, NetworkDiagnosticsNetworkPacket.CODEC);
        packetHandler.register(NetworkDiagnosticsTriggerClient.class, NetworkDiagnosticsTriggerClient.ID, NetworkDiagnosticsTriggerClient.CODEC);
        packetHandler.register(PlayerTeleportPacket.class, PlayerTeleportPacket.ID, PlayerTeleportPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeSlottedValueChangedPacket.class, LogicProgrammerValueTypeSlottedValueChangedPacket.ID, LogicProgrammerValueTypeSlottedValueChangedPacket.CODEC);
        packetHandler.register(LogicProgrammerSetElementInventory.class, LogicProgrammerSetElementInventory.ID, LogicProgrammerSetElementInventory.CODEC);
        packetHandler.register(LogicProgrammerValueTypeIngredientsValueChangedPacket.class, LogicProgrammerValueTypeIngredientsValueChangedPacket.ID, LogicProgrammerValueTypeIngredientsValueChangedPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeRecipeValueChangedPacket.class, LogicProgrammerValueTypeRecipeValueChangedPacket.ID, LogicProgrammerValueTypeRecipeValueChangedPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket.class, LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket.ID, LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket.CODEC);
        packetHandler.register(SpeakTextPacket.class, SpeakTextPacket.ID, SpeakTextPacket.CODEC);
        packetHandler.register(LogicProgrammerValueTypeBooleanValueChangedPacket.class, LogicProgrammerValueTypeBooleanValueChangedPacket.ID, LogicProgrammerValueTypeBooleanValueChangedPacket.CODEC);
        packetHandler.register(PartOffsetsSubscribePacket.class, PartOffsetsSubscribePacket.ID, PartOffsetsSubscribePacket.CODEC);
        packetHandler.register(PartOffsetsDataPacket.class, PartOffsetsDataPacket.ID, PartOffsetsDataPacket.CODEC);

        IntegratedDynamics.clog("Registered packet handler.");
    }

}
