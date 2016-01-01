package org.cyclops.integrateddynamics;

import com.google.common.collect.Maps;
import net.minecraft.command.ICommand;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.client.gui.GuiHandler;
import org.cyclops.cyclopscore.command.CommandMod;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.config.extendedconfig.BlockItemConfigReference;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.init.ItemCreativeTab;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.persist.world.GlobalCounters;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProviderRegistry;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRendererRegistry;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRendererRegistry;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueCastRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeLightLevelRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementTypeRegistry;
import org.cyclops.integrateddynamics.api.part.IPartTypeRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.block.BlockCableConfig;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRendererRegistry;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRendererRegistry;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;
import org.cyclops.integrateddynamics.command.CommandTest;
import org.cyclops.integrateddynamics.core.TickHandler;
import org.cyclops.integrateddynamics.core.client.gui.ExtendedGuiHandler;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviderRegistry;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.ProxyVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorRegistry;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypeRegistry;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.part.PartTypeRegistry;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;
import org.cyclops.integrateddynamics.core.test.TestHelpers;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Map;

/**
 * The main mod class of IntegratedDynamics.
 * @author rubensworks
 *
 */
@Mod(modid = Reference.MOD_ID,
     name = Reference.MOD_NAME,
     useMetadata = true,
     version = Reference.MOD_VERSION,
     dependencies = Reference.MOD_DEPENDENCIES,
     guiFactory = "org.cyclops.integrateddynamics.GuiConfigOverview$ExtendedConfigGuiFactory"
)
public class IntegratedDynamics extends ModBaseVersionable {

    /**
     * The proxy of this mod, depending on 'side' a different proxy will be inside this field.
     * @see net.minecraftforge.fml.common.SidedProxy
     */
    @SidedProxy(clientSide = "org.cyclops.integrateddynamics.proxy.ClientProxy", serverSide = "org.cyclops.integrateddynamics.proxy.CommonProxy")
    public static ICommonProxy proxy;

    /**
     * The unique instance of this mod.
     */
    @Mod.Instance(value = Reference.MOD_ID)
    public static IntegratedDynamics _instance;

    public static GlobalCounters globalCounters = null;

    public IntegratedDynamics() {
        super(Reference.MOD_ID, Reference.MOD_NAME, Reference.MOD_VERSION);

        // Register world storages
        registerWorldStorage(NetworkWorldStorage.getInstance(this));
        registerWorldStorage(globalCounters = new GlobalCounters(this));
        registerWorldStorage(LabelsWorldStorage.getInstance(this));
    }

    @Override
    protected GuiHandler constructGuiHandler() {
        return new ExtendedGuiHandler(this);
    }

    @Override
    protected RecipeHandler constructRecipeHandler() {
        return new ExtendedRecipeHandler(this
                // TODO
        );
    }

    @Override
    protected ICommand constructBaseCommand() {
        Map<String, ICommand> commands = Maps.newHashMap();
        if(TestHelpers.canRunIntegrationTests()) {
            commands.put(CommandTest.NAME, new CommandTest(this));
        }
        return new CommandMod(this, commands);
    }

    @Mod.EventHandler
    @Override
    public final void preInit(FMLPreInitializationEvent event) {
        getRegistryManager().addRegistry(IVariableFacadeHandlerRegistry.class, VariableFacadeHandlerRegistry.getInstance());
        getRegistryManager().addRegistry(IValueTypeRegistry.class, ValueTypeRegistry.getInstance());
        getRegistryManager().addRegistry(IValueCastRegistry.class, ValueCastRegistry.getInstance());
        getRegistryManager().addRegistry(IValueTypeListProxyFactoryTypeRegistry.class, ValueTypeListProxyFactoryTypeRegistry.getInstance());
        getRegistryManager().addRegistry(IValueTypeLightLevelRegistry.class, ValueTypeLightLevelRegistry.getInstance());
        getRegistryManager().addRegistry(IPartTypeRegistry.class, PartTypeRegistry.getInstance());
        getRegistryManager().addRegistry(IAspectRegistry.class, AspectRegistry.getInstance());
        getRegistryManager().addRegistry(IOperatorRegistry.class, OperatorRegistry.getInstance());
        getRegistryManager().addRegistry(ILogicProgrammerElementTypeRegistry.class, LogicProgrammerElementTypeRegistry.getInstance());
        if(MinecraftHelpers.isClientSide()) {
            getRegistryManager().addRegistry(IPartOverlayRendererRegistry.class, PartOverlayRendererRegistry.getInstance());
            getRegistryManager().addRegistry(IValueTypeWorldRendererRegistry.class, ValueTypeWorldRendererRegistry.getInstance());
            getRegistryManager().addRegistry(IVariableModelProviderRegistry.class, VariableModelProviderRegistry.getInstance());
        }
        getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).registerHandler(ProxyVariableFacadeHandler.getInstance());

        addInitListeners(getRegistryManager().getRegistry(IPartTypeRegistry.class));

        ValueTypes.load();
        ValueCastMappings.load();
        ValueTypeLightLevels.load();
        ValueTypeListProxyFactories.load();
        Operators.load();
        Aspects.load();
        PartTypes.load();
        LogicProgrammerElementTypes.load();
        if(MinecraftHelpers.isClientSide()) {
            PartOverlayRenderers.load();
            ValueTypeWorldRenderers.load();
            VariableModelProviders.load();
        }

        super.preInit(event);

        FMLCommonHandler.instance().bus().register(TickHandler.getInstance());
    }

    @Mod.EventHandler
    @Override
    public final void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Mod.EventHandler
    @Override
    public final void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Mod.EventHandler
    @Override
    public void onServerStarting(FMLServerStartingEvent event) {
        super.onServerStarting(event);
    }

    @Mod.EventHandler
    @Override
    public void onServerStarted(FMLServerStartedEvent event) {
        super.onServerStarted(event);
    }

    @Mod.EventHandler
    @Override
    public void onServerStopping(FMLServerStoppingEvent event) {
        super.onServerStopping(event);
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return new ItemCreativeTab(this, new BlockItemConfigReference(BlockCableConfig.class));
    }

    @Override
    public void onGeneralConfigsRegister(ConfigHandler configHandler) {
        configHandler.add(new GeneralConfig());
    }

    @Override
    public void onMainConfigsRegister(ConfigHandler configHandler) {
        Configs.registerBlocks(configHandler);
    }

    @Override
    public ICommonProxy getProxy() {
        return proxy;
    }

    /**
     * Log a new info message for this mod.
     * @param message The message to show.
     */
    public static void clog(String message) {
        IntegratedDynamics._instance.log(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     * @param level The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void clog(Level level, String message) {
        IntegratedDynamics._instance.log(level, message);
    }
}