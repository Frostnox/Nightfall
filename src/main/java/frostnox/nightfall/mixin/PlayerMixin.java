package frostnox.nightfall.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    private PlayerMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    /**
     * @author Frostnox
     * @reason Allowing players to change the main arm would cause significant functional changes during attacks, so the feature is removed
     */
    @Overwrite
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
