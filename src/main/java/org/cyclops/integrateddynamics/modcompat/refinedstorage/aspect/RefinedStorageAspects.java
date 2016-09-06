package org.cyclops.integrateddynamics.modcompat.refinedstorage.aspect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.*;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.part.aspect.build.AspectBuilder;
import org.cyclops.integrateddynamics.core.part.aspect.build.IAspectValuePropagator;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.part.aspect.read.AspectReadBuilders;
import org.cyclops.integrateddynamics.part.aspect.write.AspectWriteBuilders;
import refinedstorage.api.autocrafting.ICraftingPattern;
import refinedstorage.api.autocrafting.task.ICraftingTask;
import refinedstorage.api.network.INetworkMaster;
import refinedstorage.api.network.INetworkNode;
import refinedstorage.api.network.NetworkUtils;
import refinedstorage.api.storage.CompareUtils;

import java.util.Collections;
import java.util.List;

/**
 * Builders for Refined Storage aspects
 * @author rubensworks
 */
public class RefinedStorageAspects {

    public static final class Read {

        public static final IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetworkNode> PROP_GET_NODE = new IAspectValuePropagator<Pair<PartTarget, IAspectProperties>, INetworkNode>() {
            @Override
            public INetworkNode getOutput(Pair<PartTarget, IAspectProperties> input) {
                DimPos pos = input.getLeft().getTarget().getPos();
                return CableHelpers.getInterface(pos, INetworkNode.class);
            }
        };
        public static final IAspectValuePropagator<INetworkNode, INetworkMaster> PROP_GET_MASTER = new IAspectValuePropagator<INetworkNode, INetworkMaster>() {
            @Override
            public INetworkMaster getOutput(INetworkNode input) {
                return input != null ? input.getNetwork() : null;
            }
        };

        public static final AspectBuilder<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean, INetworkMaster>
                BUILDER_BOOLEAN = AspectReadBuilders.BUILDER_BOOLEAN.handle(PROP_GET_NODE, "refinedstorage").handle(PROP_GET_MASTER);
        public static final AspectBuilder<ValueTypeList.ValueList, ValueTypeList, INetworkMaster>
                BUILDER_LIST = AspectReadBuilders.BUILDER_LIST.handle(PROP_GET_NODE, "refinedstorage").handle(PROP_GET_MASTER);

        public static final class Network {

            public static final IAspectRead<ValueTypeBoolean.ValueBoolean, ValueTypeBoolean> BOOLEAN_APPLICABLE =
                    BUILDER_BOOLEAN.appendKind("network").handle(new IAspectValuePropagator<INetworkMaster, Boolean>() {
                        @Override
                        public Boolean getOutput(INetworkMaster networkMaster) {
                            return networkMaster != null;
                        }
                    }).handle(AspectReadBuilders.PROP_GET_BOOLEAN, "applicable").buildRead();

        }

        public static final class Inventory {

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_ITEMSTACKS =
                    BUILDER_LIST.appendKind("inventory").handle(new IAspectValuePropagator<INetworkMaster, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(INetworkMaster networkMaster) {
                            if (networkMaster != null) {
                                return ValueTypeList.ValueList.ofFactory(
                                        new ValueTypeListProxyPositionedNetworkMasterItemInventory(
                                                DimPos.of(networkMaster.getNetworkWorld(), networkMaster.getPosition())));
                            }
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.<ValueObjectTypeItemStack.ValueItemStack>emptyList());
                        }
                    }, "itemstacks").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_CRAFTABLEITEMS =
                    BUILDER_LIST.appendKind("inventory").handle(new IAspectValuePropagator<INetworkMaster, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(INetworkMaster networkMaster) {
                            if (networkMaster != null) {
                                List<ValueObjectTypeItemStack.ValueItemStack> itemStacks = Lists.newArrayList();
                                for (ICraftingPattern craftingPattern : networkMaster.getPatterns()) {
                                    for (ItemStack itemStack : craftingPattern.getOutputs()) {
                                        itemStacks.add(ValueObjectTypeItemStack.ValueItemStack.of(itemStack));
                                    }
                                }

                                return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, itemStacks);
                            }
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.<ValueObjectTypeItemStack.ValueItemStack>emptyList());
                        }
                    }, "craftableitems").buildRead();

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_CRAFTINGITEMS =
                    BUILDER_LIST.appendKind("inventory").handle(new IAspectValuePropagator<INetworkMaster, ValueTypeList.ValueList>() {

                        protected void addPatternItemStacks(List<ValueObjectTypeItemStack.ValueItemStack> itemStacks, ICraftingTask craftingTask) {
                            ICraftingPattern craftingPattern = craftingTask.getPattern();
                            for (ItemStack itemStack : craftingPattern.getOutputs()) {
                                itemStacks.add(ValueObjectTypeItemStack.ValueItemStack.of(itemStack));
                            }
                            if (craftingTask.getChild() != null) {
                                addPatternItemStacks(itemStacks, craftingTask.getChild());
                            }
                        }

                        @Override
                        public ValueTypeList.ValueList getOutput(INetworkMaster networkMaster) {
                            if (networkMaster != null) {
                                List<ValueObjectTypeItemStack.ValueItemStack> itemStacks = Lists.newArrayList();
                                for (ICraftingTask craftingTask : networkMaster.getCraftingTasks()) {
                                    addPatternItemStacks(itemStacks, craftingTask);
                                }
                                return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, itemStacks);
                            }
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_ITEMSTACK, Collections.<ValueObjectTypeItemStack.ValueItemStack>emptyList());
                        }
                    }, "craftingitems").buildRead();

        }

        public static final class Fluid {

            public static final IAspectRead<ValueTypeList.ValueList, ValueTypeList> LIST_FLUIDSTACKS =
                    BUILDER_LIST.appendKind("fluid").handle(new IAspectValuePropagator<INetworkMaster, ValueTypeList.ValueList>() {
                        @Override
                        public ValueTypeList.ValueList getOutput(INetworkMaster networkMaster) {
                            if (networkMaster != null) {
                                return ValueTypeList.ValueList.ofFactory(
                                        new ValueTypeListProxyPositionedNetworkMasterFluidInventory(
                                                DimPos.of(networkMaster.getNetworkWorld(), networkMaster.getPosition())));
                            }
                            return ValueTypeList.ValueList.ofList(ValueTypes.OBJECT_FLUIDSTACK, Collections.<ValueObjectTypeFluidStack.ValueFluidStack>emptyList());
                        }
                    }, "fluidstacks").buildRead();

        }

    }

    public static final class Write {

        public static final IAspectPropertyTypeInstance<ValueTypeBoolean, ValueTypeBoolean.ValueBoolean> PROPERTY_SKIPCRAFTING =
                new AspectPropertyTypeInstance<>(ValueTypes.BOOLEAN, "aspect.aspecttypes.integrateddynamics.boolean.refinedstorage.skipcrafting.name");
        public static final IAspectPropertyTypeInstance<ValueTypeBoolean, ValueTypeBoolean.ValueBoolean> PROPERTY_SKIPSTORAGE =
                new AspectPropertyTypeInstance<>(ValueTypes.BOOLEAN, "aspect.aspecttypes.integrateddynamics.boolean.refinedstorage.skipstorage.name");
        public static final IAspectProperties CRAFTING_PROPERTIES = new AspectProperties(ImmutableList.<IAspectPropertyTypeInstance>of(
                PROPERTY_SKIPCRAFTING,
                PROPERTY_SKIPSTORAGE
        ));
        static {
            CRAFTING_PROPERTIES.setValue(PROPERTY_SKIPCRAFTING, ValueTypeBoolean.ValueBoolean.of(true));
            CRAFTING_PROPERTIES.setValue(PROPERTY_SKIPSTORAGE, ValueTypeBoolean.ValueBoolean.of(false));
        }

        public static final IAspectWrite<ValueObjectTypeItemStack.ValueItemStack, ValueObjectTypeItemStack>
                ITEMSTACK_CRAFT = AspectWriteBuilders.BUILDER_ITEMSTACK.appendKind("refinedstorage")
                .withProperties(CRAFTING_PROPERTIES).handle(
                        new IAspectValuePropagator<Triple<PartTarget, IAspectProperties, ValueObjectTypeItemStack.ValueItemStack>, Void>() {
                            @Override
                            public Void getOutput(Triple<PartTarget, IAspectProperties, ValueObjectTypeItemStack.ValueItemStack> input)
                                    throws EvaluationException {
                                if (input.getRight().getRawValue().isPresent()) {
                                    DimPos pos = input.getLeft().getTarget().getPos();
                                    INetworkNode networkNode = CableHelpers.getInterface(pos, INetworkNode.class);
                                    if (networkNode != null) {
                                        INetworkMaster networkMaster = networkNode.getNetwork();
                                        if (networkMaster != null) {
                                            int compareFlags = CompareUtils.COMPARE_DAMAGE | CompareUtils.COMPARE_NBT;
                                            ItemStack itemStack = input.getRight().getRawValue().get();
                                            ICraftingPattern craftingPattern = NetworkUtils.getPattern(networkMaster, itemStack);
                                            if (craftingPattern != null) {
                                                ICraftingTask craftingTask = NetworkUtils.createCraftingTask(networkMaster, craftingPattern);

                                                if (input.getMiddle().getValue(PROPERTY_SKIPCRAFTING).getRawValue()) {
                                                    for (ICraftingTask task : networkMaster.getCraftingTasks()) {
                                                        for (ItemStack output : task.getPattern().getOutputs()) {
                                                            if (CompareUtils.compareStack(output, itemStack, compareFlags)) {
                                                                // If there's already one crafting, stop.
                                                                return null;
                                                            }
                                                        }
                                                    }
                                                }

                                                if (input.getMiddle().getValue(PROPERTY_SKIPSTORAGE).getRawValue()) {
                                                    ItemStack present = networkMaster.getItemStorage().get(itemStack, compareFlags);
                                                    if (present != null && present.stackSize >= itemStack.stackSize) {
                                                        // If there's already one in the inventory, stop.
                                                        return null;
                                                    }
                                                }

                                                // Once we get here, we are certain that we want to shedule the task.
                                                networkMaster.addCraftingTask(craftingTask);
                                            }
                                        }
                                    }
                                }
                                return null;
                            }
                        }, "craft").buildWrite();

    }

}
