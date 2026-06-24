package net.itsfirecat.arcbound.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.List;
import java.util.UUID;

public record QTEESPPacket(List<UUID> targets) implements CustomPayload {
    public static final Id<QTEESPPacket> ID = new Id<>(Identifier.of("arcbound", "qte_esp"));

    public static final PacketCodec<RegistryByteBuf, QTEESPPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(java.util.ArrayList::new, Uuids.PACKET_CODEC), QTEESPPacket::targets,
            QTEESPPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}