package org.cyclops.integrateddynamics.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.core.persist.world.LabelsWorldStorage;

import java.util.function.Consumer;

/**
 * Base implementation of {@link IVariableFacade}
 *
 * @author rubensworks
 */
public abstract class VariableFacadeBase implements IVariableFacade {

    private final int id;
    private IVariableFacadeClient client;

    public VariableFacadeBase(boolean generateId) {
        this(generateId ? generateId() : -1);
    }

    public VariableFacadeBase(int id) {
        this.id = id;
        if (IModHelpers.get().getMinecraftHelpers().isClientSide()) {
            this.client = constructClient();
        }
    }

    protected abstract IVariableFacadeClient constructClient();

    public IVariableFacadeClient getClient() {
        return client;
    }

    /**
     * @return A unique new variable id.
     */
    public static int generateId() {
        return IntegratedDynamics.globalCounters.getNext("variable");
    }

    @Override
    public String getLabel() {
        return LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(getId());
    }

    protected String getReferenceDisplay(int variableId) {
        String label = LabelsWorldStorage.getInstance(IntegratedDynamics._instance).getLabel(variableId);
        if (label == null) {
            return String.valueOf(variableId);
        } else {
            return String.format("%s:%s", label, variableId);
        }
    }

    @Override
    public void appendHoverText(Consumer<Component> tooltipAdder, Item.TooltipContext context) {
        tooltipAdder.accept(Component.translatable("item.integrateddynamics.variable.id", getId() == -1 ? "..." : getId()));
    }

    public int getId() {
        return this.id;
    }

    public void setClient(IVariableFacadeClient client) {
        this.client = client;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof VariableFacadeBase)) return false;
        final VariableFacadeBase other = (VariableFacadeBase) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getId() != other.getId()) return false;
        final Object this$client = this.getClient();
        final Object other$client = other.getClient();
        if (this$client == null ? other$client != null : !this$client.equals(other$client)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof VariableFacadeBase;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getId();
        final Object $client = this.getClient();
        result = result * PRIME + ($client == null ? 43 : $client.hashCode());
        return result;
    }

    public String toString() {
        return "VariableFacadeBase(id=" + this.getId() + ", client=" + this.getClient() + ")";
    }
}
