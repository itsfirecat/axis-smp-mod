package net.itsfirecat.arcbound.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import java.util.UUID;

public record ArcVisualPayload(UUID casterUuid, int stateId) implements CustomPayload {
    public static final Id<ArcVisualPayload> ID = new Id<>(Identifier.of("arcbound", "visual_sync"));

    public static final PacketCodec<RegistryByteBuf, ArcVisualPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, ArcVisualPayload::casterUuid,
            PacketCodecs.INTEGER, ArcVisualPayload::stateId,
            ArcVisualPayload::new
    );

    @Override public Id<? extends CustomPayload> getId() { return ID; }
}