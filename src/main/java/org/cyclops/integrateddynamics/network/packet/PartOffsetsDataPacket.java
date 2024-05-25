package org.cyclops.integrateddynamics.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.client.render.level.PartOffsetsOverlayRenderer;
import org.cyclops.integrateddynamics.core.network.PartOffsetsClientNotifier;

import java.util.List;

/**
 * Packet for sending part offsets to a player.
 * @author rubensworks
 */
public class PartOffsetsDataPacket extends PacketCodec {

    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "part_offsets_data");

    static {
        PacketCodec.addCodedAction(PartOffsetsClientNotifier.Entry.class, new ICodecAction() {
            @Override
            public void encode(Object object, FriendlyByteBuf output) {
                PartOffsetsClientNotifier.Entry entry = (PartOffsetsClientNotifier.Entry) object;
                PacketCodec.getAction(BlockPos.class).encode(entry.source(), output);
                PacketCodec.getAction(Direction.class).encode(entry.sourceSide(), output);
                PacketCodec.getAction(Vec3i.class).encode(entry.targetOffset(), output);
                PacketCodec.getAction(Direction.class).encode(entry.targetSide(), output);
            }

            @Override
            public Object decode(FriendlyByteBuf input) {
                return new PartOffsetsClientNotifier.Entry(
                        (BlockPos) PacketCodec.getAction(BlockPos.class).decode(input),
                        (Direction) PacketCodec.getAction(Direction.class).decode(input),
                        (Vec3i) PacketCodec.getAction(Vec3i.class).decode(input),
                        (Direction) PacketCodec.getAction(Direction.class).decode(input)
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
    @OnlyIn(Dist.CLIENT)
    public void actionClient(Level world, Player player) {
        PartOffsetsOverlayRenderer.getInstance().setData(this.offsets);
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {

    }

}
