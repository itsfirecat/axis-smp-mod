package net.itsfirecat.arcbound.network;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record FreezePulsePayload(Vec3d center, float entityHeight, int durationTicks) implements CustomPayload {
    public static final Id<FreezePulsePayload> ID = new Id<>(Identifier.of("arcbound", "freeze_pulse"));

    public static final PacketCodec<RegistryByteBuf, FreezePulsePayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeDouble(value.center.x);
                buf.writeDouble(value.center.y);
                buf.writeDouble(value.center.z);
                buf.writeFloat(value.entityHeight);
                buf.writeInt(value.durationTicks);
            },
            buf -> new FreezePulsePayload(
                    new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readFloat(),
                    buf.readInt()
            )
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}