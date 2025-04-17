package frostnox.nightfall.entity.entity;

import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.block.Tree;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.item.item.BuildingMaterialItem;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.registry.forge.BlocksNF;
import frostnox.nightfall.registry.forge.ItemsNF;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * TODO: Allow turning armor stands into dummies that can be hit with weapons to test damage
 * Allows placing items in hands (vanilla is capable of this but doesn't allow it in survival)
 * Supports texture variants
 */
public class ArmorStandDummyEntity extends LivingEntity {
    private static final Rotations ZERO = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0F, 0.0F, -10.0F);
    private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0F, 0.0F, 10.0F);
    private static final EntityDimensions MARKER_DIMENSIONS = new EntityDimensions(0.0F, 0.0F, true);
    private static final double FEET_OFFSET = 0.1D;
    private static final double CHEST_OFFSET = 0.9D;
    private static final double LEGS_OFFSET = 0.4D;
    private static final double HEAD_OFFSET = 1.6D;
    public static final int DISABLE_TAKING_OFFSET = 8;
    public static final int DISABLE_PUTTING_OFFSET = 16;
    public static final int CLIENT_FLAG_SMALL = 1;
    public static final int CLIENT_FLAG_SHOW_ARMS = 4;
    public static final int CLIENT_FLAG_NO_BASEPLATE = 8;
    public static final int CLIENT_FLAG_MARKER = 16;
    public static final EntityDataAccessor<Byte> DATA_CLIENT_FLAGS = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_BODY_POSE = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_ARM_POSE = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_ARM_POSE = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_LEFT_LEG_POSE = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.ROTATIONS);
    public static final EntityDataAccessor<Rotations> DATA_RIGHT_LEG_POSE = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.ROTATIONS);
    private static final EntityDataAccessor<String> MATERIAL = SynchedEntityData.defineId(ArmorStandDummyEntity.class, EntityDataSerializers.STRING);
    private static final Predicate<Entity> RIDEABLE_MINECARTS = (entity) -> entity instanceof AbstractMinecart cart && cart.canBeRidden();
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
    private boolean invisible;
    public long lastHit;
    private int disabledSlots;
    private Rotations headPose = ZERO;
    private Rotations bodyPose = ZERO;
    private Rotations leftArmPose = DEFAULT_LEFT_ARM_POSE;
    private Rotations rightArmPose = DEFAULT_RIGHT_ARM_POSE;
    private Rotations leftLegPose = ZERO;
    private Rotations rightLegPose = ZERO;

    public ArmorStandDummyEntity(EntityType<? extends ArmorStandDummyEntity> type, Level level) {
        super(type, level);
        maxUpStep = 0.0F;
    }

    public Item getAsItem() {
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(getMaterial().replace("plank", "armor_stand")));
    }

    private void setMaterial(String name) {
        if(name.contains(":")) entityData.set(MATERIAL, name);
    }

    /**
     * @param id registry ID of type from item used to create this
     */
    public void setMaterial(ResourceLocation id) {
        entityData.set(MATERIAL, id.toString());
    }

    public String getMaterial() {
        return entityData.get(MATERIAL);
    }

    public void setShowArms(boolean pShowArms) {
        entityData.set(DATA_CLIENT_FLAGS, setBit(entityData.get(DATA_CLIENT_FLAGS), 4, pShowArms));
    }

    protected byte setBit(byte p_31570_, int p_31571_, boolean p_31572_) {
        if (p_31572_) {
            p_31570_ = (byte)(p_31570_ | p_31571_);
        } else {
            p_31570_ = (byte)(p_31570_ & ~p_31571_);
        }

        return p_31570_;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_CLIENT_FLAGS, (byte)CLIENT_FLAG_SHOW_ARMS);
        entityData.define(DATA_HEAD_POSE, ZERO);
        entityData.define(DATA_BODY_POSE, ZERO);
        entityData.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
        entityData.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
        entityData.define(DATA_LEFT_LEG_POSE, ZERO);
        entityData.define(DATA_RIGHT_LEG_POSE, ZERO);
        entityData.define(MATERIAL, ItemsNF.PLANKS.get(Tree.OAK).getId().toString());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        ListTag listtag = new ListTag();

        for(ItemStack itemstack : armorItems) {
            CompoundTag compoundtag = new CompoundTag();
            if (!itemstack.isEmpty()) {
                itemstack.save(compoundtag);
            }

            listtag.add(compoundtag);
        }

        pCompound.put("ArmorItems", listtag);
        ListTag listtag1 = new ListTag();

        for(ItemStack itemstack1 : handItems) {
            CompoundTag compoundtag1 = new CompoundTag();
            if (!itemstack1.isEmpty()) {
                itemstack1.save(compoundtag1);
            }

            listtag1.add(compoundtag1);
        }

        pCompound.put("HandItems", listtag1);
        pCompound.putBoolean("Invisible", isInvisible());
        pCompound.putBoolean("Small", isSmall());
        pCompound.putBoolean("ShowArms", isShowArms());
        pCompound.putInt("DisabledSlots", disabledSlots);
        pCompound.putBoolean("NoBasePlate", isNoBasePlate());
        if(isMarker()) pCompound.putBoolean("Marker", isMarker());

        pCompound.put("Pose", writePose());

        pCompound.putString("Material", getMaterial());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("ArmorItems", 9)) {
            ListTag listtag = pCompound.getList("ArmorItems", 10);

            for(int i = 0; i < armorItems.size(); ++i) {
                armorItems.set(i, ItemStack.of(listtag.getCompound(i)));
            }
        }

        if (pCompound.contains("HandItems", 9)) {
            ListTag listtag1 = pCompound.getList("HandItems", 10);

            for(int j = 0; j < handItems.size(); ++j) {
                handItems.set(j, ItemStack.of(listtag1.getCompound(j)));
            }
        }

        setInvisible(pCompound.getBoolean("Invisible"));
        setSmall(pCompound.getBoolean("Small"));
        if(pCompound.contains("ShowArms")) setShowArms(pCompound.getBoolean("ShowArms"));
        disabledSlots = pCompound.getInt("DisabledSlots");
        setNoBasePlate(pCompound.getBoolean("NoBasePlate"));
        setMarker(pCompound.getBoolean("Marker"));
        noPhysics = !hasPhysics();
        CompoundTag compoundtag = pCompound.getCompound("Pose");
        readPose(compoundtag);

        setMaterial(pCompound.getString("Material"));
    }

    @Override
    protected void equipEventAndSound(ItemStack itemStack) {
        SoundEvent soundevent = itemStack.getEquipSound();
        if(soundevent == null && itemStack.getItem() instanceof TieredArmorItem armorItem) soundevent = armorItem.material.getSoundEvent();
        if(soundevent != null && !isSpectator()) {
            gameEvent(GameEvent.EQUIP);
            playSound(soundevent, 1.0F, 1.0F);
        }
    }

    protected void brokenByPlayer(DamageSource pSource) {
        Block.popResource(level, blockPosition(), new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(getMaterial())), 12));
        brokenByAnything(pSource);
    }

    //The Forge event will always fire this on the server despite respecting cancellation on the client
    @Override
    public InteractionResult interactAt(Player pPlayer, Vec3 pVec, InteractionHand pHand) {
        if(!ActionTracker.get(pPlayer).isInactive()) return InteractionResult.FAIL;
        else {
            InteractionResult result = tryItemSwap(pPlayer, pVec, pHand);
            //Should return success so client doesn't start action when server would swing the hand later anyways
            if(pPlayer.level.isClientSide() && result == InteractionResult.CONSUME) return InteractionResult.SUCCESS;
            else return result;
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.getMsgId().equals("lava")) {
            causeDamage(pSource, pAmount);
            return false;
        }
        else if(pSource instanceof DamageTypeSource source) {
            if(source.isFire()) {
                causeDamage(pSource, pAmount);
                return false;
            }
        }
        if(!level.isClientSide && !isRemoved()) {
            if(DamageSource.OUT_OF_WORLD.equals(pSource)) {
                kill();
                return false;
            }
            else if(!isInvulnerableTo(pSource) && !invisible && !isMarker()) {
                if(pSource.isExplosion()) {
                    brokenByAnything(pSource);
                    kill();
                    return false;
                }
                else if(DamageSource.IN_FIRE.equals(pSource)) {
                    if(isOnFire()) causeDamage(pSource, 0.15F);
                    else setSecondsOnFire(5);
                    return false;
                }
                else if(DamageSource.ON_FIRE.equals(pSource) && getHealth() > 0.5F) {
                    causeDamage(pSource, 4.0F);
                    return false;
                }
                else {
                    boolean fromArrow = pSource.getDirectEntity() instanceof AbstractArrow;
                    boolean fromPiercingArrow = fromArrow && ((AbstractArrow)pSource.getDirectEntity()).getPierceLevel() > 0;
                    boolean fromPlayer = pSource.getEntity() instanceof Player;
                    if(!fromPlayer && !fromArrow) return false;
                    else if(fromPlayer && !((Player)pSource.getEntity()).getAbilities().mayBuild) return false;
                    else if(pSource.isCreativePlayer()) {
                        playBrokenSound();
                        showBreakingParticles();
                        kill();
                        return fromPiercingArrow;
                    }
                    else {
                        long time = level.getGameTime();
                        if(time - lastHit > 5L && !fromArrow) {
                            level.broadcastEntityEvent(this, (byte)32);
                            gameEvent(GameEvent.ENTITY_DAMAGED, pSource.getEntity());
                            lastHit = time;
                        }
                        else {
                            brokenByPlayer(pSource);
                            showBreakingParticles();
                            kill();
                        }
                        return true;
                    }
                }
            }
            else return false;
        }
        else return false;
    }

    protected void showBreakingParticles() {
        if(level instanceof ServerLevel) {
            BlockState state;
            if(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(getMaterial())) instanceof BuildingMaterialItem buildingItem
                && buildingItem.getRecipes(level, null).get(0).output instanceof BlockItem blockItem) {
                state = blockItem.getBlock().defaultBlockState();
            }
            else state = BlocksNF.PLANK_BLOCKS.get(Tree.OAK).get().defaultBlockState();
            ((ServerLevel)level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    getX(), getY(0.6666666666666666D), getZ(), 10, (double)(getBbWidth() / 4.0F), (double)(getBbHeight() / 4.0F), (double)(getBbWidth() / 4.0F), 0.05D);
        }
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(getMaterial())));
    }

    @Override
    public void refreshDimensions() {
        double d0 = getX();
        double d1 = getY();
        double d2 = getZ();
        super.refreshDimensions();
        setPos(d0, d1, d2);
    }

    private boolean hasPhysics() {
        return !isMarker() && !isNoGravity();
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && hasPhysics();
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return handItems;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        switch(pSlot.getType()) {
            case HAND:
                return handItems.get(pSlot.getIndex());
            case ARMOR:
                return armorItems.get(pSlot.getIndex());
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
        verifyEquippedItem(pStack);
        switch(pSlot.getType()) {
            case HAND:
                equipEventAndSound(pStack);
                handItems.set(pSlot.getIndex(), pStack);
                break;
            case ARMOR:
                equipEventAndSound(pStack);
                armorItems.set(pSlot.getIndex(), pStack);
        }

    }

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(pItemstack);
        return getItemBySlot(equipmentslot).isEmpty() && !isDisabled(equipmentslot);
    }

    private void readPose(CompoundTag pTagCompound) {
        ListTag listtag = pTagCompound.getList("Head", 5);
        setHeadPose(listtag.isEmpty() ? ZERO : new Rotations(listtag));
        ListTag listtag1 = pTagCompound.getList("Body", 5);
        setBodyPose(listtag1.isEmpty() ? ZERO : new Rotations(listtag1));
        ListTag listtag2 = pTagCompound.getList("LeftArm", 5);
        setLeftArmPose(listtag2.isEmpty() ? DEFAULT_LEFT_ARM_POSE : new Rotations(listtag2));
        ListTag listtag3 = pTagCompound.getList("RightArm", 5);
        setRightArmPose(listtag3.isEmpty() ? DEFAULT_RIGHT_ARM_POSE : new Rotations(listtag3));
        ListTag listtag4 = pTagCompound.getList("LeftLeg", 5);
        setLeftLegPose(listtag4.isEmpty() ? ZERO : new Rotations(listtag4));
        ListTag listtag5 = pTagCompound.getList("RightLeg", 5);
        setRightLegPose(listtag5.isEmpty() ? ZERO : new Rotations(listtag5));
    }

    private CompoundTag writePose() {
        CompoundTag compoundtag = new CompoundTag();
        if (!ZERO.equals(headPose)) {
            compoundtag.put("Head", headPose.save());
        }

        if (!ZERO.equals(bodyPose)) {
            compoundtag.put("Body", bodyPose.save());
        }

        if (!DEFAULT_LEFT_ARM_POSE.equals(leftArmPose)) {
            compoundtag.put("LeftArm", leftArmPose.save());
        }

        if (!DEFAULT_RIGHT_ARM_POSE.equals(rightArmPose)) {
            compoundtag.put("RightArm", rightArmPose.save());
        }

        if (!ZERO.equals(leftLegPose)) {
            compoundtag.put("LeftLeg", leftLegPose.save());
        }

        if (!ZERO.equals(rightLegPose)) {
            compoundtag.put("RightLeg", rightLegPose.save());
        }

        return compoundtag;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity pEntity) {

    }

    @Override
    protected void pushEntities() {
        if(level.isClientSide) return;
        List<Entity> list = level.getEntities(this, getBoundingBox(), RIDEABLE_MINECARTS);
        for(int i = 0; i < list.size(); ++i) {
            Entity entity = list.get(i);
            if(distanceToSqr(entity) <= 0.2D) {
                entity.push(this);
            }
        }
    }

    @Override
    public boolean isInWall() {
        if(level.isClientSide) return false;
        else return super.isInWall();
    }

    protected InteractionResult tryItemSwap(Player pPlayer, Vec3 pVec, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (!isMarker() && !itemstack.is(Items.NAME_TAG)) {
            if (pPlayer.isSpectator()) {
                return InteractionResult.SUCCESS;
            } else if (pPlayer.level.isClientSide) {
                return InteractionResult.CONSUME;
            } else {
                EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
                if (itemstack.isEmpty()) {
                    EquipmentSlot equipmentslot1 = getClickedSlot(pVec);
                    EquipmentSlot equipmentslot2 = isDisabled(equipmentslot1) ? equipmentslot : equipmentslot1;
                    if (hasItemInSlot(equipmentslot2) && swapItem(pPlayer, equipmentslot2, itemstack, pHand)) {
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    if (isDisabled(equipmentslot)) {
                        return InteractionResult.FAIL;
                    }

                    if (equipmentslot.getType() == EquipmentSlot.Type.HAND && !isShowArms()) {
                        return InteractionResult.FAIL;
                    }

                    if (swapItem(pPlayer, equipmentslot, itemstack, pHand)) {
                        return InteractionResult.SUCCESS;
                    }
                }

                return InteractionResult.PASS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private EquipmentSlot getClickedSlot(Vec3 p_31660_) {
        EquipmentSlot equipmentslot = EquipmentSlot.MAINHAND;
        boolean flag = isSmall();
        double d0 = flag ? p_31660_.y * 2.0D : p_31660_.y;
        EquipmentSlot equipmentslot1 = EquipmentSlot.FEET;
        if (d0 >= FEET_OFFSET && d0 < FEET_OFFSET + (flag ? 0.8D : 0.45D) && hasItemInSlot(equipmentslot1)) {
            equipmentslot = EquipmentSlot.FEET;
        } else if (d0 >= CHEST_OFFSET + (flag ? 0.3D : 0.0D) && d0 < CHEST_OFFSET + (flag ? 1.0D : 0.7D) && hasItemInSlot(EquipmentSlot.CHEST)) {
            equipmentslot = EquipmentSlot.CHEST;
        } else if (d0 >= LEGS_OFFSET && d0 < LEGS_OFFSET + (flag ? 1.0D : 0.8D) && hasItemInSlot(EquipmentSlot.LEGS)) {
            equipmentslot = EquipmentSlot.LEGS;
        } else if (d0 >= HEAD_OFFSET && hasItemInSlot(EquipmentSlot.HEAD)) {
            equipmentslot = EquipmentSlot.HEAD;
        } else if (!hasItemInSlot(EquipmentSlot.MAINHAND) && hasItemInSlot(EquipmentSlot.OFFHAND)) {
            equipmentslot = EquipmentSlot.OFFHAND;
        }

        return equipmentslot;
    }

    private boolean isDisabled(EquipmentSlot pSlot) {
        return (disabledSlots & 1 << pSlot.getFilterFlag()) != 0 || pSlot.getType() == EquipmentSlot.Type.HAND && !isShowArms();
    }

    private boolean swapItem(Player pPlayer, EquipmentSlot pSlot, ItemStack pStack, InteractionHand pHand) {
        ItemStack itemstack = getItemBySlot(pSlot);
        if (!itemstack.isEmpty() && (disabledSlots & 1 << pSlot.getFilterFlag() + DISABLE_TAKING_OFFSET) != 0) {
            return false;
        } else if (itemstack.isEmpty() && (disabledSlots & 1 << pSlot.getFilterFlag() + DISABLE_PUTTING_OFFSET) != 0) {
            return false;
        } else if (pPlayer.getAbilities().instabuild && itemstack.isEmpty() && !pStack.isEmpty()) {
            ItemStack itemstack2 = pStack.copy();
            itemstack2.setCount(1);
            setItemSlot(pSlot, itemstack2);
            return true;
        } else if (!pStack.isEmpty() && pStack.getCount() > 1) {
            if (!itemstack.isEmpty()) {
                return false;
            } else {
                ItemStack itemstack1 = pStack.copy();
                itemstack1.setCount(1);
                setItemSlot(pSlot, itemstack1);
                pStack.shrink(1);
                return true;
            }
        } else {
            setItemSlot(pSlot, pStack);
            pPlayer.setItemInHand(pHand, itemstack);
            return true;
        }
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 32) {
            if (level.isClientSide) {
                level.playLocalSound(getX(), getY(), getZ(), SoundEvents.ARMOR_STAND_HIT, getSoundSource(), 0.3F, 1.0F, false);
                lastHit = level.getGameTime();
            }
        } else {
            super.handleEntityEvent(pId);
        }

    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        double d0 = getBoundingBox().getSize() * 4.0D;
        if (Double.isNaN(d0) || d0 == 0.0D) {
            d0 = 4.0D;
        }

        d0 *= 64.0D;
        return pDistance < d0 * d0;
    }

    private void causeDamage(DamageSource p_31649_, float p_31650_) {
        float f = getHealth();
        f -= p_31650_;
        if (f <= 0.5F) {
            brokenByAnything(p_31649_);
            kill();
        } else {
            setHealth(f);
            gameEvent(GameEvent.ENTITY_DAMAGED, p_31649_.getEntity());
        }

    }

    private void brokenByAnything(DamageSource p_31654_) {
        playBrokenSound();
        dropAllDeathLoot(p_31654_);

        for(int i = 0; i < handItems.size(); ++i) {
            ItemStack itemstack = handItems.get(i);
            if (!itemstack.isEmpty()) {
                Block.popResource(level, blockPosition().above(), itemstack);
                handItems.set(i, ItemStack.EMPTY);
            }
        }

        for(int j = 0; j < armorItems.size(); ++j) {
            ItemStack itemstack1 = armorItems.get(j);
            if (!itemstack1.isEmpty()) {
                Block.popResource(level, blockPosition().above(), itemstack1);
                armorItems.set(j, ItemStack.EMPTY);
            }
        }

    }

    private void playBrokenSound() {
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.ARMOR_STAND_BREAK, getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    protected float tickHeadTurn(float p_31644_, float p_31645_) {
        yBodyRotO = yRotO;
        yBodyRot = getYRot();
        return 0.0F;
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * (isBaby() ? 0.5F : 0.9F);
    }

    @Override
    public double getMyRidingOffset() {
        return isMarker() ? 0.0D : (double)0.1F;
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        if (hasPhysics()) {
            super.travel(pTravelVector);
        }
    }

    @Override
    public void setYBodyRot(float pOffset) {
        yBodyRotO = yRotO = pOffset;
        yHeadRotO = yHeadRot = pOffset;
    }

    @Override
    public void setYHeadRot(float pRotation) {
        yBodyRotO = yRotO = pRotation;
        yHeadRotO = yHeadRot = pRotation;
    }

    @Override
    public void tick() {
        super.tick();
        Rotations rotations = entityData.get(DATA_HEAD_POSE);
        if (!headPose.equals(rotations)) {
            setHeadPose(rotations);
        }

        Rotations rotations1 = entityData.get(DATA_BODY_POSE);
        if (!bodyPose.equals(rotations1)) {
            setBodyPose(rotations1);
        }

        Rotations rotations2 = entityData.get(DATA_LEFT_ARM_POSE);
        if (!leftArmPose.equals(rotations2)) {
            setLeftArmPose(rotations2);
        }

        Rotations rotations3 = entityData.get(DATA_RIGHT_ARM_POSE);
        if (!rightArmPose.equals(rotations3)) {
            setRightArmPose(rotations3);
        }

        Rotations rotations4 = entityData.get(DATA_LEFT_LEG_POSE);
        if (!leftLegPose.equals(rotations4)) {
            setLeftLegPose(rotations4);
        }

        Rotations rotations5 = entityData.get(DATA_RIGHT_LEG_POSE);
        if (!rightLegPose.equals(rotations5)) {
            setRightLegPose(rotations5);
        }

    }

    @Override
    protected void updateInvisibilityStatus() {
        setInvisible(invisible);
    }

    @Override
    public void setInvisible(boolean pInvisible) {
        invisible = pInvisible;
        super.setInvisible(pInvisible);
    }

    @Override
    public boolean isBaby() {
        return isSmall();
    }

    @Override
    public void kill() {
        remove(Entity.RemovalReason.KILLED);
    }

    @Override
    public boolean ignoreExplosion() {
        return isInvisible();
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return isMarker() ? PushReaction.IGNORE : super.getPistonPushReaction();
    }

    private void setSmall(boolean pSmall) {
        entityData.set(DATA_CLIENT_FLAGS, setBit(entityData.get(DATA_CLIENT_FLAGS), CLIENT_FLAG_SMALL, pSmall));
    }

    public boolean isSmall() {
        return (entityData.get(DATA_CLIENT_FLAGS) & CLIENT_FLAG_SMALL) != 0;
    }

    public boolean isShowArms() {
        return (entityData.get(DATA_CLIENT_FLAGS) & 4) != 0;
    }

    private void setNoBasePlate(boolean pNoBasePlate) {
        entityData.set(DATA_CLIENT_FLAGS, setBit(entityData.get(DATA_CLIENT_FLAGS), CLIENT_FLAG_NO_BASEPLATE, pNoBasePlate));
    }

    public boolean isNoBasePlate() {
        return (entityData.get(DATA_CLIENT_FLAGS) & CLIENT_FLAG_NO_BASEPLATE) != 0;
    }

    /**
     * Marker defines where if true, the size is 0 and will not be rendered or intractable.
     */
    private void setMarker(boolean pMarker) {
        entityData.set(DATA_CLIENT_FLAGS, setBit(entityData.get(DATA_CLIENT_FLAGS), CLIENT_FLAG_MARKER, pMarker));
    }

    public boolean isMarker() {
        return (entityData.get(DATA_CLIENT_FLAGS) & CLIENT_FLAG_MARKER) != 0;
    }

    public void setHeadPose(Rotations pVec) {
        headPose = pVec;
        entityData.set(DATA_HEAD_POSE, pVec);
    }

    public void setBodyPose(Rotations pVec) {
        bodyPose = pVec;
        entityData.set(DATA_BODY_POSE, pVec);
    }

    public void setLeftArmPose(Rotations pVec) {
        leftArmPose = pVec;
        entityData.set(DATA_LEFT_ARM_POSE, pVec);
    }

    public void setRightArmPose(Rotations pVec) {
        rightArmPose = pVec;
        entityData.set(DATA_RIGHT_ARM_POSE, pVec);
    }

    public void setLeftLegPose(Rotations pVec) {
        leftLegPose = pVec;
        entityData.set(DATA_LEFT_LEG_POSE, pVec);
    }

    public void setRightLegPose(Rotations pVec) {
        rightLegPose = pVec;
        entityData.set(DATA_RIGHT_LEG_POSE, pVec);
    }

    public Rotations getHeadPose() {
        return headPose;
    }

    public Rotations getBodyPose() {
        return bodyPose;
    }

    public Rotations getLeftArmPose() {
        return leftArmPose;
    }

    public Rotations getRightArmPose() {
        return rightArmPose;
    }

    public Rotations getLeftLegPose() {
        return leftLegPose;
    }

    public Rotations getRightLegPose() {
        return rightLegPose;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !isMarker();
    }

    @Override
    public boolean skipAttackInteraction(Entity pEntity) {
        return pEntity instanceof Player player && !this.level.mayInteract(player, blockPosition());
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt pLightning) {

    }


    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_CLIENT_FLAGS.equals(pKey)) {
            refreshDimensions();
            blocksBuilding = !isMarker();
        }

        super.onSyncedDataUpdated(pKey);
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return getDimensionsMarker(isMarker());
    }

    private EntityDimensions getDimensionsMarker(boolean p_31684_) {
        if (p_31684_) {
            return MARKER_DIMENSIONS;
        } else {
            return isBaby() ? getType().getDimensions().scale(0.5F) : getType().getDimensions();
        }
    }

    @Override
    public Vec3 getLightProbePosition(float pPartialTicks) {
        if (isMarker()) {
            AABB aabb = getDimensionsMarker(false).makeBoundingBox(position());
            BlockPos blockpos = blockPosition();
            int i = Integer.MIN_VALUE;

            for(BlockPos blockpos1 : BlockPos.betweenClosed(new BlockPos(aabb.minX, aabb.minY, aabb.minZ), new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ))) {
                int j = Math.max(level.getBrightness(LightLayer.BLOCK, blockpos1), level.getBrightness(LightLayer.SKY, blockpos1));
                if (j == 15) {
                    return Vec3.atCenterOf(blockpos1);
                }

                if (j > i) {
                    i = j;
                    blockpos = blockpos1.immutable();
                }
            }

            return Vec3.atCenterOf(blockpos);
        } else {
            return super.getLightProbePosition(pPartialTicks);
        }
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return !isInvisible() && !isMarker();
    }
}