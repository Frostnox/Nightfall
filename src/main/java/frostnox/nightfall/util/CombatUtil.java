package frostnox.nightfall.util;

import com.mojang.math.Vector3d;
import frostnox.nightfall.action.Action;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.Poise;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.entity.entity.ActionableEntity;
import frostnox.nightfall.entity.entity.ArmorStandDummyEntity;
import frostnox.nightfall.item.item.TieredArmorItem;
import frostnox.nightfall.registry.forge.AttributesNF;

import frostnox.nightfall.util.animation.AnimationCalculator;
import frostnox.nightfall.util.data.Vec3f;
import frostnox.nightfall.util.data.Wrapper;
import frostnox.nightfall.util.math.Easing;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CombatUtil {
    public static final UUID BLOCK_SLOW_ID = UUID.fromString("c59cdce7-9656-4f59-add3-09be735e32b3");
    public static final UUID BLOCK_POISE_ID = UUID.fromString("3f84ae45-448a-488c-9815-8475a621cb47");
    public static final UUID STUN_SLOW_ID = UUID.fromString("06dbd56c-1fe0-4183-81a2-15fada3c0f8b");
    public static final UUID ACTION_SLOW_ID = UUID.fromString("cecd39e8-0308-4403-ac3c-faa9463acf9f");
    public static final UUID ACTION_SPEED_ID = UUID.fromString("f543633f-f272-4504-9940-a2fe3aee3698");
    public static final int STUN_SHORT = 8;
    public static final int STUN_MEDIUM = 12;
    public static final int STUN_LONG = 16;
    public static final int STUN_VERY_LONG = 20;
    public static final int STUN_MAX = 24;
    public static final double DODGE_STAMINA_COST = -25.0D;
    public static final int DODGE_PENALTY_TICK = 10;

    public static float getRelativeHorizontalAngle(Vec3 startVec, Vec3 endVec, float userYaw) {
        return getRelativeHorizontalAngle(endVec.x - startVec.x, endVec.z - startVec.z, userYaw);
    }

    public static float getRelativeHorizontalAngle(double dX, double dZ, float userYaw) {
        float angle = MathUtil.getAngleDegrees(dZ, dX) - (userYaw % 360);
        if(angle > 180F) angle -= 360F;
        else if(angle < -180F) angle += 360F;
        return angle;
    }

    public static void addMovementTowardsTarget(double max, double scale, Mob entity) {
        LivingEntity target = entity.getTarget();
        double dist;
        if(target != null) {
            AABB box1 = entity.getBoundingBox();
            AABB box2 = target.getBoundingBox();
            double x1 = Math.max(0, box1.minX - box2.maxX);
            double z1 = Math.max(0, box1.minZ - box2.maxZ);
            double x2 = Math.max(0, box2.minX - box1.maxX);
            double z2 = Math.max(0, box2.minZ - box1.maxZ);
            dist = Math.sqrt(x1 * x1 + z1 * z1 + x2 * x2 + z2 * z2);
        }
        else dist = 0;
        addFacingMovement(Math.min(max, dist * scale), entity);
    }

    public static void addFacingMovement(double magnitude, LivingEntity entity) {
        Vec3 vec = entity.getDeltaMovement();
        float cos = Mth.cos(MathUtil.toRadians(entity.getYHeadRot()));
        float sin = Mth.sin(MathUtil.toRadians(entity.getYHeadRot()));
        double xMove = -magnitude * sin;
        double zMove = magnitude * cos;
        double scalar = entity.isInWaterOrBubble() ? 0.5 : 1;
        entity.setDeltaMovement(vec.x() + xMove * scalar, vec.y(), vec.z() + zMove * scalar);
    }

    /**
     * @return shortest distance from attacker's eye position (modeled as circle) to target's bounding box
     */
    public static double getShortestDistanceSqr(Entity attacker, Entity target) {
        AABB box = target.getBoundingBox();
        double x = attacker.getX(), y = attacker.getEyeY(), z = attacker.getZ();
        float radius = attacker.getBbWidth() / 2, radiusSqr = radius * radius;
        double bestX = Mth.clamp(x, box.minX, box.maxX), bestZ = Mth.clamp(z, box.minZ, box.maxZ);
        double dX = x - bestX, dZ = z - bestZ;
        double xzDistSqr = dX * dX + dZ * dZ;
        double xzDistAdjusted = xzDistSqr <= radiusSqr ? 0 : (Math.sqrt(xzDistSqr) - radius);
        double yDist;
        if(y < box.minY) yDist = box.minY - y;
        else if(y > box.maxY) yDist = y - box.maxY;
        else yDist = 0.0;
        return xzDistAdjusted * xzDistAdjusted + yDist * yDist;
    }

    public static void damageAllInRadius(Entity source, Vec3 center, double radius, float damage, float knockback, float blockedDamageMultiplier, DamageSource damageSource) {
        boolean notExplosion = !damageSource.isExplosion();
        for(Entity entity : source.level.getEntities(source, new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius))) {
            if(notExplosion|| !entity.ignoreExplosion()) {
                Vector3d hitPoint = MathUtil.getShortestPointFromPointToBox(center.x, center.y, center.z, entity.getBoundingBox());
                double dX = hitPoint.x - center.x;
                double dY = hitPoint.y - center.y;
                double dZ = hitPoint.z - center.z;
                double dist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
                if(dist <= radius) {
                    dX /= dist;
                    dY /= dist;
                    dZ /= dist;
                    float modifier = Easing.outCubic.apply((float) (1.0D - dist / radius));
                    float adjustedDamage = damage * modifier;
                    if(source.level.clip(new ClipContext(center, new Vec3(hitPoint.x, hitPoint.y, hitPoint.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getType() == HitResult.Type.BLOCK) {
                        adjustedDamage *= blockedDamageMultiplier;
                    }
                    entity.hurt(damageSource, adjustedDamage);
                    float adjustedKnockback = knockback * modifier;
                    entity.push(dX * adjustedKnockback, dY * adjustedKnockback, dZ * adjustedKnockback);
                }
            }
        }
    }

    public static void alignBodyRotWithHead(LivingEntity entity, IActionTracker capA) {
        if(capA.getState() == 0) {
            Action action = capA.getAction();
            AnimationCalculator calc = new AnimationCalculator(capA.isCharging() ? action.getRequiredCharge(entity) : capA.getDuration(), capA.getFrame(), 1F, Easing.inOutSine);
            float f = 0;
            if (Math.abs(entity.yBodyRotO % 360 - (entity.yHeadRot % 360)) > 180) {
                f = entity.yBodyRotO % 360 < entity.yHeadRot % 360 ? 360 : -360;
            }
            calc.setVectors(entity.yBodyRotO % 360 + f, 0, 0, entity.yHeadRot % 360, 0, 0);
            entity.yBodyRot = calc.getTransformations().x();
        }
        else if(capA.getState() < capA.getAction().getTotalStates() - 1) {
            entity.yBodyRot = entity.yHeadRot;
        }
    }

    public static void alignBodyRotWithHead(LivingEntity entity, IPlayerData capP) {
        AnimationCalculator calc = new AnimationCalculator(3, capP.getClimbTicks(), 1F, Easing.inOutSine);
        float f = 0;
        if (Math.abs(entity.yBodyRotO % 360 - (entity.yHeadRot % 360)) > 180) {
            f = entity.yBodyRotO % 360 < entity.yHeadRot % 360 ? 360 : -360;
        }
        calc.setVectors(entity.yBodyRotO % 360 + f, 0, 0, entity.yHeadRot % 360, 0, 0);
        entity.yBodyRot = calc.getTransformations().x();
    }

    public static void addTransientMultiplier(LivingEntity entity, AttributeInstance attribute, double amount, UUID id, String name) {
        addTransientModifier(entity, attribute, amount, id, name, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public static void addTransientModifier(LivingEntity entity, AttributeInstance attribute, double amount, UUID id, String name, AttributeModifier.Operation op) {
        if(!entity.level.isClientSide()) {
            if(attribute != null) {
                if(attribute.getModifier(id) != null) {
                    attribute.removeModifier(id);
                }
                attribute.addTransientModifier(new AttributeModifier(id, name, amount, op));
            }
        }
    }

    public static void removeTransientModifier(LivingEntity entity, AttributeInstance attribute, UUID id) {
        if(!entity.level.isClientSide()) {
            if(attribute != null) {
                if(attribute.getModifier(id) != null) {
                    attribute.removeModifier(id);
                }
            }
        }
    }

    public static List<Entity> getAttackableEntitiesNearby(AABB box, LivingEntity user, Level world) {
        List<Entity> list = world.getEntities(user, box);
        if(user instanceof Player) return list.stream().filter(e -> ((e instanceof HangingEntity || e instanceof Boat ||
                (e instanceof LivingEntity le && !le.isDeadOrDying() && !(e instanceof ArmorStandDummyEntity)) || e instanceof Fireball ||
                e instanceof ShulkerBullet) && !e.hasPassenger(user))).collect(Collectors.toCollection(ArrayList::new));
        else return list.stream().filter(e -> ((e instanceof Boat || (e instanceof LivingEntity le && !le.isDeadOrDying())) &&
                !e.hasPassenger(user))).collect(Collectors.toCollection(ArrayList::new));
    }

    public static float applyDamageReduction(float damage, float defense) {
        return Math.max(0, damage * (1F - defense));
    }

    public static float getArmorDefenseDurabilityPenalty(float durability, float maxDurability) {
        float halfDurability = maxDurability / 2;
        return durability > halfDurability ? 1 : (1F - (0.5F - (durability / halfDurability * 0.5F)));
    }

    public static float applyArmorDamageReduction(float damage, float durability, float maxDurability, float defense) {
        return Math.max(0, damage * (1F - defense) * getArmorDefenseDurabilityPenalty(durability, maxDurability));
    }

    //TODO: Implement this
    public static float applyAccessoryDamageCalculations(LivingEntity hitEntity, DamageTypeSource source, float damage) {
        return damage;
    }

    public static float applyAttributeDamageCalculations(LivingEntity hitEntity, DamageTypeSource source, float damage) {
        if(!source.isDoT()) {
            if(hitEntity.getAttribute(AttributesNF.STRIKING_DEFENSE.get()) != null) {
                float defense = 0F;
                for(DamageType type : source.types) {
                    switch(type) {
                        case STRIKING -> defense += (float) hitEntity.getAttribute(AttributesNF.STRIKING_DEFENSE.get()).getValue();
                        case SLASHING -> defense += (float) hitEntity.getAttribute(AttributesNF.SLASHING_DEFENSE.get()).getValue();
                        case PIERCING -> defense += (float) hitEntity.getAttribute(AttributesNF.PIERCING_DEFENSE.get()).getValue();
                        case FIRE -> defense += (float) hitEntity.getAttribute(AttributesNF.FIRE_DEFENSE.get()).getValue();
                        case FROST -> defense += (float) hitEntity.getAttribute(AttributesNF.FROST_DEFENSE.get()).getValue();
                        case ELECTRIC -> defense += (float) hitEntity.getAttribute(AttributesNF.ELECTRIC_DEFENSE.get()).getValue();
                        case WITHER -> defense += (float) hitEntity.getAttribute(AttributesNF.WITHER_DEFENSE.get()).getValue();
                    }
                }
                defense /= source.types.length;
                return applyDamageReduction(damage, defense);
            }
        }
        return damage;
    }

    public static float applyBodyDamageCalculations(LivingEntity hitEntity, DamageTypeSource source, float damage, Wrapper<Poise> poise) {
        //Armor
        if(!source.isDoT()) {
            if(hitEntity instanceof Player player) {
                //Pick body part based on y collision
                if(source.hasHitCoords() && !player.isVisuallyCrawling()) {
                    float durabilityDmg = Math.max(1, damage / 5F);
                    int armorIndex = 0;
                    double y = source.getHitCoords().y;
                    //Armor indexes: 0 - feet, 1 - legs, 2 - chest, 3 - head
                    if(!player.isCrouching()) {
                        if(y > 5D/16D * 0.9375D && y <= 12D/16D * 0.9375D) armorIndex = 1;
                        else if(y > 12D/16D * 0.9375D && y <= 24D/16D * 0.9375D) armorIndex = 2;
                        else if(y > 24D/16D * 0.9375F) armorIndex = 3;
                    }
                    else {
                        if(y > 4D/16D * 0.9375D && y <= 10D/16D * 0.9375D) armorIndex = 1;
                        else if(y > 10D/16D * 0.9375D && y <= 18D/16D * 0.9375D) armorIndex = 2;
                        else if(y > 18D/16D * 0.9375D) armorIndex = 3;
                    }
                    if(armorIndex == 3) damage *= 1.2F; //Damage multiplier for head
                    ItemStack stack = player.getInventory().armor.get(armorIndex);
                    if(stack.getItem() instanceof TieredArmorItem armor) {
                        if(poise.val.ordinal() < armor.material.getPoise().ordinal()) poise.val = armor.material.getPoise();
                        if(!source.isOnlyType(DamageType.ABSOLUTE)) {
                            if(armor.material.isMetal()) source.tryArmorSoundConversion();
                            damage = armor.material.getFinalDamage(armor.slot, source.types, stack.getMaxDamage() - stack.getDamageValue(), damage, false);
                            int index = armorIndex;
                            stack.hurtAndBreak((int)durabilityDmg, hitEntity, (p_214023_1_) -> {
                                p_214023_1_.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, index));
                            });
                        }
                    }
                }
                //Take weighted averages of slots if source had no location
                else if(!source.isOnlyType(DamageType.ABSOLUTE)) {
                    float durabilityDmg = Math.max(1, damage / 20F);
                    boolean isMetal = false;
                    int totalPoise = 0;
                    for(int i = 0; i < player.getInventory().armor.size(); i++) {
                        ItemStack stack = player.getInventory().armor.get(i);
                        if(stack.getItem() instanceof TieredArmorItem armor) {
                            totalPoise += armor.material.getPoise().ordinal();
                            if(!isMetal && armor.material.isMetal()) isMetal = true;
                            damage = armor.material.getFinalDamage(armor.slot, source.types, stack.getDamageValue(), damage, true);
                            int index = i;
                            stack.hurtAndBreak((int)durabilityDmg, hitEntity, (p_214023_1_) -> {
                                p_214023_1_.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, index));}
                            );
                        }
                    }
                    totalPoise /= 4;
                    if(poise.val.ordinal() < totalPoise) poise.val = Poise.values()[totalPoise];
                    if(isMetal) source.tryArmorSoundConversion();
                }
            }
            else if(hitEntity instanceof ActionableEntity entity) {
                damage = entity.modifyIncomingDamage(source, damage, poise);
            }
        }
        return damage;
    }

    public static float applyEffectDamageCalculations(LivingEntity e, DamageTypeSource source, float damage) {
        if(source.isDoT()) return damage;
        else {
            if (e.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                int i = (e.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = damage * (float) j;
                float f1 = damage;
                damage = Math.max(f / 25.0F, 0.0F);
                float f2 = f1 - damage;
                if (f2 > 0.0F && f2 < 3.4028235E37F) {
                    if (e instanceof ServerPlayer) {
                        ((ServerPlayer) e).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
                    } else if (source.getDirectEntity() instanceof ServerPlayer) {
                        ((ServerPlayer) source.getDirectEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
                    }
                }
            }

            if (damage <= 0.0F) {
                return 0.0F;
            } else {
                int k = EnchantmentHelper.getDamageProtection(e.getArmorSlots(), source);
                if (k > 0) {
                    damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) k);
                }

                return damage;
            }
        }
    }

    public static void knockbackEntity(Entity target, Vec3f knockbackVec) {
        if(knockbackVec.lengthSqr() > 0D) {
            if(target.isOnGround()) target.push(knockbackVec.x(), knockbackVec.y(), knockbackVec.z());
            else target.push(knockbackVec.x() * 1.3F, knockbackVec.y(), knockbackVec.z() * 1.3F);
            target.hurtMarked = true; //Have to mark target or the server will not sync movement
        }
    }

    /**
     * Applies knockback to target relative to attacker
     */
    public static void knockbackEntity(Entity target, Entity attacker, double magnitude) {
        if(magnitude > 0D) {
            double xRatio = attacker.getX() - target.getX();
            double zRatio;
            for(zRatio = attacker.getZ() - target.getZ(); xRatio * xRatio + zRatio * zRatio < 1.0E-4D; zRatio = (Math.random() - Math.random()) * 0.01D) {
                xRatio = (Math.random() - Math.random()) * 0.01D;
            }
            Vec3 knockbackVec = new Vec3(xRatio, 0, zRatio).normalize().scale(-magnitude);

            if(target.isOnGround()) target.push(knockbackVec.x, 0.25, knockbackVec.z);
            else target.push(knockbackVec.x / 2D, 0, knockbackVec.z / 2D);
            target.hurtMarked = true; //Have to mark target or the server will not sync movement
        }
    }

    /*public List<AABB> getBlocksNearby(double radius, LivingEntity user, Level world) {
        List<AABB> list = new ArrayList<AABB>();
        AABB box = user.getBoundingBox().inflate(Math.ceil(radius), Math.ceil(radius), Math.ceil(radius));
        for (double y = Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
            for (double x = Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
                for (double z = Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).getMaterial().isSolid()) {
                        AABB bBox = world.getBlockState(pos).getShape(world, pos).getBoundingBox();
                        AABB trueBox = new AABB(x + bBox.minX, y + bBox.minY, z + bBox.minZ, x + bBox.maxX, y + bBox.maxY, z + bBox.maxZ);
                        if (getShortestDistanceFromEyes(trueBox, user) <= radius) list.add(trueBox);
                    }
                }
            }
        }
        return list;
    }*/

    public enum DodgeDirection {
        NORTH(1, 0),
        SOUTH(-1, 0),
        WEST(0, 1),
        EAST(0, -1),
        NORTHWEST(0.7071F, 0.7071F),
        NORTHEAST(0.7071F, -0.7071F),
        SOUTHWEST(-0.7071F, 0.7071F),
        SOUTHEAST(-0.7071F, -0.7071F);

        private final float x;
        private final float z;

        DodgeDirection(float x, float z) {
            this.x = x;
            this.z = z;
        }

        public float getXAmount() {
            return x;
        }
        public float getZAmount() {
            return z;
        }

        public static DodgeDirection get(int value) {
            return switch (value) {
                case 1 -> NORTHWEST;
                case 2 -> WEST;
                case 3 -> SOUTHWEST;
                case 4 -> SOUTH;
                case 5 -> SOUTHEAST;
                case 6 -> EAST;
                case 7 -> NORTHEAST;
                default -> NORTH;
            };
        }
    }
}
