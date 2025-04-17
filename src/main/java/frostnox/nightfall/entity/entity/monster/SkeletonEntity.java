package frostnox.nightfall.entity.entity.monster;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.data.TagsNF;
import frostnox.nightfall.entity.EntityPart;
import frostnox.nightfall.entity.ai.goals.FleeDamageGoal;
import frostnox.nightfall.entity.ai.goals.RangedAttackGoal;
import frostnox.nightfall.entity.ai.goals.RushAttackGoal;
import frostnox.nightfall.entity.ai.goals.WanderLandGoal;
import frostnox.nightfall.entity.ai.goals.target.TrackNearestTargetGoal;
import frostnox.nightfall.entity.ai.pathfinding.FlankingLandEntityNavigator;
import frostnox.nightfall.item.Armament;
import frostnox.nightfall.item.IProjectileItem;
import frostnox.nightfall.item.TieredItemMaterial;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import frostnox.nightfall.registry.forge.ParticleTypesNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.util.animation.AnimationData;
import frostnox.nightfall.world.MoonPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Random;

public class SkeletonEntity extends UndeadEntity {
    public final float headRotation;
    private final Goal meleeGoal = new RushAttackGoal(this, 1D);
    private final Goal rangedGoal = new RangedAttackGoal(this, 1D, 16F);
    public IProjectileItem arrowItem = ItemsNF.RUSTED_ARROW.get();

    public SkeletonEntity(EntityType<? extends SkeletonEntity> type, Level worldIn) {
        super(type, worldIn);
        Random rand = new Random();
        headRotation = (float) Math.toRadians(15 * rand.nextFloat() * (rand.nextBoolean() ? 1 : -1));
        updateGoals();
    }

    public static AttributeSupplier.Builder getAttributeMap() {
        return createAttributes().add(Attributes.MAX_HEALTH, 80D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0D)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.ATTACK_KNOCKBACK, 1)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 30)
                .add(AttributesNF.HEARING_RANGE.get(), 15)
                .add(AttributesNF.PIERCING_DEFENSE.get(), 5)
                .add(AttributesNF.PIERCING_ABSORPTION.get(), 0.5)
                .add(AttributesNF.SLASHING_DEFENSE.get(), 5)
                .add(AttributesNF.SLASHING_ABSORPTION.get(), 0.5)
                .add(AttributesNF.STRIKING_ABSORPTION.get(), -0.25);
    }

    public static EnumMap<EntityPart, AnimationData> getRightArmAnimMap() {
        EnumMap<EntityPart, AnimationData> map = new EnumMap<>(EntityPart.class);
        map.put(EntityPart.BODY, new AnimationData(new Vector3f(6F/16F, -2F/16F, 0F/16F), new Vector3f(5, 0, 0)));
        map.put(EntityPart.ARM_RIGHT, new AnimationData(new Vector3f(0F/16F, 9F/16F, 0F/16F), new Vector3f(-5, 0, 0)));
        map.put(EntityPart.HAND_RIGHT, new AnimationData(new Vector3f(0F/16F, 9F/16F, 0F/16F)));
        return map;
    }

    public void updateGoals() {
        if(!level.isClientSide) {
            goalSelector.removeGoal(meleeGoal);
            goalSelector.removeGoal(rangedGoal);
            if(getMainHandItem().is(TagsNF.BOW)) goalSelector.addGoal(2, rangedGoal);
            else goalSelector.addGoal(2, meleeGoal);
        }
    }

    @Override
    protected float getBaseReach() {
        return 3.5F;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(4, new WanderLandGoal(this, 0.8D));
        goalSelector.addGoal(5, new FleeDamageGoal(this, 0.8D));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new TrackNearestTargetGoal<>(this, Player.class, MoonPhase.get(level) != MoonPhase.FULL));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        if(worldIn.getRandom().nextBoolean()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemsNF.RUSTED_SPEAR.get()));
        }
        else {
            ItemStack bow = new ItemStack(ItemsNF.TWISTED_BOW.get());
            bow.getOrCreateTag().putByte("ammo", (byte) arrowItem.getAmmoId());
            setItemSlot(EquipmentSlot.MAINHAND, bow);
        }
        equipHumanoid(worldIn.getRandom(), 0.3F, 0.3F, 0.3F, 1F,
                MoonPhase.get(worldIn) == MoonPhase.FULL ? 0.25F : 0F);
        updateGoals();
        return spawnDataIn;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if(arrowItem != ItemsNF.RUSTED_ARROW.get()) tag.putString("arrowItem", arrowItem.getItem().getRegistryName().toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if(tag.contains("arrowItem")) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(tag.getString("arrowItem")));
            if(item instanceof IProjectileItem) arrowItem = (IProjectileItem) item;
        }
        updateGoals();
    }

    @Override
    public void setItemSlot(EquipmentSlot p_32138_, ItemStack p_32139_) {
        super.setItemSlot(p_32138_, p_32139_);
        if(!level.isClientSide) updateGoals();
    }

    @Override
    public ItemStack getProjectile(ItemStack p_33038_) {
        return new ItemStack((Item) arrowItem);
    }

    @Override
    public float getSafeDistanceToTarget() {
        return 2F * 2F;
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        if(isDeadOrDying()) return super.getStandingEyeHeight(poseIn, sizeIn);
        return 1.7F;
    }

    @Override
    protected int decreaseAirSupply(int pAir) {
        return pAir;
    }

    @Override
    public ResourceLocation pickActionEnemy(double distanceSqr, Entity target) {
        if(getItemBySlot(EquipmentSlot.MAINHAND).is(TagsNF.BOW)) return ActionsNF.SKELETON_SHOOT.getId();
        else return ActionsNF.SKELETON_THRUST.getId();
    }

    @Override
    public EquipmentSlot getHitSlot(Vector3d hitPos, int boxIndex) {
        double y = hitPos.y;
        if(y > 5D/16D && y <= 12D/16D) return EquipmentSlot.LEGS;
        else if(y > 12D/16D && y <= 24D/16D) return EquipmentSlot.CHEST;
        else if(y > 24D/16D) return EquipmentSlot.HEAD;
        return EquipmentSlot.FEET;
    }

    @Override
    public ParticleOptions getHurtParticle() {
        return ParticleTypesNF.FRAGMENT_BONE.get();
    }

    @Override
    public boolean canDoRangedAction() {
        return getMainHandItem().is(TagsNF.BOW);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlankingLandEntityNavigator(this, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundsNF.SKELETON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundsNF.SKELETON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundsNF.SKELETON_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState pBlock) {
        playSound(SoundsNF.SKELETON_STEP.get(), 0.15F, 1.0F);
    }
}
