package net.itsfirecat.arcbound.network;

import net.itsfirecat.arcbound.arcbound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record ResonancePulsePayload(Vec3d center, boolean isQteSuccess) implements CustomPayload {
    public static final Id<ResonancePulsePayload> ID = new Id<>(Identifier.of(arcbound.MOD_ID, "resonance_pulse"));

    public static final PacketCodec<RegistryByteBuf, ResonancePulsePayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeDouble(value.center.x);
                buf.writeDouble(value.center.y);
                buf.writeDouble(value.center.z);
                buf.writeBoolean(value.isQteSuccess);
            },
            buf -> new ResonancePulsePayload(
                    new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readBoolean()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}