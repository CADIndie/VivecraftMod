package com.example.examplemod.mixin.world.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Entity {

	protected AbstractArrowMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V")
    public void pickup(EntityType<? extends AbstractArrow> p_36717_, LivingEntity p_36718_, Level p_36719_,
                       CallbackInfo info) {
        if (p_36718_ instanceof Player) {
            ServerVivePlayer serverviveplayer = NetworkHelper.vivePlayers.get(p_36718_.getUUID());
            if (serverviveplayer != null && serverviveplayer.isVR()) {
                Vec3 vec3 = serverviveplayer.getControllerPos(serverviveplayer.activeHand, (Player) p_36718_);
                Vec3 vec31 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);

                if (!serverviveplayer.isSeated() && serverviveplayer.getDraw() > 0.0F) {
                    vec31 = serverviveplayer.getControllerPos(1, (Player) p_36718_)
                            .subtract(serverviveplayer.getControllerPos(0, (Player) p_36718_)).normalize();
                    vec3 = serverviveplayer.getControllerPos(0, (Player) p_36718_);
                }

                this.setPos(vec3.x + vec31.x, vec3.y + vec31.y, vec3.z + vec31.z);
            }
        }

    }
}
