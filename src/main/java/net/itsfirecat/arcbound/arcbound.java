package net.itsfirecat.arcbound;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.itsfirecat.arcbound.entity.HollowPurpleEntity;
import net.itsfirecat.arcbound.item.ModItems;
import net.itsfirecat.arcbound.item.ModItemGroups;
import net.itsfirecat.arcbound.network.*;
import net.itsfirecat.arcbound.qte.ActiveQTE;
import net.itsfirecat.arcbound.qte.QTEManager;
import net.itsfirecat.arcbound.sound.ArcSoundEvents;
import net.itsfirecat.block.ModBlocks;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class arcbound implements ModInitializer {
	public static final String MOD_ID = "arcbound";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final int PULSE_FLASH_DURATION_MS = 1500;

	public static final EntityType<HollowPurpleEntity> HOLLOW_PURPLE_ENTITY = Registry.register(
			Registries.ENTITY_TYPE,
			Identifier.of("arcbound", "hollow_purple"),
			FabricEntityTypeBuilder.<HollowPurpleEntity>create(SpawnGroup.MISC, HollowPurpleEntity::new)
					.dimensions(EntityDimensions.fixed(1.0f, 1.0f))
					.trackRangeBlocks(64)
					.trackedUpdateRate(10)
					.build()
	);
	@Override
	public void onInitialize() {
		// Register standard mod elements
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		// Register Audio
		net.itsfirecat.arcbound.sound.ArcSoundEvents.register();

		// 1. Register modern Fabric 1.21.1 network payload structures
		// Server-to-Client payloads
		PayloadTypeRegistry.playS2C().register(QTEStartPacket.ID, QTEStartPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(QTEClearPacket.ID, QTEClearPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(QTEESPPacket.ID, QTEESPPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ArcFlashPacket.ID, ArcFlashPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(ArcVisualPayload.ID, ArcVisualPayload.CODEC);

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
							System.out.println("[Arcbound-Debug] Server received PULSE QTE success for player: " + player.getName().getString());

							double range = 10.0;
							net.minecraft.util.math.Box area = player.getBoundingBox().expand(range);

							player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
									net.minecraft.entity.effect.StatusEffects.SLOWNESS,
									40,
									255,
									false,
									false,
									false
							));

							List<ServerPlayerEntity> nearbyServerPlayers = player.getServerWorld().getEntitiesByClass(
									ServerPlayerEntity.class,
									player.getBoundingBox().expand(range),
									p -> true
							);

							System.out.println("[Arcbound-Debug] Broadcasting visual states to " + nearbyServerPlayers.size() + " nearby players.");

							for (ServerPlayerEntity nearby : nearbyServerPlayers) {
								ServerPlayNetworking.send(nearby, new ArcVisualPayload(player.getUuid(), 6));
								ServerPlayNetworking.send(nearby, new ArcFlashPacket(arcbound.PULSE_FLASH_DURATION_MS, 0x0A0A0A));
							}

							player.setVelocity(0.0, 0.45, 0.0);
							player.velocityModified = true;
							System.out.println("[Arcbound-Debug] Server applied initial upward velocity vector.");

							List<PlayerEntity> playersInRange = player.getServerWorld().getEntitiesByClass(
									PlayerEntity.class,
									area,
									p -> p != player
							);

							List<Item> arcItems = List.of(
									ModItems.RESONANCE_ARC,
									ModItems.FREEZE_ARC,
									ModItems.DASH_ARC,
									ModItems.PULSE_ARC,
									ModItems.INFINITY_ARC
							);

							int lockCount = 0;
							for (PlayerEntity targetPlayer : playersInRange) {
								for (Item arc : arcItems) {
									if (arc != null) {
										targetPlayer.getItemCooldownManager().set(arc, 200);
										lockCount++;
									}
								}
							}
							System.out.println("[Arcbound-Debug] Server locked down " + lockCount + " total arc items on nearby targets.");

							player.getServerWorld().playSound(
									null,
									player.getBlockPos(),
									ArcSoundEvents.PULSE_EMP,
									SoundCategory.PLAYERS,
									1.5f,
									0.8f
							);
						}

						case INFINITY -> {
							// Start the cinematic charging phase instead of shooting instantly
							net.itsfirecat.arcbound.entity.HollowPurpleChargeEntity charge =
									new net.itsfirecat.arcbound.entity.HollowPurpleChargeEntity(player.getWorld(), player);
							player.getWorld().spawnEntity(charge);
						}

					}

					// Finalize and tear down server tracking record
					QTEManager.removeQTE(player);
				}
			});
		});
	}
}