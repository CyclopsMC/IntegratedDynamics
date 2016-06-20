package org.cyclops.integrateddynamics.proxy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.cyclops.cyclopscore.client.key.IKeyRegistry;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.model.VariableLoader;
import org.lwjgl.input.Keyboard;

/**
 * Client Proxy
 * @author rubensworks
 */
public class ClientProxy extends ClientProxyComponent {

    private static final String KEYBINDING_CATEGORY_NAME = "key.categories." + Reference.MOD_ID;

    public static final KeyBinding FOCUS_LP_SEARCH = new KeyBinding(
            "key." + Reference.MOD_ID + ".logicProgrammerFocusSearch",
            KeyConflictContext.GUI, KeyModifier.ALT, Keyboard.KEY_F,
            KEYBINDING_CATEGORY_NAME);
    public static final KeyBinding FOCUS_LP_RENAME = new KeyBinding(
            "key." + Reference.MOD_ID + ".logicProgrammerOpenRename",
            KeyConflictContext.GUI, KeyModifier.ALT, Keyboard.KEY_R,
            KEYBINDING_CATEGORY_NAME);

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBase getMod() {
        return IntegratedDynamics._instance;
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        ModelLoaderRegistry.registerLoader(new VariableLoader());
    }

    @Override
    public void registerKeyBindings(IKeyRegistry keyRegistry) {
        super.registerKeyBindings(keyRegistry);
        ClientRegistry.registerKeyBinding(FOCUS_LP_SEARCH);
        ClientRegistry.registerKeyBinding(FOCUS_LP_RENAME);
    }
}
