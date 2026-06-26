package net.itsfirecat.arcbound.item.custom;

import net.itsfirecat.arcbound.util.ArcboundCooldowns;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DashArcItem extends Item {

    private static final double DISTANCE = 6.0;

    public DashArcItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient()) {

            Vec3d start = user.getPos();
            Vec3d dir = user.getRotationVec(1.0F);

            Vec3d step = dir.normalize().multiply(0.5);
            Vec3d current = start;

            int solidCount = 0;
            Vec3d lastSafe = start;

            for (double d = 0; d < DISTANCE; d += 0.5) {

                current = current.add(step);
                BlockPos pos = BlockPos.ofFloored(current);

                var state = world.getBlockState(pos);
                boolean solid = !state.getCollisionShape(world, pos).isEmpty();

                if (solid) {
                    solidCount++;
                } else {
                    solidCount = 0;
                    lastSafe = current;
                }

                // thick wall = stop dash
                if (solidCount > 2) {
                    break;
                }
            }

            Vec3d oldVel = user.getVelocity();

            user.requestTeleport(lastSafe.x, lastSafe.y, lastSafe.z);

            // momentum carry so it doesn't feel dead
            user.setVelocity(
                    oldVel.x + dir.x * 0.9,
                    oldVel.y,
                    oldVel.z + dir.z * 0.9
            );

            user.velocityModified = true;

            user.getItemCooldownManager().set(this, ArcboundCooldowns.getDashCooldown());
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}