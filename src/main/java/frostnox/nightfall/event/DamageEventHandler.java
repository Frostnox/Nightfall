package frostnox.nightfall.event;

import frostnox.nightfall.action.*;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.entity.ActionableEntity;

import frostnox.nightfall.entity.entity.ArmorStandDummyEntity;
import frostnox.nightfall.item.IWeaponItem;
import frostnox.nightfall.network.NetworkHandler;
import frostnox.nightfall.network.message.entity.HitParticlesToClient;

import frostnox.nightfall.network.message.entity.HitTargetToServer;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.util.CombatUtil;
import frostnox.nightfall.util.data.Vec2f;
import frostnox.nightfall.util.data.Vec3f;
import frostnox.nightfall.util.data.Wrapper;
import frostnox.nightfall.world.OrientedEntityHitResult;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class DamageEventHandler {
    @SubscribeEvent
    public static void onLivingDeathEvent(LivingDeathEvent event) {
        if(event.getEntityLiving().getCombatTracker().getKiller() instanceof ActionableEntity killer) {
            killer.lastTargetPos = null;
            killer.setTarget(null);
        }
    }

    @SubscribeEvent
    public static void onAttackEntityEvent(AttackEntityEvent event) {
        Player p = event.getPlayer();
        if(p != null && p.isAlive()) {
            Entity target = event.getTarget();
            boolean notLiving = (!(target instanceof LivingEntity)) || target instanceof ArmorStandDummyEntity || target instanceof ArmorStand;
            boolean usingWeapon = p.getItemInHand(PlayerData.get(p).getActiveHand()).getItem() instanceof IWeaponItem;
            if(p.level.isClientSide && !usingWeapon) {
                if(target.isAttackable()) {
                    if(!target.skipAttackInteraction(p) && (notLiving || PlayerData.get(p).getPunchTicks() <= 0)) {
                        PlayerData.get(p).setPunchTicks(8);
                        float damage = (float)p.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        if(damage > 0.0F) {
                            EntityHitResult hitResult = (EntityHitResult) Minecraft.getInstance().hitResult;
                            Vec3 hitPos = hitResult.getLocation();
                            int boxIndex = hitResult instanceof OrientedEntityHitResult orientedHitResult ? orientedHitResult.boxIndex : -1;
                            NetworkHandler.toServer(new HitTargetToServer(target.getId(), (float) (hitPos.x - target.getX()),
                                    (float) (hitPos.y - target.getY()), (float) (hitPos.z - target.getZ()), Vec3f.ZERO, boxIndex));
                        }
                    }
                }
            }
            event.setCanceled(!notLiving || usingWeapon);
        }
    }

    @SubscribeEvent
    public static void onLivingAttackEvent(LivingAttackEvent event) {
        if(event.getEntityLiving().invulnerableTime > 10) event.getEntityLiving().invulnerableTime = 10; //This can't be over 10 or vanilla hurt function starts mitigating damage
        float damageAmount = event.getAmount();
        DamageTypeSource source;
        if(event.getSource() instanceof DamageTypeSource) {
            source = (DamageTypeSource) event.getSource();
        }
        else source = DamageTypeSource.convertFromVanilla(event.getSource());
        if(source.isFromBlock()) {
            if((event.getEntity() instanceof ActionableEntity || event.getEntity() instanceof Player) && ActionTracker.get(event.getEntityLiving()).getBlockInvulnerableTime() > 0) {
                event.setCanceled(true);
                return;
            }
        }
        if(source.isDoT()) {
            if((event.getEntity() instanceof ActionableEntity || event.getEntity() instanceof Player) && ActionTracker.get(event.getEntityLiving()).getDotInvulnerableTime() > 0) {
                event.setCanceled(true);
                return;
            }
        }
        if(event.getEntityLiving() instanceof ServerPlayer player) {
            if(source != DamageSource.OUT_OF_WORLD && PlayerData.get(player).hasGodMode()) event.setCanceled(true);
            IActionTracker capA = ActionTracker.get(player);
            //Cancel attack completely if technique blocked all of it
            if(capA.getAction().onAttackReceived(player, source, damageAmount) <= 0) {
                float durabilityDmg = Math.max(1F, damageAmount / 20F);
                InteractionHand hand = PlayerData.get(player).getActiveHand();
                if(!player.isCreative()) player.getItemInHand(hand).hurtAndBreak((int) durabilityDmg, player, (p) -> p.broadcastBreakEvent(hand));
                if(source.isFromBlock()) capA.setBlockInvulnerableTime(10);
                //Reduced knockback
                CombatUtil.knockbackEntity(player, source.getKnockbackVec().scale(source.getAttack().getKnockback() / 2F));
                event.setCanceled(true);
            }
        }
        else if(event.getEntityLiving() instanceof ActionableEntity entity) {
            if(entity.isImmuneTo(source, damageAmount)) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurtEvent(LivingHurtEvent event) {
        float damageAmount = event.getAmount();
        if(damageAmount > 32768) return; //Below logic will cause issues with /kill command so just let vanilla logic run
        DamageTypeSource source;
        if(event.getSource() instanceof DamageTypeSource) {
            source = (DamageTypeSource) event.getSource();
            if(source.isExplosion()) damageAmount *= 1.5F * 5F;
        }
        else {
            damageAmount *= 5;
            source = DamageTypeSource.convertFromVanilla(event.getSource());
        }
        float originalAmount = damageAmount;
        DamageSource damageSrc = event.getSource();
        LivingEntity entity = event.getEntityLiving();
        //Pre damage
        if(source.hasOwner() && (source.getOwner() instanceof Player || source.getOwner() instanceof ActionableEntity) && ActionTracker.get(source.getOwner()).getAction() instanceof Attack attack) {
            damageAmount = attack.onDamageDealtPre((LivingEntity) source.getOwner(), entity, damageAmount);
        }
        //Technique calculations
        if(entity instanceof ServerPlayer player) {
            IActionTracker capA = ActionTracker.get(player);
            float durabilityDmg = Math.max(1, damageAmount / 20F);
            damageAmount = capA.getAction().onDamageReceived(player, source, damageAmount);
            if(damageAmount != originalAmount) {
                InteractionHand hand = PlayerData.get(player).getActiveHand();
                if(!player.isCreative()) {
                    player.getItemInHand(hand).hurtAndBreak((int) durabilityDmg, player, (p_219998_1_) -> {
                        p_219998_1_.broadcastBreakEvent(hand);
                    });
                }
            }
        }
        Wrapper<Poise> poise = new Wrapper<>(AttributesNF.getPoise(entity));
        //System.out.println("Damage: " + damageAmount);
        float oldDamageAmount = damageAmount;
        //New calculations
        damageAmount = CombatUtil.applyEffectDamageCalculations(entity, source, damageAmount);
        //System.out.println("Effects: " + damageAmount);
        damageAmount = CombatUtil.applyAttributeDamageCalculations(entity, source, damageAmount);
        //System.out.println("Attributes: " + damageAmount);
        damageAmount = CombatUtil.applyBodyDamageCalculations(entity, source, damageAmount, poise);
        //System.out.println("Body & Armor: " + damageAmount);
        damageAmount = CombatUtil.applyAccessoryDamageCalculations(entity ,source, damageAmount);
        //System.out.println("Accessories: " + damageAmount);
        //Post damage
        if(source.hasOwner() && (source.getOwner() instanceof Player || source.getOwner() instanceof ActionableEntity) && ActionTracker.get(source.getOwner()).getAction() instanceof Attack attack) {
            damageAmount = attack.onDamageDealtPost((LivingEntity) source.getOwner(), entity, damageAmount);
        }
        //Effects
        Impact impact = source.getImpact();
        if(entity instanceof ActionableEntity actionable) impact = actionable.modifyIncomingImpact(source, impact);
        boolean impacted = !impact.negatedBy(poise.val);
        float reducedDamage = Math.min(2F, damageAmount / originalAmount);
        float chargedModifier = (source.getOwner() instanceof Player || source.getOwner() instanceof ActionableEntity) ? ActionTracker.get(source.getOwner()).getChargeAttackMultiplier() : 1F;
        if(reducedDamage > 0.1F) {
            float chanceModifier = Math.min(1F, reducedDamage + 0.25F);
            if(source.getEffects() != null) {
                for(AttackEffect attackEffect : source.getEffects()) {
                    if(entity.level.getRandom().nextFloat() <= attackEffect.chance * chanceModifier) {
                        entity.addEffect(attackEffect.getEffect(), source.getEntity());
                    }
                }
            }
            for(AttackEffect attackEffect : source.getAttack().getEffects(source.getEntity() instanceof LivingEntity livingEntity ? livingEntity : null)) {
                if(entity.level.getRandom().nextFloat() <= attackEffect.chance * chanceModifier) {
                    entity.addEffect(attackEffect.getEffect(), source.getEntity());
                }
            }
        }
        //Knockback
        float magnitude = Math.max(source.getAttack().getKnockback() / 2F, source.getAttack().getKnockback() * (impacted ? 1F : 0.5F) * chargedModifier);
        CombatUtil.knockbackEntity(entity, source.getKnockbackVec().scale(magnitude));

        damageAmount = Math.max(damageAmount, oldDamageAmount * 0.1F); //Limit damage reduction to 90%
        float healthDamage = Math.max(damageAmount - entity.getAbsorptionAmount(), 0.0F);
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (damageAmount - healthDamage));
        float f = damageAmount - healthDamage;
        if (f > 0.0F && f < 3.4028235E37F && damageSrc.getDirectEntity() instanceof Player) {
            ((Player) damageSrc.getDirectEntity()).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
        }
        int stunDuration = source.getStunDuration();
        if(damageAmount > 0) {
            boolean doStun = true;
            if(healthDamage != 0) {
                float health = entity.getHealth();
                if(healthDamage >= health && healthDamage - health < 40 && entity instanceof ActionableEntity actionable && actionable.getCollapseAction() != null
                        && !actionable.getActionTracker().getActionID().equals(actionable.getCollapseAction())) {
                    doStun = false;
                    healthDamage = health - 1;
                    actionable.forceAction(actionable.getCollapseAction());
                }
                entity.getCombatTracker().recordDamage(damageSrc, health, healthDamage);
                entity.setHealth(health - healthDamage);
                if(damageSrc.getDirectEntity() instanceof Player) {
                    if(healthDamage < 3.4028235E37F) ((Player) damageSrc.getDirectEntity()).awardStat(Stats.DAMAGE_TAKEN, Math.round(healthDamage * 10.0F));
                }
            }
            if(doStun && impacted && stunDuration > 0 && (entity instanceof ActionableEntity || entity instanceof Player)) {
                IActionTracker capA = ActionTracker.get(entity);
                if(!capA.isStunned()) capA.stunServer(Math.round(stunDuration * chargedModifier), false);
            }
            float x, y, z;
            if(source.hasHitCoords()) {
                x = (float) source.getHitCoords().x;
                y = (float) source.getHitCoords().y;
                z = (float) source.getHitCoords().z;
            }
            else {
                x = (float) (entity.getBoundingBox().getXsize() * 0.5F);
                y = (float) (entity.getBoundingBox().getYsize() * 0.5F);
                z = (float) (entity.getBoundingBox().getZsize() * 0.5F);
            }
            //Damage at 10 filters out unarmed players and DoTs
            if(damageAmount > 10F) {
                Vec2f particleVec = new Vec2f(source.getKnockbackVec().x(), source.getKnockbackVec().z()).normalize().reverse();
                NetworkHandler.toAllTrackingAndSelf(entity, new HitParticlesToClient(entity.getId(), damageAmount, x, y, z, particleVec.x(), particleVec.y()));
            }
            x += entity.getX();
            y += entity.getY();
            z += entity.getZ();
            if(source.getSound() != null && (!source.isDoT() || entity instanceof Player)) { //Only play DoT sound for players
                entity.level.playSound(null, x, y, z, source.getSound(), entity.getSoundSource(), 1F, 1F + entity.level.random.nextFloat(-0.03F, 0.03F));
            }
            if(entity instanceof ActionableEntity actionableEntity && !source.isType(DamageType.ABSOLUTE) && (!source.isDoT() || (source.isType(DamageType.FIRE) && actionableEntity.panicsOnFireDamage()))) {
                actionableEntity.reactToDamage = true;
            }
        }
        if(entity instanceof ActionableEntity || entity instanceof Player) {
            IActionTracker capA = ActionTracker.get(entity);
            if(source.isFromBlock()) capA.setBlockInvulnerableTime(10);
        }
        if(entity.animationSpeed == 1.5F) entity.animationSpeed = entity.animationSpeedOld;
        entity.gameEvent(GameEvent.ENTITY_DAMAGED);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingKnockBackEvent(LivingKnockBackEvent event) {
        event.setStrength(0F);
    }

    @SubscribeEvent
    public static void onLivingHealEvent(LivingHealEvent event) {
        if(event.getEntityLiving().hasEffect(EffectsNF.BLEEDING.get())) {
            event.setAmount(event.getAmount() / 2F);
        }
        if(event.getEntityLiving() instanceof Player player) {
            float temp = PlayerData.get(player).getTemperature();
            if(temp < 0.25F) event.setAmount(event.getAmount() * Math.max(0, temp / 0.25F));
        }
    }
}
