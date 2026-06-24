package net.itsfirecat.arcbound.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ArcFlashPacket(long durationMs, int color) implements CustomPayload {
    public static final Id<ArcFlashPacket> ID = new Id<>(Identifier.of("arcbound", "arc_flash"));

    public static final PacketCodec<RegistryByteBuf, ArcFlashPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_LONG, ArcFlashPacket::durationMs,
            PacketCodecs.INTEGER, ArcFlashPacket::color,
            ArcFlashPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}