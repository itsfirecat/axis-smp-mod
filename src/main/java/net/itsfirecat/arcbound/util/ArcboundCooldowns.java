package net.itsfirecat.arcbound.util;

import net.itsfirecat.arcbound.command.ArcDebugCommand;

public class ArcboundCooldowns {

    public static int getDashCooldown() {
        // Production: 5 mins (6000t)
        return ArcDebugCommand.isDebugModeActive() ? 20 : 6000;
    }

    public static int getResonanceCooldown() {
        // Production: 7 mins (8400t)
        return ArcDebugCommand.isDebugModeActive() ? 20 : 8400;
    }

    public static int getFreezeCooldown() {
        // Production: 7 mins (8400t)
        return ArcDebugCommand.isDebugModeActive() ? 20 : 8400;
    }

    public static int getPulseCooldown() {
        // Production: 10 mins (12000t)
        return ArcDebugCommand.isDebugModeActive() ? 20 : 12000;
    }

    public static int getInfinityCooldown() {
        // Production: 10 mins (12000t)
        return ArcDebugCommand.isDebugModeActive() ? 20 : 12000;
    }
}