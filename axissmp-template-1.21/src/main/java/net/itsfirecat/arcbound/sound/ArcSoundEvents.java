package net.itsfirecat.arcbound.sound;

import net.itsfirecat.arcbound.arcbound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ArcSoundEvents {

    public static final SoundEvent BLUE_SPAWN = register("hollow_purple.blue_spawn");
    public static final SoundEvent RED_SPAWN = register("hollow_purple.red_spawn");
    public static final SoundEvent MERGE = register("hollow_purple.merge");
    public static final SoundEvent LAUNCH = register("hollow_purple.launch");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(arcbound.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void register() {}
}