package org.cyclops.integrateddynamics.modcompat.waila;

import mcp.mobius.waila.api.IWailaRegistrar;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.tileentity.TileMultipartTicking;
import org.cyclops.integrateddynamics.tileentity.TileDryingBasin;
import org.cyclops.integrateddynamics.tileentity.TileProxy;
import org.cyclops.integrateddynamics.tileentity.TileSqueezer;

/**
 * Waila support class.
 * @author rubensworks
 *
 */
public class Waila {
    
    /**
     * Waila callback.
     * @param registrar The Waila registrar.
     */
    public static void callbackRegister(IWailaRegistrar registrar){
        registrar.addConfig(Reference.MOD_NAME, getPartConfigId(), L10NHelpers.localize("gui." + Reference.MOD_ID + ".waila.partConfig"));
        registrar.addConfig(Reference.MOD_NAME, getProxyConfigId(), L10NHelpers.localize("gui." + Reference.MOD_ID + ".waila.proxyConfig"));
        registrar.addConfig(Reference.MOD_NAME, getDryingBasinConfigId(), L10NHelpers.localize("gui." + Reference.MOD_ID + ".waila.dryingBasinConfig"));
        registrar.registerBodyProvider(new PartDataProvider(), TileMultipartTicking.class);
        //registrar.registerBodyProvider(new PartDataProvider(), IMultipartContainer.class); // TODO: enable when MCMP is back
        registrar.registerBodyProvider(new ProxyDataProvider(), TileProxy.class);
        registrar.registerBodyProvider(new DryingBasinDataProvider(), TileDryingBasin.class);
        registrar.registerBodyProvider(new SqueezerDataProvider(), TileSqueezer.class);
    }
    
    /**
     * Part config ID.
     * @return The config ID.
     */
    public static String getPartConfigId() {
        return Reference.MOD_ID + ".part";
    }

    /**
     * Proxy config ID.
     * @return The config ID.
     */
    public static String getProxyConfigId() {
        return Reference.MOD_ID + ".proxy";
    }

    /**
     * Proxy config ID.
     * @return The config ID.
     */
    public static String getDryingBasinConfigId() {
        return Reference.MOD_ID + ".dryingBasin";
    }
    
}
