package net.itsfirecat.axissmp.network;

import net.itsfirecat.axissmp.qte.QTEType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QTEStartPacket(QTEType type, int durationTicks) implements CustomPayload {
    public static final Id<QTEStartPacket> ID = new Id<>(Identifier.of("axissmp", "qte_start"));

    public static final PacketCodec<RegistryByteBuf, QTEStartPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.indexed(index -> QTEType.values()[index], Enum::ordinal), QTEStartPacket::type,
            PacketCodecs.INTEGER, QTEStartPacket::durationTicks,
            QTEStartPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}