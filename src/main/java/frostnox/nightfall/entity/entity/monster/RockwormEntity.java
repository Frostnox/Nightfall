package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.block.block.nest.GuardedNestBlockEntity;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.ai.sensing.AudioSensing;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.MathUtil;
import frostnox.nightfall.util.animation.AnimationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;

public class RockwormEntity extends MonsterEntity {
    public int retreatCooldown;

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

    public boolean enterNest(boolean simulate) {
        BlockPos.MutableBlockPos pos = blockPosition().mutable();
        LevelChunk chunk = level.getChunkAt(pos);
        for(int i = 0; i < 16; i++) {
            BlockState belowState = chunk.getBlockState(pos.setY(pos.getY() - 1));
            if(belowState.is(BlocksNF.ANCHORING_RESIN.get())) {
                if(chunk.getBlockEntity(pos) instanceof GuardedNestBlockEntity nest) {
                    if(!nest.isFull()) {
                        if(!simulate) {
                            getActionTracker().startAction(ActionsNF.EMPTY.getId());
                            nest.addEntity(this);
                        }
                        return true;
                    }
                    else return false;
                }
            }
            else if(!belowState.is(TagsNF.STONE_TUNNELS)) return false;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        setYBodyRot(getYHeadRot());
        if(!level.isClientSide && isAlive()) {
            if(tickCount > 1 && !verticalCollisionBelow) hurt(DamageTypeSource.UPROOTED, Float.MAX_VALUE);
            else {
                IActionTracker capA = getActionTracker();
                Entity target = null;
                double bestDistSqr = Double.MAX_VALUE;
                Vec3 eyePos = getEyePosition();
                for(Entity entity : audioSensing.getHeardEntities()) {
                    double dist = MathUtil.getShortestDistanceSqrPointToBox(eyePos.x, eyePos.y, eyePos.z, entity.getBoundingBox());
                    if(dist < bestDistSqr && dist < 2.1 * 2.1) target = entity;
                }
                if(target != null) {
                    getLookControl().setLookAt(target);
                    if(capA.isInactive()) startAction(ActionsNF.ROCKWORM_BITE.getId());
                }
                else if(capA.isInactive()) {
                    if(retreatCooldown > 0) retreatCooldown--;
                    if(retreatCooldown == 0 && tickCount > 20 && enterNest(true)) {
                        startAction(ActionsNF.ROCKWORM_RETREAT.getId());
                        retreatCooldown = 20 * 20 + random.nextInt(20 * 10);
                    }
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        retreatCooldown = tag.getInt("retreatCooldown");
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tag.putInt("retreatCooldown", retreatCooldown);
    }

    @Override
    public int getHeadRotSpeed() {
        return 20;
    }

    @Override
    protected AudioSensing createAudioSensing() {
        return new AudioSensing(this, 30);
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
