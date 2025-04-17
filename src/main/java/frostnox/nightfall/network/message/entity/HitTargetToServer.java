package frostnox.nightfall.network.message.entity;

import frostnox.nightfall.Nightfall;
import frostnox.nightfall.action.Attack;
import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.DamageTypeSource;
import frostnox.nightfall.action.HitData;
import frostnox.nightfall.capability.ActionTracker;
import frostnox.nightfall.capability.IActionTracker;
import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.capability.PlayerData;
import frostnox.nightfall.entity.IOrientedHitBoxes;
import frostnox.nightfall.entity.entity.ArmorStandDummyEntity;
import frostnox.nightfall.item.IActionableItem;
import frostnox.nightfall.network.NetworkHandler;

import frostnox.nightfall.network.message.GenericEntityToClient;
import frostnox.nightfall.network.message.capability.ActionTrackerToClient;
import frostnox.nightfall.registry.forge.AttributesNF;
import frostnox.nightfall.util.data.Vec3f;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HitTargetToServer {
    private boolean isValid;
    private int entityID, boxIndex;
    private float x, y, z;
    private Vec3f force;

    public HitTargetToServer(HitData hitData) {
        this(hitData.hitEntity.getId(), hitData.x, hitData.y, hitData.z, hitData.force, hitData.boxIndex);
    }

    public HitTargetToServer(int entityID, float x, float y, float z, Vec3f force, int boxIndex) {
        this.entityID = entityID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.force = force;
        this.boxIndex = boxIndex;
        this.isValid = true;
    }

    private HitTargetToServer() {
        isValid = false;
    }

    public void write(FriendlyByteBuf b) {
        if(isValid) {
            b.writeInt(entityID);
            b.writeFloat(x);
            b.writeFloat(y);
            b.writeFloat(z);
            b.writeFloat(force.x());
            b.writeFloat(force.y());
            b.writeFloat(force.z());
            b.writeVarInt(boxIndex);
        }
    }

    public static HitTargetToServer read(FriendlyByteBuf b) {
        HitTargetToServer msg = new HitTargetToServer();
        msg.entityID = b.readInt();
        msg.x = b.readFloat();
        msg.y = b.readFloat();
        msg.z = b.readFloat();
        msg.force = new Vec3f(b.readFloat(), b.readFloat(), b.readFloat());
        msg.boxIndex = b.readVarInt();
        msg.isValid = true;
        return msg;
    }

    public static void handle(HitTargetToServer msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        //Handle by side
        if(sideReceived.isClient()) {
            Nightfall.LOGGER.warn("DamageToServer received on client.");
        }
        else if(sideReceived.isServer()) {
            ServerPlayer player = ctx.getSender();
            if(player != null) {
                ctx.enqueueWork(() -> doWork(msg, player));
            }
            else Nightfall.LOGGER.warn("ServerPlayer is null.");
        }
    }

    private static void doWork(HitTargetToServer msg, ServerPlayer player) {
        if(!player.isAlive()) return;
        IActionTracker capA = ActionTracker.get(player);
        if(capA.isDamaging()) {
            IPlayerData capP = PlayerData.get(player);
            InteractionHand hand = capP.getActiveHand();
            ItemStack stack = player.getItemInHand(capP.getActiveHand());
            if(stack.isEmpty()) {
                Nightfall.LOGGER.warn("Player {} tried to attack but has no item in hand.", player.getName().getString());
                NetworkHandler.toClient(player, new DamageFailToClient(player.getId(), msg.entityID));
                return;
            }
            if(!(stack.getItem() instanceof IActionableItem item)) {
                Nightfall.LOGGER.warn("Player {} tried to attack but is not carrying an actionable item.", player.getName().getString());
                NetworkHandler.toClient(player, new DamageFailToClient(player.getId(), msg.entityID));
                return;
            }
            if(!item.hasAction(capA.getActionID(), player)) {
                Nightfall.LOGGER.warn("Player {} tried to attack with {} but it is not allowed by {}.", player.getName().getString(), capA.getActionID(), item.toString());
                NetworkHandler.toClient(player, new DamageFailToClient(player.getId(), msg.entityID));
                return;
            }
            Attack attack = (Attack) capA.getAction();
            int amountHit = 0;
            if(capA.getLivingEntitiesHit() >= attack.getMaxTargets()) {
                return;
            }
            Entity target = player.level.getEntity(msg.entityID);
            if(target != null) {
                if(target instanceof ItemEntity || target instanceof ExperienceOrb || target instanceof AbstractArrow || target == player) {
                    player.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_entity_attacked"));
                    Nightfall.LOGGER.warn("Player {} tried to attack an invalid entity", player.getName().getString());
                    return;
                }
                if(!capA.getHitEntities().contains(target.getId())) {
                    AABB box = target instanceof IOrientedHitBoxes hitBoxesEntity ? hitBoxesEntity.getEnclosingAABB() : target.getBoundingBox();
                    if(box.move(-target.getX(), -target.getY(), -target.getZ()).inflate(0.01).contains(msg.x, msg.y, msg.z)) {
                        //Distance is from player's eye position to the closest point of the target's bounding box
                        Vec3 t = target.getBoundingBox().getCenter();
                        Vec3 p = player.getEyePosition(1);
                        double x = Math.max(Math.abs(p.x - t.x) - target.getBoundingBox().getXsize() / 2D, 0);
                        double y = Math.max(Math.abs(p.y - t.y) - target.getBoundingBox().getYsize() / 2D, 0);
                        double z = Math.max(Math.abs(p.z - t.z) - target.getBoundingBox().getZsize() / 2D, 0);
                        double dist = x * x + y * y + z * z;
                        //Reach is lower than in vanilla (6 blocks is vanilla limit) so 5 blocks should do well
                        if(dist < 25.0D) {
                            if(target instanceof LivingEntity livingEntity) {
                                if(livingEntity.isDeadOrDying()) return;
                                livingEntity.setLastHurtByMob(player);
                                capA.setLivingEntitiesHit(capA.getLivingEntitiesHit() + 1);
                            }
                            if(capA.getHitEntities().isEmpty()) {
                                capA.setHitPause(0F);
                                NetworkHandler.toAllTracking(player, new GenericEntityToClient(NetworkHandler.Type.HIT_PAUSE_CLIENT, player.getId()));
                            }
                            amountHit++;
                            capA.getHitEntities().add(msg.entityID);
                            DamageTypeSource source = DamageTypeSource.createAttackSource(player, attack,
                                    new HitData(target, msg.x, msg.y, msg.z, msg.force.normalize(), msg.boxIndex));
                            if(capP.getTicksSinceHit() > 1) {
                                source.setImpactSoundType(attack.getImpactSoundType(player), target);
                                capP.setTicksSinceHit(0);
                            }
                            target.hurt(source, (attack.getDamage(player) * capA.getChargeAttackMultiplier()));
                            if(!player.isCreative()) {
                                player.getItemInHand(hand).hurtAndBreak(amountHit, player, (p_219998_1_) -> {
                                    p_219998_1_.broadcastBreakEvent(hand);
                                });
                            }
                        }
                    }
                }
            }
        }
        else {
            if(!capA.isInactive()) NetworkHandler.toClient(player, new DamageFailToClient(player.getId(), msg.entityID));
            else {
                Entity target = player.level.getEntity(msg.entityID);
                if(target != null) {
                    if(target instanceof ItemEntity || target instanceof ExperienceOrb || target instanceof AbstractArrow || target == player) {
                        player.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_entity_attacked"));
                        Nightfall.LOGGER.warn("Player {} tried to attack an invalid entity", player.getName().getString());
                        return;
                    }
                    if(target.isAttackable()) {
                        AABB box = target instanceof IOrientedHitBoxes hitBoxesEntity ? hitBoxesEntity.getEnclosingAABB() : target.getBoundingBox();
                        if(box.move(-target.getX(), -target.getY(), -target.getZ()).inflate(0.01).contains(msg.x, msg.y, msg.z)) {
                            //Distance is from player's eye position to the closest point of the target's bounding box
                            Vec3 t = target.getBoundingBox().getCenter();
                            Vec3 p = player.getEyePosition(1);
                            double x = Math.max(Math.abs(p.x - t.x) - target.getBoundingBox().getXsize() / 2D, 0);
                            double y = Math.max(Math.abs(p.y - t.y) - target.getBoundingBox().getYsize() / 2D, 0);
                            double z = Math.max(Math.abs(p.z - t.z) - target.getBoundingBox().getZsize() / 2D, 0);
                            double dist = x * x + y * y + z * z;
                            //Reach is lower than in vanilla (6 blocks is vanilla limit) so 5 blocks should do well
                            if(dist < 25.0D) {
                                if(target instanceof LivingEntity livingEntity) {
                                    if(livingEntity.isDeadOrDying()) return;
                                    livingEntity.setLastHurtByMob(player);
                                }
                                player.setDeltaMovement(player.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                                player.setSprinting(false);
                                boolean notLiving = (!(target instanceof LivingEntity)) || target instanceof ArmorStandDummyEntity || target instanceof ArmorStand;
                                if(!target.skipAttackInteraction(player) && (notLiving || PlayerData.get(player).getPunchTicks() <= 0)) {
                                    PlayerData.get(player).setPunchTicks(8);
                                    DamageSource source = notLiving ? DamageTypeSource.playerAttack(player) : DamageTypeSource.createPlayerSource(player, DamageType.STRIKING,
                                            new HitData(target, msg.x, msg.y, msg.z, msg.force.normalize(), msg.boxIndex)).setSound(() -> SoundEvents.PLAYER_ATTACK_WEAK);
                                    target.hurt(source, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * AttributesNF.getStrengthMultiplier(player));
                                    if(!player.isCreative()) {
                                        player.getMainHandItem().hurtAndBreak(1, player, (p_219998_1_) -> {
                                            p_219998_1_.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(!capA.getAction().isStateDamaging(capA.getState() + 1)) NetworkHandler.toClient(player, new ActionTrackerToClient(capA.writeNBT(), player.getId()));
        }
    }
}
