package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.entity.ai.goal.FleeDamageGoal;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.ai.goal.MineToTargetGoal;
import frostnox.nightfall.entity.ai.goal.RushAttackGoal;
import frostnox.nightfall.entity.ai.goal.WanderLandGoal;
import frostnox.nightfall.entity.ai.goal.target.TrackNearestTargetGoal;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.*;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class HuskEntity extends UndeadEntity {
    public HuskEntity(EntityType<? extends HuskEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 120D)
                .add(Attributes.MOVEMENT_SPEED, 0.275F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.STRIKING_DEFENSE.get(), 0.1)
                .add(AttributesNF.SLASHING_DEFENSE.get(), 0.1)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 0.1)
                .add(AttributesNF.ELECTRIC_DEFENSE.get(), 0.1);
    }

    public static EnumMap<EntityPart, AnimationData> getRightArmAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(6F/16F, -2F/16F, 0F/16F), new Vector3f(12.5F, 0, 0)));
        map.put(EntityPart.ARM_RIGHT, new AnimationData(new Vector3f(0F/16F, 8F/16F, 0F/16F)));
        map.put(EntityPart.HAND_RIGHT, new AnimationData(new Vector3f(0F/16F, 8F/16F, 0F/16F), new Vector3f(-20F, -3, -3)));
        return map;
    }

    public static EnumMap<EntityPart, AnimationData> getLeftArmAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(-6F/16F, -2F/16F, 0F/16F), new Vector3f(12.5F, 0, 0)));
        map.put(EntityPart.ARM_LEFT, new AnimationData(new Vector3f(0F/16F, 8F/16F, 0F/16F)));
        map.put(EntityPart.HAND_LEFT, new AnimationData(new Vector3f(0F/16F, 8F/16F, 0F/16F), new Vector3f(-20F, 3, 3)));
        return map;
    }

    @Override
    protected float getBaseReach() {
        return 3.5F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new RushAttackGoal(this, 1D));
        this.goalSelector.addGoal(4, new MineToTargetGoal(this));
        this.goalSelector.addGoal(5, new FleeDamageGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new WanderLandGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, Player.class, MoonPhase.get(level) != MoonPhase.FULL));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        if(worldIn.getRandom().nextFloat() < 0.9F) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemsNF.RUSTED_MAUL.get()));
        }
        equipHumanoid(worldIn.getRandom(), 0.45F, 0.5F, 0.55F, 0.4F,
                MoonPhase.get(worldIn) == MoonPhase.FULL ? 0.25F : 0F);
        return spawnDataIn;
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.isAlive()) return;
        IActionTracker capA = getActionTracker();
        //Outdated way of doing this, use the onTick function in Action instead
        if(!capA.isInactive() && !capA.isStunned() && this.getTarget() != null) {
            ResourceLocation id = capA.getActionID();
            if(id.equals(ActionsNF.HUSK_OVERHEAD.getId())) {
                if(capA.getFrame() == 1 && capA.getState() == 1) {
                    CombatUtil.addFacingMovement(0.5, this);
                }
            }
            else if(id.equals(ActionsNF.HUSK_RIGHT_SWIPE_2.getId()) || id.equals(ActionsNF.HUSK_LEFT_SWIPE_2.getId())) {
                if(capA.getFrame() == 1 && capA.getState() == 1) {
                    CombatUtil.addFacingMovement(0.3, this);
                }
            }
            else if(id.equals(ActionsNF.HUSK_RIGHT_SWIPE_1.getId()) || id.equals(ActionsNF.HUSK_LEFT_SWIPE_1.getId())) {
                double reach = ActionsNF.get(id).getChain(this).get().getMaxDistToStart(this);
                double distSqr = CombatUtil.getShortestDistanceSqr(this, getTarget());
                if(distSqr <= reach * reach && capA.getState() == 2) queueAction();
                else if(capA.getFrame() == 1 && capA.getState() == 1) {
                    CombatUtil.addFacingMovement(0.3, this);
                }
            }
        }
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        if(isDeadOrDying()) return super.getStandingEyeHeight(poseIn, sizeIn);
        return 1.7F;
    }

    @Override
    public boolean canMineAnything() {
        return !getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && getTargetPos() != null;
    }

    @Override
    public boolean canMineBlock(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if(state.is(BlocksNF.MOON_ESSENCE.get())) return false;
        boolean climbable = state.is(BlockTags.CLIMBABLE);
        if(!climbable && state.getShape(level, pos).isEmpty()) return false;
        double x = pos.getX() + 0.5 - getX();
        double y = pos.getY() + 0.5 - getEyeY();
        double z = pos.getZ() + 0.5 - getZ();
        if(x * x + y * y + z * z > getReachSqr()) return false;
        return (climbable || state.getMaterial().blocksMotion()) && state.getDestroySpeed(level, pos) >= 0F;
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, LivingEntity target) {
        if(random.nextDouble() < Math.min(0.85, 0.15 + distanceSqr / (2.5 * 2.5))) return ActionsNF.HUSK_OVERHEAD.getId();
        else {
            boolean mainItem = !getMainHandItem().isEmpty(), offItem = !getOffhandItem().isEmpty();
            float mainWeight;
            if(mainItem == offItem) mainWeight = 0.6F;
            else if(mainItem) mainWeight = 0.9F;
            else mainWeight = 0.15F;
            if(random.nextDouble() < mainWeight) return ActionsNF.HUSK_RIGHT_SWIPE_1.getId();
            else return ActionsNF.HUSK_LEFT_SWIPE_1.getId();
        }
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        double y = hitPos.y;
        if(y > 5D/16D && y <= 12D/16D) return EquipmentSlot.LEGS;
        else if(y > 12D/16D && y <= 23D/16D) return EquipmentSlot.CHEST;
        else if(y > 23D/16D) return EquipmentSlot.HEAD;
        return EquipmentSlot.FEET;
    }

    @Override
    public ParticleOptions getHurtParticle() {
        return ParticleTypesNF.BLOOD_DARK_RED.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsNF.HUSK_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.HUSK_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.HUSK_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        this.playSound(SoundsNF.HUSK_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public float getVoicePitch() {
        return 1.0F + (random.nextFloat() - random.nextFloat()) * 0.1F;
    }
}
