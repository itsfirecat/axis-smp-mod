package net.itsfirecat.arcbound;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.itsfirecat.arcbound.item.ModItems;
import net.itsfirecat.arcbound.item.ModItemGroups;
import net.itsfirecat.arcbound.network.QTEClearPacket;
import net.itsfirecat.arcbound.network.QTEESPPacket;
import net.itsfirecat.arcbound.network.QTEHitPacket;
import net.itsfirecat.arcbound.network.QTEStartPacket;
import net.itsfirecat.arcbound.qte.ActiveQTE;
import net.itsfirecat.arcbound.qte.QTEManager;
import net.itsfirecat.block.ModBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class arcbound implements ModInitializer {
	public static final String MOD_ID = "arcbound";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Register standard mod elements
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		// 1. Register modern Fabric 1.21.1 network payload structures
		// Server-to-Client payloads
		PayloadTypeRegistry.playS2C().register(QTEStartPacket.ID, QTEStartPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(QTEClearPacket.ID, QTEClearPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(QTEESPPacket.ID, QTEESPPacket.CODEC);

		// Client-to-Server payloads
		PayloadTypeRegistry.playC2S().register(QTEHitPacket.ID, QTEHitPacket.CODEC);

		// 2. Automated Server Tick Routine: Remove tracking objects when timing windows elapse
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			QTEManager.cleanupExpired(server.getOverworld().getTime(), server);
		});

		// 3. Central QTE Completion Handler
		ServerPlayNetworking.registerGlobalReceiver(QTEHitPacket.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ActiveQTE activeQte = QTEManager.getActiveQTE(player);

				// Confirm a QTE is actually running for this player and matches the incoming payload context
				if (activeQte != null && activeQte.getType() == payload.type()) {
					switch (payload.type()) {
						case RESONANCE -> {
							// RESONANCE QTE: Gather nearby living entities and send their UUIDs for Client ESP
							double radius = 25.0;
							Box area = player.getBoundingBox().expand(radius);
							List<LivingEntity> targets = player.getServerWorld().getEntitiesByClass(
									LivingEntity.class,
									area,
									entity -> entity != player
							);

							List<java.util.UUID> targetUuids = targets.stream().map(Entity::getUuid).toList();

							// Dispatch exclusively back to the user's client graphics pipeline
							ServerPlayNetworking.send(player, new QTEESPPacket(targetUuids));
						}

						case PULSE -> {
							// PULSE QTE: EMP Wave — Forces 10-second penalty cooldown on nearby players holding Arcs
							double range = 10.0;
							Box area = player.getBoundingBox().expand(range);
							List<PlayerEntity> playersInRange = player.getServerWorld().getEntitiesByClass(
									PlayerEntity.class,
									area,
									p -> p != player
							);

							// Reference your custom item instances safely from ModItems
							List<Item> arcItems = List.of(
									ModItems.RESONANCE_ARC,
									ModItems.FREEZE_ARC,
									ModItems.DASH_ARC,
									ModItems.PULSE_ARC,
									ModItems.INFINITY_ARC
							);

							for (PlayerEntity targetPlayer : playersInRange) {
								for (Item arc : arcItems) {
									if (arc != null) {
										// 200 ticks = 10 seconds weapon lock penalty
										targetPlayer.getItemCooldownManager().set(arc, 200);
									}
								}
							}
						}

						case INFINITY -> {
							// INFINITY QTE: Hollow Purple projectile payload release
							// Spawns a baseline velocity entity (can be swapped for a custom projectile entity later)
							SnowballEntity purpleBall = new SnowballEntity(player.getWorld(), player);
							purpleBall.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 3.5f, 0.0f);
							player.getWorld().spawnEntity(purpleBall);
						}
					}

					// Finalize and tear down server tracking record
					QTEManager.removeQTE(player);
				}
			});
		});
	}
}