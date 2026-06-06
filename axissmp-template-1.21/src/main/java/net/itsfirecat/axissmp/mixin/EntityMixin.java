package net.itsfirecat.axissmp.mixin;

import net.itsfirecat.axissmp.qte.client.ClientQTE;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract UUID getUuid();

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void forceClientSideESP(CallbackInfoReturnable<Boolean> cir) {
        // If this entity's UUID is marked in our local client-side ESP list, force it to render glowing
        if (ClientQTE.shouldOutlineEntity(this.getUuid())) {
            cir.setReturnValue(true);
        }
    }
}