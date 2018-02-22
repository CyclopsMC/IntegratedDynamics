package org.cyclops.integrateddynamics.proxy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.cyclopscore.client.key.IKeyRegistry;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.model.VariableLoader;
import org.cyclops.integrateddynamics.core.ingredient.ItemMatchType;
import org.cyclops.integrateddynamics.core.inventory.container.slot.SlotVariable;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDiagnosticsPartOverlayRenderer;
import org.lwjgl.input.Keyboard;

/**
 * Client Proxy
 * @author rubensworks
 */
public class ClientProxy extends ClientProxyComponent {

    private static final String KEYBINDING_CATEGORY_NAME = "key.categories." + Reference.MOD_ID;

    public static final KeyBinding FOCUS_LP_SEARCH = new KeyBinding(
            "key." + Reference.MOD_ID + ".logic_programmer_focus_search",
            KeyConflictContext.GUI, KeyModifier.ALT, Keyboard.KEY_F,
            KEYBINDING_CATEGORY_NAME);
    public static final KeyBinding FOCUS_LP_RENAME = new KeyBinding(
            "key." + Reference.MOD_ID + ".logic_programmer_open_rename",
            KeyConflictContext.GUI, KeyModifier.ALT, Keyboard.KEY_R,
            KEYBINDING_CATEGORY_NAME);

    public ClientProxy() {
        super(new CommonProxy());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        ModelLoaderRegistry.registerLoader(new VariableLoader());
        MinecraftForge.EVENT_BUS.register(NetworkDiagnosticsPartOverlayRenderer.getInstance());
    }

    @Override
    public void registerKeyBindings(IKeyRegistry keyRegistry) {
        super.registerKeyBindings(keyRegistry);
        ClientRegistry.registerKeyBinding(FOCUS_LP_SEARCH);
        ClientRegistry.registerKeyBinding(FOCUS_LP_RENAME);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(SlotVariable.VARIABLE_EMPTY);
        for (ItemMatchType itemMatchType : ItemMatchType.values()) {
            event.getMap().registerSprite(itemMatchType.getSlotSpriteName());
        }
    }
}
