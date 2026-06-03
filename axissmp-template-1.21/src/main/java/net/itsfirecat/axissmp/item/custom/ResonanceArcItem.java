package net.itsfirecat.axissmp.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ResonanceArcItem extends Item {

    private static final double RADIUS = 10.0;
    private static final int COOLDOWN = 8400;

    public ResonanceArcItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient()) {

            Box box = user.getBoundingBox().expand(RADIUS);

            List<LivingEntity> entities = world.getEntitiesByClass(
                    LivingEntity.class,
                    box,
                    e -> e != user
            );

            for (LivingEntity entity : entities) {

                Vec3d direction = new Vec3d(
                        user.getX() - entity.getX(),
                        (user.getY() - entity.getY()) * 0.2, // reduced vertical pull
                        user.getZ() - entity.getZ()
                );

                if (direction.lengthSquared() == 0) continue;

                direction = direction.normalize().multiply(1.2);

                entity.setVelocity(direction);
                entity.velocityModified = true;
            }

            user.getItemCooldownManager().set(this, COOLDOWN);
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}