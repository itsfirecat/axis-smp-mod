package net.itsfirecat.axissmp.item.custom;

import net.itsfirecat.axissmp.qte.QTEManager;
import net.itsfirecat.axissmp.qte.QTEType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;

public class ResonanceArcItem extends Item {
    public ResonanceArcItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            if (user.getItemCooldownManager().isCoolingDown(this)) {
                return TypedActionResult.fail(stack);
            }

            executeBaseAbility(serverPlayer, (ServerWorld) world);

            // Double time allocation (40 ticks = 2 seconds total loop: 1s forward, 1s back)
            QTEManager.startQTE(serverPlayer, QTEType.RESONANCE, 40, world.getTime());

            // Cooldown handling: 60 Ticks = 3 Seconds testing window. Swap to 8400 later.
            user.getItemCooldownManager().set(this, 60);
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    public static void executeBaseAbility(ServerPlayerEntity player, ServerWorld world) {
        double radius = 10.0;
        Box area = player.getBoundingBox().expand(radius);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, area, entity -> entity != player);

        for (LivingEntity entity : targets) {
            Vec3d direction = player.getPos().subtract(entity.getPos()).normalize();
            entity.setVelocity(direction.multiply(1.2));
            entity.velocityModified = true;
        }
    }
}