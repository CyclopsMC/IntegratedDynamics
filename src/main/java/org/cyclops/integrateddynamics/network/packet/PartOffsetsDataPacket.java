package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.cyclopscore.network.PacketCodecs;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.render.level.PartOffsetsOverlayRenderer;
import org.cyclops.integrateddynamics.core.network.PartOffsetsClientNotifier;

import java.util.List;

/**
 * Packet for sending part offsets to a player.
 * @author rubensworks
 */
public class PartOffsetsDataPacket extends PacketCodec {

    public static final Type<PartOffsetsDataPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "part_offsets_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PartOffsetsDataPacket> CODEC = getCodec(PartOffsetsDataPacket::new);

    static {
        PacketCodecs.addCodedAction(PartOffsetsClientNotifier.Entry.class, new ICodecAction() {
            @Override
            public void encode(Object object, RegistryFriendlyByteBuf output) {
                PartOffsetsClientNotifier.Entry entry = (PartOffsetsClientNotifier.Entry) object;
                PacketCodecs.getAction(BlockPos.class).encode(entry.source(), output);
                PacketCodecs.getAction(Direction.class).encode(entry.sourceSide(), output);
                PacketCodecs.getAction(Vec3i.class).encode(entry.targetOffset(), output);
                PacketCodecs.getAction(Direction.class).encode(entry.targetSide(), output);
            }

            @Override
            public Object decode(RegistryFriendlyByteBuf input) {
                return new PartOffsetsClientNotifier.Entry(
                        (BlockPos) PacketCodecs.getAction(BlockPos.class).decode(input),
                        (Direction) PacketCodecs.getAction(Direction.class).decode(input),
                        (Vec3i) PacketCodecs.getAction(Vec3i.class).decode(input),
                        (Direction) PacketCodecs.getAction(Direction.class).decode(input)
                );
            }
        });
    }

    @CodecField
    private List<PartOffsetsClientNotifier.Entry> offsets;

    public PartOffsetsDataPacket() {
        super(ID);
    }

    public PartOffsetsDataPacket(List<PartOffsetsClientNotifier.Entry> offsets) {
        super(ID);
        this.offsets = offsets;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        PartOffsetsOverlayRenderer.getInstance().setData(this.offsets);
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
