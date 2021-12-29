package org.cyclops.integrateddynamics.core.part;

import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.part.IPartTypeRegistry;
import org.cyclops.integrateddynamics.part.*;

/**
 * Collection of parts types.
 * @author rubensworks
 */
public final class PartTypes {

    public static final IPartTypeRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IPartTypeRegistry.class);

    // Readers
    public static final PartTypeAudioReader AUDIO_READER = new PartTypeAudioReader("audio_reader");
    public static final PartTypeBlockReader BLOCK_READER = new PartTypeBlockReader("block_reader");
    public static final PartTypeEntityReader ENTITY_READER = new PartTypeEntityReader("entity_reader");
    public static final PartTypeExtraDimensionalReader EXTRADIMENSIONAL_READER = new PartTypeExtraDimensionalReader("extradimensional_reader");
    public static final PartTypeFluidReader FLUID_READER = new PartTypeFluidReader("fluid_reader");
    public static final PartTypeInventoryReader INVENTORY_READER = new PartTypeInventoryReader("inventory_reader");
    public static final PartTypeMachineReader MACHINE_READER = new PartTypeMachineReader("machine_reader");
    public static final PartTypeNetworkReader NETWORK_READER = new PartTypeNetworkReader("network_reader");
    public static final PartTypeRedstoneReader REDSTONE_READER = new PartTypeRedstoneReader("redstone_reader");
    public static final PartTypeWorldReader WORLD_READER = new PartTypeWorldReader("world_reader");

    // Writers
    public static final PartTypeAudioWriter AUDIO_WRITER = new PartTypeAudioWriter("audio_writer");
    public static final PartTypeEffectWriter EFFECT_WRITER = new PartTypeEffectWriter("effect_writer");
    public static final PartTypeEntityWriter ENTITY_WRITER = new PartTypeEntityWriter("entity_writer");
    public static final PartTypeMachineWriter MACHINE_WRITER = new PartTypeMachineWriter("machine_writer");
    public static final PartTypeInventoryWriter INVENTORY_WRITER = new PartTypeInventoryWriter("inventory_writer");
    public static final PartTypeRedstoneWriter REDSTONE_WRITER = new PartTypeRedstoneWriter("redstone_writer");

    // Panels
    public static final PartTypePanelLightStatic STATIC_LIGHT_PANEL = new PartTypePanelLightStatic("static_light_panel");
    public static final PartTypePanelLightDynamic DYNAMIC_LIGHT_PANEL = new PartTypePanelLightDynamic("dynamic_light_panel");
    public static final PartTypePanelDisplay DISPLAY_PANEL = new PartTypePanelDisplay("display_panel");

    // Connectors
    public static final PartTypeConnectorMonoDirectional CONNECTOR_MONO = new PartTypeConnectorMonoDirectional("connector_mono_directional");
    public static final PartTypeConnectorOmniDirectional CONNECTOR_OMNI = new PartTypeConnectorOmniDirectional("connector_omni_directional");

    public static void load() {}

    public static void register() {
        REGISTRY.register(AUDIO_READER);
        REGISTRY.register(BLOCK_READER);
        REGISTRY.register(ENTITY_READER);
        REGISTRY.register(EXTRADIMENSIONAL_READER);
        REGISTRY.register(FLUID_READER);
        REGISTRY.register(INVENTORY_READER);
        REGISTRY.register(MACHINE_READER);
        REGISTRY.register(NETWORK_READER);
        REGISTRY.register(REDSTONE_READER);
        REGISTRY.register(WORLD_READER);

        REGISTRY.register(AUDIO_WRITER);
        REGISTRY.register(EFFECT_WRITER);
        REGISTRY.register(ENTITY_WRITER);
        REGISTRY.register(MACHINE_WRITER);
        REGISTRY.register(INVENTORY_WRITER);
        REGISTRY.register(REDSTONE_WRITER);

        REGISTRY.register(STATIC_LIGHT_PANEL);
        REGISTRY.register(DYNAMIC_LIGHT_PANEL);
        REGISTRY.register(DISPLAY_PANEL);

        REGISTRY.register(CONNECTOR_MONO);
        REGISTRY.register(CONNECTOR_OMNI);
    }

}
