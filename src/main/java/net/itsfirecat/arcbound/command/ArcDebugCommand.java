package net.itsfirecat.arcbound.command;

import com.mojang.brigadier.CommandDispatcher;
import net.itsfirecat.arcbound.item.ModItems;
import net.itsfirecat.arcbound.util.ArcboundCooldowns;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ArcDebugCommand {
    private static boolean debugModeActive = false;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("arcdebug")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("toggle")
                        .executes(context -> {
                            debugModeActive = !debugModeActive;

                            // Wire the feedback directly to display the active high-tier debug ticks
                            int debugTicks = ArcboundCooldowns.getPulseCooldown();
                            double debugSeconds = debugTicks / 20.0;

                            String statusMessage = debugModeActive
                                    ? "§aENABLED §7(all arc cooldowns set to " + debugSeconds + " second(s))"
                                    : "§cDISABLED §7(production mode)";

                            context.getSource().sendFeedback(() ->
                                    Text.literal("arcbound debug mode: " + statusMessage), true);
                            return 1;
                        })
                )
                .then(CommandManager.literal("resetcooldowns")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            if (source.getPlayer() != null) {
                                PlayerEntity player = source.getPlayer();

                                player.getItemCooldownManager().set(ModItems.DASH_ARC, 0);
                                player.getItemCooldownManager().set(ModItems.RESONANCE_ARC, 0);
                                player.getItemCooldownManager().set(ModItems.FREEZE_ARC, 0);
                                player.getItemCooldownManager().set(ModItems.PULSE_ARC, 0);
                                player.getItemCooldownManager().set(ModItems.INFINITY_ARC, 0);

                                source.sendFeedback(() -> Text.literal("§a arc cooldowns cleared."), false);
                            }
                            return 1;
                        })
                )
        );
    }

    public static boolean isDebugModeActive() {
        return debugModeActive;
    }
}