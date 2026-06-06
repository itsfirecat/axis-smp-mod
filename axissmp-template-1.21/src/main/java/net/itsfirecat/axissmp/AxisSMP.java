package net.itsfirecat.axissmp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.itsfirecat.axissmp.item.ModItems;
import net.itsfirecat.axissmp.item.ModItemGroups;
import net.itsfirecat.axissmp.network.QTEClearPacket;
import net.itsfirecat.axissmp.network.QTEESPPacket;
import net.itsfirecat.axissmp.network.QTEHitPacket;
import net.itsfirecat.axissmp.network.QTEStartPacket;
import net.itsfirecat.axissmp.qte.QTEManager;
import net.itsfirecat.block.ModBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AxisSMP implements ModInitializer {
	public static final String MOD_ID = "axissmp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		// Register networking payload configurations under 1.21.1 protocols
		PayloadTypeRegistry.playS2C().register(QTEStartPacket.ID, QTEStartPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(QTEClearPacket.ID, QTEClearPacket.CODEC);

		// FIX: Register QTEESPPacket as S2C (Server-to-Client) because the server sends it to the client
		PayloadTypeRegistry.playS2C().register(QTEESPPacket.ID, QTEESPPacket.CODEC);

		// Register QTEHitPacket as C2S because the client sends it to the server
		PayloadTypeRegistry.playC2S().register(QTEHitPacket.ID, QTEHitPacket.CODEC);

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			QTEManager.cleanupExpired(server.getOverworld().getTime(), server);
		});

		// Clean up: Merged the duplicate listeners into a single clean handler block
		ServerPlayNetworking.registerGlobalReceiver(QTEHitPacket.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				if (QTEManager.getActiveQTE(player) != null) {
					double radius = 25.0;
					Box area = player.getBoundingBox().expand(radius);
					List<LivingEntity> targets = player.getServerWorld().getEntitiesByClass(LivingEntity.class, area, entity -> entity != player);

					// Collect target UUIDs
					List<java.util.UUID> targetUuids = targets.stream().map(Entity::getUuid).toList();

					// Send the target UUID collection exclusively back to the client that executed the QTE successfully
					ServerPlayNetworking.send(player, new QTEESPPacket(targetUuids));

					QTEManager.removeQTE(player);
				}
			});
		});
	}
}