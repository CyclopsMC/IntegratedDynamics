package org.cyclops.integrateddynamics;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.cyclops.commoncapabilities.api.ingredient.capability.IngredientComponentCapability;
import org.cyclops.integrateddynamics.api.block.IDynamicLight;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.block.IFacadeable;
import org.cyclops.integrateddynamics.api.block.IVariableContainer;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.api.evaluate.IValueInterface;
import org.cyclops.integrateddynamics.api.ingredient.capability.IIngredientComponentValueHandler;
import org.cyclops.integrateddynamics.api.ingredient.capability.IPositionedAddonsNetworkIngredientsHandler;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;
import org.cyclops.integrateddynamics.api.network.IEnergyNetwork;
import org.cyclops.integrateddynamics.api.network.INetworkCarrier;
import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.NetworkCapability;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.PartCapability;
import org.cyclops.integrateddynamics.api.path.IPathElement;

/**
 * Used capabilities for this mod.
 * @author rubensworks
 */
public class Capabilities {
    public static final class Cable {
        public static final BlockCapability<ICable, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "cable"), ICable.class);
    }

    public static final class CableFakeable {
        public static final BlockCapability<ICableFakeable, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "cable_fakeable"), ICableFakeable.class);
    }

    public static final class DynamicLight {
        public static final BlockCapability<IDynamicLight, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "dynamic_light"), IDynamicLight.class);
    }

    public static final class DynamicRedstone {
        public static final BlockCapability<IDynamicRedstone, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "dynamic_redstone"), IDynamicRedstone.class);
    }

    public static final class Facadeable {
        public static final BlockCapability<IFacadeable, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "facadeable"), IFacadeable.class);
    }

    public static final class NetworkCarrier {
        public static final BlockCapability<INetworkCarrier, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "network_carrier"), INetworkCarrier.class);
    }

    public static final class NetworkElementProvider {
        public static final BlockCapability<INetworkElementProvider, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "network_element_provider"), INetworkElementProvider.class);
    }

    public static final class PartContainer {
        public static final BlockCapability<IPartContainer, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "part_container"), IPartContainer.class);
    }

    public static final class PathElement {
        public static final BlockCapability<IPathElement, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "path_element"), IPathElement.class);
        public static final PartCapability<IPathElement> PART = PartCapability.create(new ResourceLocation(Reference.MOD_ID, "path_element"), IPathElement.class);
    }

    public static final class VariableFacade {
        public static final ItemCapability<IVariableFacadeHolder, Void> ITEM = ItemCapability.createVoid(new ResourceLocation(Reference.MOD_ID, "variable_facade_holder"), IVariableFacadeHolder.class);
    }

    public static final class IngredientComponentValueHandler {
        public static final IngredientComponentCapability<IIngredientComponentValueHandler, Void> INGREDIENT = IngredientComponentCapability.createVoid(new ResourceLocation(Reference.MOD_ID, "positioned_addons_network_ingredients_handler"), IIngredientComponentValueHandler.class);
    }

    public static final class PositionedAddonsNetworkIngredientsHandler {
        public static final IngredientComponentCapability<IPositionedAddonsNetworkIngredientsHandler, Void> INGREDIENT = IngredientComponentCapability.createVoid(new ResourceLocation(Reference.MOD_ID, "ingredient_component_value_handler"), IPositionedAddonsNetworkIngredientsHandler.class);
    }

    public static final class ValueInterface {
        public static final BlockCapability<IValueInterface, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "value_interface"), IValueInterface.class);
        public static final PartCapability<IValueInterface> PART = PartCapability.create(new ResourceLocation(Reference.MOD_ID, "value_interface"), IValueInterface.class);
    }

    public static final class VariableContainer {
        public static final BlockCapability<IVariableContainer, Direction> BLOCK = BlockCapability.createSided(new ResourceLocation(Reference.MOD_ID, "variable_container"), IVariableContainer.class);
        public static final PartCapability<IVariableContainer> PART = PartCapability.create(new ResourceLocation(Reference.MOD_ID, "variable_container"), IVariableContainer.class);
    }

    public static final class EnergyNetwork {
        public static final NetworkCapability<IEnergyNetwork> NETWORK = NetworkCapability.create(new ResourceLocation(Reference.MOD_ID, "energy_network"), IEnergyNetwork.class);
    }

    public static final class PartNetwork {
        public static final NetworkCapability<IPartNetwork> NETWORK = NetworkCapability.create(new ResourceLocation(Reference.MOD_ID, "part_network"), IPartNetwork.class);
    }

    public static final class EnergyStorage {
        public static final NetworkCapability<IEnergyStorage> NETWORK = NetworkCapability.create(new ResourceLocation(Reference.MOD_ID, "energy_storage"), IEnergyStorage.class);
    }
}
