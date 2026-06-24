package net.itsfirecat.arcbound.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record QTEClearPacket() implements CustomPayload {
    public static final Id<QTEClearPacket> ID = new Id<>(Identifier.of("arcbound", "qte_clear"));
    public static final PacketCodec<RegistryByteBuf, QTEClearPacket> CODEC = PacketCodec.unit(new QTEClearPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}