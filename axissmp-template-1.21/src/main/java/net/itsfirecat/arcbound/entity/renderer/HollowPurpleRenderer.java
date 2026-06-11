package net.itsfirecat.arcbound.entity.renderer;

import net.itsfirecat.arcbound.arcbound;
import net.itsfirecat.arcbound.entity.HollowPurpleEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class HollowPurpleRenderer extends EntityRenderer<HollowPurpleEntity> {

    private static final Identifier TEXTURE =
            Identifier.of(arcbound.MOD_ID, "textures/entity/hollow_purple.png");
    private static final float SCALE = 3.0f;

    public HollowPurpleRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(HollowPurpleEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(HollowPurpleEntity entity, float yaw, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.scale(SCALE, SCALE, SCALE);

        // Billboard — always face the camera
        matrices.multiply(this.dispatcher.getRotation());

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityTranslucentCull(TEXTURE));

        float s = 0.5f;
        // Full brightness, ignore world lighting so it glows
        int fullbright = 0xF000F0;

        consumer.vertex(matrix, -s,  s, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(fullbright).normal(0, 1, 0);
        consumer.vertex(matrix, -s, -s, 0).color(1f, 1f, 1f, 1f).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(fullbright).normal(0, 1, 0);
        consumer.vertex(matrix,  s, -s, 0).color(1f, 1f, 1f, 1f).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(fullbright).normal(0, 1, 0);
        consumer.vertex(matrix,  s,  s, 0).color(1f, 1f, 1f, 1f).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(fullbright).normal(0, 1, 0);

        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}