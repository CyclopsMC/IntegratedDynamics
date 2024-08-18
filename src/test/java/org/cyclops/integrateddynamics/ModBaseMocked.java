package org.cyclops.integrateddynamics;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.helper.LoggerHelper;
import org.cyclops.cyclopscore.helper.ModHelpersNeoForge;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.modcompat.ModCompatLoader;
import org.cyclops.cyclopscore.proxy.ICommonProxyCommon;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author rubensworks
 */
public class ModBaseMocked implements IModBase {
    @Override
    public String getModId() {
        return "";
    }

    @Override
    public IModHelpers getModHelpers() {
        return ModHelpersNeoForge.INSTANCE;
    }

    @Override
    public ConfigHandlerCommon getConfigHandler() {
        return null;
    }

    @Override
    public LoggerHelper getLoggerHelper() {
        return null;
    }

    @Override
    public ICommonProxyCommon getProxy() {
        return null;
    }

    @Override
    public ModCompatLoader getModCompatLoader() {
        return null;
    }

    @Nullable
    @Override
    public CreativeModeTab getDefaultCreativeTab() {
        return null;
    }

    @Override
    public void registerDefaultCreativeTabEntry(ItemStack itemStack, CreativeModeTab.TabVisibility visibility) {

    }

    @Override
    public List<Pair<ItemStack, CreativeModeTab.TabVisibility>> getDefaultCreativeTabEntries() {
        return List.of();
    }
}
