package net.itsfirecat.axissmp.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QTEHitPacket() implements CustomPayload {
    public static final Id<QTEHitPacket> ID = new Id<>(Identifier.of("axissmp", "qte_hit"));
    public static final PacketCodec<RegistryByteBuf, QTEHitPacket> CODEC = PacketCodec.unit(new QTEHitPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}