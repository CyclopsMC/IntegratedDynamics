package org.cyclops.integrateddynamics;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.infobook.IInfoBookRegistry;
import org.cyclops.cyclopscore.infobook.InfoBookRegistry;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.persist.world.GlobalCounters;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProviderRegistry;
import org.cyclops.integrateddynamics.api.client.render.part.IPartOverlayRendererRegistry;
import org.cyclops.integrateddynamics.api.client.render.valuetype.IValueTypeWorldRendererRegistry;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueCastRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeLightLevelRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueTypeRegistry;
import org.cyclops.integrateddynamics.api.ingredient.IIngredientComponentHandlerRegistry;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElementTypeRegistry;
import org.cyclops.integrateddynamics.api.network.INetworkCraftingHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.IPartTypeRegistry;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRegistry;
import org.cyclops.integrateddynamics.capability.ingredient.IngredientComponentCapabilities;
import org.cyclops.integrateddynamics.capability.network.NetworkCapabilityConstructors;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRendererRegistry;
import org.cyclops.integrateddynamics.client.render.part.PartOverlayRenderers;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRendererRegistry;
import org.cyclops.integrateddynamics.client.render.valuetype.ValueTypeWorldRenderers;
import org.cyclops.integrateddynamics.command.CommandCrash;
import org.cyclops.integrateddynamics.command.CommandNetworkDiagnostics;
import org.cyclops.integrateddynamics.command.CommandTest;
import org.cyclops.integrateddynamics.core.NoteBlockEventReceiver;
import org.cyclops.integrateddynamics.core.TickHandler;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviderRegistry;
import org.cyclops.integrateddynamics.core.client.model.VariableModelProviders;
import org.cyclops.integrateddynamics.core.evaluate.DelayVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.ProxyVariableFacadeHandler;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorRegistry;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueCastMappings;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueCastRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLightLevelRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLightLevels;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactories;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeListProxyFactoryTypeRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeRegistry;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.event.IntegratedDynamicsSetupEvent;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlerRegistry;
import org.cyclops.integrateddynamics.core.ingredient.IngredientComponentHandlers;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypeRegistry;
import org.cyclops.integrateddynamics.core.logicprogrammer.LogicProgrammerElementTypes;
import org.cyclops.integrateddynamics.core.network.NetworkCraftingHandlerRegistry;
import org.cyclops.integrateddynamics.core.part.PartTypeRegistry;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;
import org.cyclops.integrateddynamics.core.persist.world.NetworkWorldStorage;
import org.cyclops.integrateddynamics.infobook.OnTheDynamicsOfIntegrationBook;
import org.cyclops.integrateddynamics.item.ItemOnTheDynamicsOfIntegrationConfig;
import org.cyclops.integrateddynamics.metadata.RegistryExportables;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.proxy.ClientProxy;
import org.cyclops.integrateddynamics.proxy.CommonProxy;

/**
 * The main mod class of IntegratedDynamics.
 * @author rubensworks
 *
 */
@Mod(Reference.MOD_ID)
public class IntegratedDynamics extends ModBaseVersionable<IntegratedDynamics> {

    /**
     * The unique instance of this mod.
     */
    public static IntegratedDynamics _instance;

    public static GlobalCounters globalCounters = null;

    public IntegratedDynamics(IEventBus modEventBus) {
        super(Reference.MOD_ID, (instance) -> _instance = instance, modEventBus);

        // Register world storages
        registerWorldStorage(NetworkWorldStorage.getInstance(this));
        registerWorldStorage(globalCounters = new GlobalCounters(this));
        registerWorldStorage(LabelsWorldStorage.getInstance(this));

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
        getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class).registerHandler(DelayVariableFacadeHandler.getInstance());
        getRegistryManager().addRegistry(IInfoBookRegistry.class, new InfoBookRegistry());
        getRegistryManager().addRegistry(IIngredientComponentHandlerRegistry.class, IngredientComponentHandlerRegistry.getInstance());
        getRegistryManager().addRegistry(INetworkCraftingHandlerRegistry.class, NetworkCraftingHandlerRegistry.getInstance());

        // Preload parts, so we force their blocks and items to be registered
        PartTypes.load();

        modEventBus.addListener(EventPriority.LOWEST, IngredientComponentHandlers::onIngredientComponentsPopulated);
        modEventBus.addListener(this::onRegistriesCreate);
        modEventBus.register(new NetworkCapabilityConstructors());

        NeoForge.EVENT_BUS.addListener(this::onServerStartedLoadedGroups);
        NeoForge.EVENT_BUS.register(TickHandler.getInstance());
        NeoForge.EVENT_BUS.register(NoteBlockEventReceiver.getInstance());
    }

    public void onRegistriesCreate(NewRegistryEvent event) {
        IngredientComponentCapabilities.load();
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> constructBaseCommand() {
        LiteralArgumentBuilder<CommandSourceStack> root = super.constructBaseCommand();

        root.then(CommandCrash.make());
        root.then(CommandNetworkDiagnostics.make());
        root.then(CommandTest.make());

        return root;
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        ValueTypes.load();
        IngredientComponentHandlers.load();
        ValueCastMappings.load();
        ValueTypeLightLevels.load();
        ValueTypeListProxyFactories.load();
        Operators.load();
        Aspects.load();
        PartTypes.register();
        LogicProgrammerElementTypes.load();
        RegistryExportables.load();
        if(MinecraftHelpers.isClientSide()) {
            PartOverlayRenderers.load();
            ValueTypeWorldRenderers.load();
            VariableModelProviders.load();
        }

        super.setup(event);

        // Register info book
        putGenericReference(ModBase.REFKEY_INFOBOOK_REWARDS, ItemOnTheDynamicsOfIntegrationConfig.bookRewards);
        getRegistryManager().getRegistry(IInfoBookRegistry.class).registerInfoBook(
                OnTheDynamicsOfIntegrationBook.getInstance(), "/data/" + Reference.MOD_ID + "/info/on_the_dynamics_of_integration.xml");

        IntegratedDynamicsSetupEvent integratedDynamicsSetupEvent = new IntegratedDynamicsSetupEvent(this.getContainer());
        ModList.get().forEachModContainer((name, container) -> {
            if (container instanceof FMLModContainer fmlModContainer) {
                fmlModContainer.getEventBus().post(integratedDynamicsSetupEvent);
            }
        });
    }

    protected void onServerStartedLoadedGroups(ServerStartedEvent event) {
        PartTypeConnectorOmniDirectional.LOADED_GROUPS.onStartedEvent(event);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected IClientProxy constructClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected ICommonProxy constructCommonProxy() {
        return new CommonProxy();
    }

    @Override
    protected CreativeModeTab.Builder constructDefaultCreativeModeTab(CreativeModeTab.Builder builder) {
        return super.constructDefaultCreativeModeTab(builder)
                .icon(() -> new ItemStack(RegistryEntries.ITEM_CABLE));
    }

    @Override
    protected void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig());
        Configs.registerBlocks(configHandler);
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
