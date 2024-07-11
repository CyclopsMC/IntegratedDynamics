package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.api.logicprogrammer.ILogicProgrammerElement;
import org.cyclops.integrateddynamics.core.ingredient.ItemMatchProperties;
import org.cyclops.integrateddynamics.core.logicprogrammer.ValueTypeRecipeLPElement;
import org.cyclops.integrateddynamics.inventory.container.ContainerLogicProgrammerBase;

/**
 * Packet for sending a to the server if recipe slot properties have changed.
 * @author rubensworks
 *
 */
public class LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket extends PacketCodec {

    public static final Type<LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "logic_programmer_value_type_recipe_slot_properties_value_changed"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket> CODEC = getCodec(LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket::new);

    @CodecField
    private int slot;
    @CodecField
    private boolean nbt;
    @CodecField
    private String tag;
    @CodecField
    private int tagQuantity;
    @CodecField
    private boolean reusable;

    public LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket() {
        super(ID);
    }

    public LogicProgrammerValueTypeRecipeSlotPropertiesChangedPacket(int slot, boolean nbt, String tag, int tagQuantity, boolean reusable) {
        super(ID);
        this.slot = slot;
        this.nbt = nbt;
        this.tag = tag;
        this.tagQuantity = tagQuantity;
        this.reusable = reusable;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {

    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        if(player.containerMenu instanceof ContainerLogicProgrammerBase) {
            ILogicProgrammerElement element = ((ContainerLogicProgrammerBase) player.containerMenu).getActiveElement();
            if(element instanceof ValueTypeRecipeLPElement) {
                ItemMatchProperties props = ((ValueTypeRecipeLPElement) element).getInputStacks().get(slot);
                props.setNbt(nbt);
                props.setItemTag(tag.isEmpty() ? null : tag);
                props.setTagQuantity(this.tagQuantity);
                props.setReusable(reusable);
                ((ContainerLogicProgrammerBase) player.containerMenu).onDirty();
            }
        }
    }

}
