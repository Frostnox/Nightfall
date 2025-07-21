package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;

public class RockwormEntity extends MonsterEntity {
    public RockwormEntity(EntityType<? extends MonsterEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 80D)
                .add(Attributes.MOVEMENT_SPEED, 0F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.STRIKING_ABSORPTION.get(), 0.25)
                .add(AttributesNF.SLASHING_ABSORPTION.get(), 0.5)
                .add(AttributesNF.PIERCING_ABSORPTION.get(), 0.5);
    }

    public static EnumMap<EntityPart, AnimationData> getHeadAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(0F/16F, -26F/16F, 0F/16F), new Vector3f(10, 0, 0)));
        map.put(EntityPart.BODY_2, new AnimationData(new Vector3f(0F/16F, -13F/16F, 0F/16F), new Vector3f(10, 0, 0)));
        map.put(EntityPart.HEAD, new AnimationData(new Vector3f(0F/16F, 0F/16F, 0F/16F), new Vector3f(75, 0, 0)));
        return map;
    }

    @Override
    public void tick() {
        super.tick();
        if(!level.isClientSide && isAlive()) {
            if(tickCount > 1 && !verticalCollisionBelow) hurt(DamageTypeSource.UPROOTED, Float.MAX_VALUE);
            else {
                IActionTracker capA = getActionTracker();
                if(capA.isInactive()) {
                    //startAction(ActionsNF.ROCKWORM_BITE.getId());
                    BlockPos belowPos = blockPosition().below();
                    BlockState belowState = level.getBlockState(belowPos);
                    if(belowState.is(BlocksNF.ANCHORING_RESIN.get())) {

                    }
                }
            }
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        if(isDeadOrDying()) return new EntityDimensions(14F/16F, 4F/16F, false).scale(getScale());
        else return getType().getDimensions().scale(getScale());
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void setDeltaMovement(Vec3 pMotion) {
        super.setDeltaMovement(new Vec3(0, pMotion.y, 0));
    }

    @Override
    public ParticleOptions getHurtParticle() {
        return ParticleTypesNF.BLOOD_PALE_BLUE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.ROCKWORM_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.ROCKWORM_DEATH.get();
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        return EquipmentSlot.CHEST;
    }
}
