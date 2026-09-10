package org.cyclops.integrateddynamics.proxy;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import org.cyclops.cyclopscore.client.key.IKeyRegistry;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.render.level.PartOffsetsOverlayRenderer;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDataClient;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnosticsPartOverlayRenderer;
import org.cyclops.integrateddynamics.core.network.diagnostics.http.DiagnosticsWebServer;
import org.lwjgl.glfw.GLFW;

/**
 * Client Proxy
 * @author rubensworks
 */
public class ClientProxy extends ClientProxyComponent {

    public static DiagnosticsWebServer DIAGNOSTICS_SERVER;

    private static final String KEYBINDING_CATEGORY_NAME = "key.categories." + Reference.MOD_ID;

    public static final KeyMapping FOCUS_LP_SEARCH = new KeyMapping(
            "key." + Reference.MOD_ID + ".logic_programmer_focus_search",
            KeyConflictContext.GUI, KeyModifier.ALT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F,
            KEYBINDING_CATEGORY_NAME);
    public static final KeyMapping FOCUS_LP_RENAME = new KeyMapping(
            "key." + Reference.MOD_ID + ".logic_programmer_open_rename",
            KeyConflictContext.GUI, KeyModifier.ALT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R,
            KEYBINDING_CATEGORY_NAME);

    public ClientProxy() {
        super(new CommonProxy());
        IntegratedDynamics._instance.getModEventBus().addListener(this::onPostTextureStitch);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
    }

    @Override
    public ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        NeoForge.EVENT_BUS.register(NetworkDiagnosticsPartOverlayRenderer.getInstance());
        NeoForge.EVENT_BUS.register(PartOffsetsOverlayRenderer.getInstance());
    }

    @Override
    public void registerKeyBindings(IKeyRegistry keyRegistry, RegisterKeyMappingsEvent event) {
        super.registerKeyBindings(keyRegistry, event);
        event.register(FOCUS_LP_SEARCH);
        event.register(FOCUS_LP_RENAME);
    }

    public void onPostTextureStitch(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            event.getAtlas().getSprite(SlotVariable.VARIABLE_EMPTY);
        }
    }

    public void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (DIAGNOSTICS_SERVER != null) {
            IntegratedDynamics.clog("Stopping diagnostics server...");
            NetworkDiagnosticsPartOverlayRenderer.getInstance().clearPositions();
            NetworkDataClient.clearNetworkData();
            PartOffsetsOverlayRenderer.getInstance().clear();
            DIAGNOSTICS_SERVER.deinitialize();
            DIAGNOSTICS_SERVER = null;
            IntegratedDynamics.clog("Stopped diagnostics server");
        }
    }
}
