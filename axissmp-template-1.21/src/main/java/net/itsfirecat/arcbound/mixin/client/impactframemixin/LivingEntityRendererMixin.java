package net.itsfirecat.arcbound.mixin.client.impactframemixin;

import net.itsfirecat.arcbound.client.ArcImpactHandler;
import net.itsfirecat.arcbound.client.ArcImpactRenderType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderLayer", at = @At("RETURN"), cancellable = true)
    private void coopSwapRenderLayer(T entity, boolean showBody, boolean translucent,
                                     boolean showOutline,
                                     CallbackInfoReturnable<RenderLayer> cir) {
        if (ArcImpactHandler.playing) {
                    ArcImpactRenderType.isReady();
        }
        if (!ArcImpactHandler.playing) return;
        if (!ArcImpactRenderType.isReady()) return;

        Identifier texture = ((EntityRenderer<T>) (Object) this).getTexture(entity);

        switch (ArcImpactHandler.currentFrameType) {
            case WHITE, RED  -> cir.setReturnValue(ArcImpactRenderType.getBlackLayer(texture));
            case BLACK, CYAN -> cir.setReturnValue(ArcImpactRenderType.getWhiteLayer(texture));
            case INVERT      -> cir.setReturnValue(ArcImpactRenderType.getInvertLayer(texture));
        }
    }
}