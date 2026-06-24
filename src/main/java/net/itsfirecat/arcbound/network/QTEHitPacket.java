package net.itsfirecat.arcbound.network;

import net.itsfirecat.arcbound.qte.QTEType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QTEHitPacket(QTEType type) implements CustomPayload {
    public static final Id<QTEHitPacket> ID = new Id<>(Identifier.of("arcbound", "qte_hit"));

    public static final PacketCodec<RegistryByteBuf, QTEHitPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.indexed(index -> QTEType.values()[index], Enum::ordinal), QTEHitPacket::type,
            QTEHitPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}