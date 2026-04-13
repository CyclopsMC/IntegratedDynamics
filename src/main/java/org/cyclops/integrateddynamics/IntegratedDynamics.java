package org.cyclops.integrateddynamics;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.gametest.GameTestLoaderHelpers;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.infobook.IInfoBookRegistry;
import org.cyclops.cyclopscore.infobook.InfoBookRegistry;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
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
import org.cyclops.integrateddynamics.command.CommandGenerateNetwork;
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
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
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
import org.cyclops.integrateddynamics.gametest.*;
import org.cyclops.integrateddynamics.gametest.integration.GameTester;
import org.cyclops.integrateddynamics.infobook.OnTheDynamicsOfIntegrationBook;
import org.cyclops.integrateddynamics.item.ItemOnTheDynamicsOfIntegrationConfig;
import org.cyclops.integrateddynamics.metadata.RegistryExportables;
import org.cyclops.integrateddynamics.part.PartTypeConnectorOmniDirectional;
import org.cyclops.integrateddynamics.part.aspect.Aspects;
import org.cyclops.integrateddynamics.proxy.ClientProxy;
import org.cyclops.integrateddynamics.proxy.CommonProxy;

import java.lang.reflect.Field;

/**
 * The main mod class of IntegratedDynamics.
 * @author rubensworks
 *
 */
@Mod(Reference.MOD_ID)
public class IntegratedDynamics extends ModBaseNeoForge<IntegratedDynamics> {

    /**
     * The unique instance of this mod.
     */
    public static IntegratedDynamics _instance;

    public static GlobalCounters.Access globalCounters = null;

    public IntegratedDynamics(IEventBus modEventBus) {
        super(Reference.MOD_ID, (instance) -> _instance = instance, modEventBus);

        // Register world storages
        registerWorldStorage(NetworkWorldStorage.Access.getInstance(this));
        registerWorldStorage(globalCounters = new GlobalCounters.Access(this));
        registerWorldStorage(LabelsWorldStorage.Access.getInstance(this));

        getRegistryManager().addRegistry(IVariableFacadeHandlerRegistry.class, VariableFacadeHandlerRegistry.getInstance());
        getRegistryManager().addRegistry(IValueTypeRegistry.class, ValueTypeRegistry.getInstance());
        getRegistryManager().addRegistry(IValueCastRegistry.class, ValueCastRegistry.getInstance());
        getRegistryManager().addRegistry(IValueTypeListProxyFactoryTypeRegistry.class, ValueTypeListProxyFactoryTypeRegistry.getInstance());
        getRegistryManager().addRegistry(IValueTypeLightLevelRegistry.class, ValueTypeLightLevelRegistry.getInstance());
        getRegistryManager().addRegistry(IPartTypeRegistry.class, PartTypeRegistry.getInstance());
        getRegistryManager().addRegistry(IAspectRegistry.class, AspectRegistry.getInstance());
        getRegistryManager().addRegistry(IOperatorRegistry.class, OperatorRegistry.getInstance());
        getRegistryManager().addRegistry(ILogicProgrammerElementTypeRegistry.class, LogicProgrammerElementTypeRegistry.getInstance());
        if(getModHelpers().getMinecraftHelpers().isClientSide()) {
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
        modEventBus.addListener(this::registerIntegrationGameTests);

        NeoForge.EVENT_BUS.addListener(this::onServerStartedLoadedGroups);
        NeoForge.EVENT_BUS.register(TickHandler.getInstance());
        NeoForge.EVENT_BUS.register(NoteBlockEventReceiver.getInstance());
    }

    public void onRegistriesCreate(NewRegistryEvent event) {
        IngredientComponentCapabilities.load();
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> constructBaseCommand(Commands.CommandSelection selection, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> root = super.constructBaseCommand(selection, context);

        root.then(CommandCrash.make());
        root.then(CommandGenerateNetwork.make());
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

        // Register info book
        putGenericReference(ModBaseNeoForge.REFKEY_INFOBOOK_REWARDS, ItemOnTheDynamicsOfIntegrationConfig.bookRewards);
        getRegistryManager().getRegistry(IInfoBookRegistry.class).registerInfoBook(
                OnTheDynamicsOfIntegrationBook.getInstance(), "/data/" + Reference.MOD_ID + "/info/on_the_dynamics_of_integration.xml");

        IntegratedDynamicsSetupEvent integratedDynamicsSetupEvent = new IntegratedDynamicsSetupEvent(this.getContainer());
        ModList.get().forEachModContainer((name, container) -> {
            if (container instanceof FMLModContainer fmlModContainer) {
                fmlModContainer.getEventBus().post(integratedDynamicsSetupEvent);
            }
        });

        if(IModHelpers.get().getMinecraftHelpers().isClientSide()) {
            PartOverlayRenderers.load();
            ValueTypeWorldRenderers.load();
            VariableModelProviders.load();
        }

        super.setup(event);
    }

    protected void onServerStartedLoadedGroups(ServerStartedEvent event) {
        PartTypeConnectorOmniDirectional.LOADED_GROUPS.onStartedEvent(event);
    }

    @Override
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
    protected void onConfigsRegister(ConfigHandlerCommon configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig());
        Configs.registerBlocks(configHandler);
    }

    @Override
    public Class<?>[] getGameTestClasses() {
        return new Class[]{
                GameTestsAdvancements.class,
                GameTestsAspectsReadAudio.class,
                GameTestsAspectsReadBlock.class,
                GameTestsAspectsReadEntity.class,
                GameTestsAspectsReadExtraDimensional.class,
                GameTestsAspectsReadFluid.class,
                GameTestsAspectsReadInventory.class,
                GameTestsAspectsReadMachine.class,
                GameTestsAspectsReadNetwork.class,
                GameTestsAspectsReadRedstone.class,
                GameTestsAspectsReadWorld.class,
                GameTestsAspectsWriteRedstone.class,
                GameTestsBattery.class,
                GameTestsCombinedAspects.class,
                GameTestsDelayer.class,
                GameTestsDryingBasin.class,
                GameTestsFacades.class,
                GameTestsFluids.class,
                GameTestsFuzzing.class,
                GameTestsGenerator.class,
                GameTestsMaterializer.class,
                GameTestsMechanicalDryingBasin.class,
                GameTestsMechanicalSqueezer.class,
                GameTestsNetwork.class,
                GameTestsOffsets.class,
                GameTestsParts.class,
                GameTestsPerformance.class,
                GameTestsProxy.class,
                GameTestsSqueezer.class,
                GameTestsWrench.class,
        };
    }

    protected void registerIntegrationGameTests(RegisterGameTestsEvent event) {
        if (!GameTestLoaderHelpers.areGameTestsEnabled(getModId())) {
            return;
        }
        try {
            Field field = RegisterGameTestsEvent.class.getDeclaredField("environmentsRegistry");
            field.setAccessible(true);
            Registry<TestEnvironmentDefinition<?>> testEnvironmentRegistry = (Registry<TestEnvironmentDefinition<?>>) field.get(event);
            GameTester.registerCommonTests(getModId(), event::registerTest, testEnvironmentRegistry);
            GameTestsFuzzing.registerCommonTests(getModId(), event::registerTest, testEnvironmentRegistry);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
