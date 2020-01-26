package org.cyclops.integrateddynamics.core.part;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.tuple.Triple;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.api.part.IPartContainer;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartRenderPosition;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerPartSettings;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * An abstract {@link IPartType} that can have settings.
 * @author rubensworks
 */
public abstract class PartTypeConfigurable<P extends IPartType<P, S>, S extends IPartState<P>> extends PartTypeBase<P, S> {

    public PartTypeConfigurable(String name, PartRenderPosition partRenderPosition) {
        super(name, partRenderPosition);
    }

    @Override
    public Optional<INamedContainerProvider> getContainerProviderSettings(PartPos pos) {
        return Optional.of(new INamedContainerProvider() {

            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent(getTranslationKey());
            }

            @Nullable
            @Override
            public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                Triple<IPartContainer, PartTypeBase, PartTarget> data = PartHelpers.getContainerPartConstructionData(pos);
                return new ContainerPartSettings(id, playerInventory, new Inventory(0),
                        data.getRight(), Optional.of(data.getLeft()), data.getMiddle());
            }
        });
    }

    @Override
    public void writeExtraGuiDataSettings(PacketBuffer packetBuffer, PartPos pos, ServerPlayerEntity player) {
        PacketCodec.write(packetBuffer, pos);
        packetBuffer.writeString(this.getUniqueName().toString());
    }

}
