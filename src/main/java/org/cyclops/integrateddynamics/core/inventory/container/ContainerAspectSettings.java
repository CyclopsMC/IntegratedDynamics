package org.cyclops.integrateddynamics.core.inventory.container;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.cyclopscore.inventory.container.InventoryContainer;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectPropertyTypeInstance;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.part.aspect.AspectRegistry;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

/**
 * Container for aspect settings.
 * @author rubensworks
 */
public class ContainerAspectSettings extends InventoryContainer {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String BUTTON_EXIT = "button_exit";
    public static final int BUTTON_SETTINGS = 1;
    private static final int PAGE_SIZE = 3;

    private final Optional<PartTarget> target;
    private final Optional<IPartContainer> partContainer;
    private final Optional<IPartType> partType;
    private final Level world;
    private final IAspect<?, ?> aspect;

    private final BiMap<Integer, IAspectPropertyTypeInstance> propertyIds = HashBiMap.create();

    public ContainerAspectSettings(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, new SimpleContainer(0),
                Optional.empty(), Optional.empty(), Optional.empty(), readAspect(packetBuffer));
    }

    protected static IAspect<?, ?> readAspect(FriendlyByteBuf packetBuffer) {
        String name = packetBuffer.readUtf();
        return Objects.requireNonNull(AspectRegistry.getInstance().getAspect(Identifier.parse(name)),
                String.format("Could not find an aspect by name %s", name));
    }

    public ContainerAspectSettings(int id, Inventory playerInventory, Container inventory,
                                   Optional<PartTarget> target, Optional<IPartContainer> partContainer,
                                   Optional<IPartType> partType, IAspect<?, ?> aspect) {
        super(RegistryEntries.CONTAINER_ASPECT_SETTINGS.get(), id, playerInventory, inventory);
        this.target = target;
        this.partType = partType;
        this.partContainer = partContainer;
        this.world = player.level();
        this.aspect = aspect;

        addPlayerInventory(player.getInventory(), 8, 131);

        for(IAspectPropertyTypeInstance property : aspect.getPropertyTypes()) {
            propertyIds.put(getNextValueId(), property);
        }

        putButtonAction(ContainerAspectSettings.BUTTON_EXIT, (s, containerExtended) -> {
            if (!world.isClientSide()) {
                PartHelpers.openContainerPart((ServerPlayer) playerInventory.player, getTarget().get().getCenter(), getPartType().get());
            }
        });
    }

    public BiMap<Integer, IAspectPropertyTypeInstance> getPropertyIds() {
        return propertyIds;
    }

    public Optional<IPartType> getPartType() {
        return partType;
    }

    public IAspect getAspect() {
        return aspect;
    }

    public Optional<PartTarget> getTarget() {
        return target;
    }

    @Override
    protected void initializeValues() {
        super.initializeValues();
        IAspectProperties properties = aspect.getProperties(getPartType().get(), getTarget().get(), getPartState().get());
        for(IAspectPropertyTypeInstance property : aspect.getPropertyTypes()) {
            setValue(ValueDeseralizationContext.ofAllEnabled(), property, properties.getValue(property));
        }
    }

    public void setValue(ValueDeseralizationContext valueDeseralizationContext, IAspectPropertyTypeInstance property, IValue value) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), LOGGER)) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(scopedCollector, valueDeseralizationContext.holderLookupProvider());
            ValueHelpers.serializeRaw(valueOutput, value);
            ValueNotifierHelpers.setValue(this, propertyIds.inverse().get(property), valueOutput.buildResult());
        }
    }

    public Optional<IPartState> getPartState() {
        return partContainer.map(p -> p.getPartState(getTarget().get().getCenter().getSide()));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    public <T extends IValueType<V>, V extends IValue> V getPropertyValue(ValueDeseralizationContext valueDeseralizationContext, IAspectPropertyTypeInstance<T, V> property) {
        if(propertyIds.containsValue(property)) {
            CompoundTag value = (CompoundTag) ValueNotifierHelpers.getValueNbt(this, propertyIds.inverse().get(property));
            if(value != null) {
                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), LOGGER)) {
                    ValueInput input = TagValueInput.create(
                            scopedCollector,
                            valueDeseralizationContext.holderLookupProvider(),
                            value
                    );
                    return ValueHelpers.deserializeRaw(input, property.getType());
                }
            }
        }
        return null;
    }

    @Override
    public void onUpdate(int valueId, CompoundTag value) {
        super.onUpdate(valueId, value);
        if(!world.isClientSide()) {
            IAspectPropertyTypeInstance property = propertyIds.get(valueId);
            if (property != null) {
                IPartType partType = getPartType().get();
                PartTarget target = getTarget().get();
                IPartState partState = getPartState().get();

                IAspectProperties aspectProperties = aspect.getProperties(partType, target, partState);
                aspectProperties = aspectProperties.clone();
                IValue trueValue;
                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(player.problemPath(), LOGGER)) {
                    ValueInput input = TagValueInput.create(
                            scopedCollector,
                            player.level().registryAccess(),
                            (CompoundTag) value.get(ValueNotifierHelpers.KEY)
                    );
                    trueValue = ValueHelpers.deserializeRaw(input, property.getType());
                }
                aspectProperties.setValue(property, trueValue);
                aspect.setProperties(partType, target, partState, aspectProperties);

                // Changing the properties might cause some erroring variables to become valid again, so trigger an update.
                NetworkHelpers.getNetwork(target.getCenter())
                        .ifPresent(network -> network.getEventBus().post(new VariableContentsUpdatedEvent(network)));
            }
        }
    }
}
