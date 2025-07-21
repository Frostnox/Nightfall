package frostnox.nightfall.action;

import com.mojang.math.Vector3d;
import frostnox.nightfall.item.ImpactSoundType;
import frostnox.nightfall.registry.forge.EffectsNF;
import frostnox.nightfall.registry.forge.SoundsNF;
import frostnox.nightfall.registry.ActionsNF;
import frostnox.nightfall.util.data.Vec3f;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class DamageTypeSource extends DamageSource {
    //Vanilla equivalents
    public static final DamageTypeSource IN_FIRE = new DamageTypeSource("inFire", DamageType.FIRE).fromBlock();
    public static final DamageTypeSource LIGHTNING_BOLT = new DamageTypeSource("lightningBolt", DamageType.ELECTRIC);
    public static final DamageTypeSource ON_FIRE = (new DamageTypeSource("onFire", DamageType.FIRE)).setDoT().setSound(() -> SoundEvents.PLAYER_HURT_ON_FIRE);
    public static final DamageTypeSource LAVA = (new DamageTypeSource("lava", DamageType.FIRE)).fromBlock();
    public static final DamageTypeSource HOT_FLOOR = (new DamageTypeSource("hotFloor", DamageType.FIRE)).fromBlock();
    public static final DamageTypeSource IN_WALL = (new DamageTypeSource("inWall", DamageType.ABSOLUTE)).fromBlock();
    public static final DamageTypeSource CRAMMING = (new DamageTypeSource("cramming", DamageType.ABSOLUTE)).fromBlock();
    public static final DamageTypeSource DROWN = (new DamageTypeSource("drown", DamageType.ABSOLUTE));
    public static final DamageTypeSource STARVE = (new DamageTypeSource("starve", DamageType.ABSOLUTE));
    public static final DamageTypeSource CACTUS = new DamageTypeSource("cactus", DamageType.PIERCING).fromBlock();
    public static final DamageTypeSource FALL = (new DamageTypeSource("fall", DamageType.ABSOLUTE));
    public static final DamageTypeSource FLY_INTO_WALL = (new DamageTypeSource("flyIntoWall", DamageType.ABSOLUTE));
    public static final DamageTypeSource OUT_OF_WORLD = (new DamageTypeSource("outOfWorld", DamageType.ABSOLUTE));
    public static final DamageTypeSource GENERIC = (new DamageTypeSource("generic", DamageType.ABSOLUTE));
    public static final DamageTypeSource MAGIC = (new DamageTypeSource("magic", DamageType.ABSOLUTE));
    public static final DamageTypeSource WITHER_DOT = (new DamageTypeSource("wither_dot", DamageType.WITHER)).setDoT();
    public static final DamageTypeSource ANVIL = new DamageTypeSource("anvil", DamageType.STRIKING);
    public static final DamageTypeSource FALLING_BLOCK = new DamageTypeSource("fallingBlock", DamageType.STRIKING);
    public static final DamageTypeSource DRAGON_BREATH = (new DamageTypeSource("dragonBreath", DamageType.ABSOLUTE)).fromBlock();
    public static final DamageTypeSource DRY_OUT = new DamageTypeSource("dryout", DamageType.ABSOLUTE).setDoT();
    public static final DamageTypeSource SWEET_BERRY_BUSH = new DamageTypeSource("sweetBerryBush", DamageType.PIERCING).fromBlock();
    public static final DamageTypeSource UPROOTED = new DamageTypeSource("uprooted", DamageType.ABSOLUTE);
    //New
    public static final DamageTypeSource PHYSICAL = new DamageTypeSource("physical", DamageType.STRIKING, DamageType.SLASHING, DamageType.PIERCING);
    public static final DamageTypeSource EXPLOSION = (DamageTypeSource) new DamageTypeSource("explosion", DamageType.STRIKING).setExplosion();
    public static final DamageTypeSource BLEEDING = new DamageTypeSource("bleeding", DamageType.ABSOLUTE).setDoT().setSound(SoundsNF.BLEEDING_HIT);
    public static final DamageTypeSource POISON = new DamageTypeSource("poison", DamageType.ABSOLUTE).setDoT().setSound(SoundsNF.POISON_HIT);

    public final DamageType[] types;
    private final Entity entity;
    private final Entity owner;
    private final @Nullable HitData hitData;
    private Supplier<Attack> attack = () -> ActionsNF.EMPTY.get();
    private boolean fromBlock = false, isDoT = false;
    private Supplier<SoundEvent> sound = () -> null;
    private @Nullable ImpactSoundType impactSoundType = null;
    private int stunDuration = 0;
    private @Nullable AttackEffect[] effects = null;

    public DamageTypeSource(String id) {
        this(id, null, null, (HitData) null, DamageType.ABSOLUTE);
    }

    public DamageTypeSource(String id, DamageType... types) {
        this(id, null, null, null, types);
    }

    public DamageTypeSource(String id, Entity entity, DamageType... types) {
        this(id, entity, null, null, types);
    }

    public DamageTypeSource(String id, Entity entity, HitData hitData, DamageType... types) {
        this(id, entity, null, hitData, types);
    }

    public DamageTypeSource(String id, Entity entity, Entity owner, DamageType... types) {
        this(id, entity, owner, null, types);
    }

    public DamageTypeSource(String id, Entity entity, Entity owner, HitData hitData, DamageType... types) {
        super(id);
        this.types = types;
        this.entity = entity;
        this.owner = owner;
        this.hitData = hitData;
    }

    public boolean hasEntity() {
        return entity != null;
    }

    public boolean hasOwner() {
        return owner != null;
    }

    public boolean hasHitCoords() {
        return hitData != null && hitData.x != Float.MAX_VALUE;
    }

    public boolean isFromBlock() {
        return fromBlock;
    }

    public boolean isDoT() {
        return isDoT;
    }

    public SoundEvent getSound() {
        return sound.get();
    }

    public @Nullable ImpactSoundType getImpactSoundType() {
        return impactSoundType;
    }

    public @Nullable AttackEffect[] getEffects() {
        return effects;
    }

    public Attack getAttack() {
        return attack.get();
    }

    public int getStunDuration() {
        if(stunDuration > 0) return stunDuration;
        else return getAttack().getStunDuration();
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    public Entity getOwner() {
        return owner;
    }

    public Vec3f getKnockbackVec() {
        return hitData != null ? hitData.force : Vec3f.ZERO;
    }

    public Vector3d getHitCoords() {
        return hitData != null ? new Vector3d(hitData.x, hitData.y, hitData.z) : new Vector3d(0, 0, 0);
    }

    public int getHitBoxIndex() {
        return hitData != null ? hitData.boxIndex : -1;
    }

    public boolean isOnlyType(DamageType type) {
        return types.length == 1 && types[0] == type;
    }

    public boolean isType(DamageType type) {
        for(DamageType t : types) if(t == type) return true;
        return false;
    }

    public DamageTypeSource fromBlock() {
        fromBlock = true;
        return this;
    }

    public DamageTypeSource setDoT() {
        isDoT = true;
        return this;
    }

    public DamageTypeSource setSound(Supplier<SoundEvent> sound) {
        this.sound = sound;
        return this;
    }

    public DamageTypeSource setImpactSoundType(ImpactSoundType type, Entity target) {
        impactSoundType = type;
        sound = impactSoundType.getImpactSound(target);
        return this;
    }

    public DamageTypeSource setStun(int stunDuration) {
        this.stunDuration = stunDuration;
        return this;
    }

    public DamageTypeSource setEffects(AttackEffect... effects) {
        this.effects = effects;
        return this;
    }

    public DamageTypeSource setAttack(Attack attack) {
        this.attack = () -> attack;
        return this;
    }

    public void tryArmorSoundConversion() {
        if(impactSoundType != null && impactSoundType.getArmorSound().get() != null) {
            sound = impactSoundType.getArmorSound();
        }
    }

    public static DamageTypeSource createFallingSource(DamageType type) {
        return new DamageTypeSource("fallingBlock", type);
    }

    public static DamageTypeSource createEntitySource(LivingEntity entity, DamageType... type) {
        return new DamageTypeSource(type[0].toString(), entity, type);
    }

    public static DamageTypeSource createEntitySource(LivingEntity entity, String name, DamageType... type) {
        return new DamageTypeSource(name, entity, type);
    }

    public static DamageTypeSource createExplosionSource(@Nullable LivingEntity pLivingEntity) {
        return (DamageTypeSource) (pLivingEntity != null ? new DamageTypeSource("explosion.entity", pLivingEntity, DamageType.STRIKING).setExplosion()
                : new DamageTypeSource("explosion", DamageType.STRIKING).setExplosion());
    }

    public static DamageTypeSource createPlayerSource(Player user, DamageType type, HitData hitData) {
        return new DamageTypeSource(type.toString(), user, hitData, type);
    }

    public static DamageTypeSource createAttackSource(LivingEntity user, Attack attack, HitData hitData) {
        return new DamageTypeSource(attack.getName(user), user, hitData, attack.getDamageTypes(user)).setAttack(attack);
    }

    public static DamageTypeSource createProjectileSource(Entity source, DamageType[] type, @Nullable Entity pIndirectEntity, HitData hitData) {
        return (DamageTypeSource) new DamageTypeSource("projectile", source, pIndirectEntity, hitData, type).setProjectile();
    }

    public static DamageTypeSource createProjectileAttackSource(Entity pSource, DamageType[] type, Attack attack, @Nullable Entity pIndirectEntity, HitData hitData) {
        return (DamageTypeSource) (new DamageTypeSource(type[0].toString(), pSource, pIndirectEntity, hitData, type).setAttack(attack)).setProjectile();
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity killedEntity) {
        if(hasEntity() && !hasOwner()) {
            ItemStack itemstack = this.entity instanceof LivingEntity ? ((LivingEntity)this.entity).getMainHandItem() : ItemStack.EMPTY;
            String s = "death.attack." + this.msgId;
            return !itemstack.isEmpty() && itemstack.hasCustomHoverName() ? new TranslatableComponent(s + ".item", killedEntity.getDisplayName(), this.entity.getDisplayName(), itemstack.getDisplayName()) : new TranslatableComponent(s, killedEntity.getDisplayName(), this.entity.getDisplayName());
        }
        else if(hasEntity() && hasOwner()) {
            Component itextcomponent = this.owner == null ? this.entity.getDisplayName() : this.owner.getDisplayName();
            ItemStack itemstack = this.owner instanceof LivingEntity ? ((LivingEntity)this.owner).getMainHandItem() : ItemStack.EMPTY;
            String s = "death.attack." + this.msgId;
            String s1 = s + ".item";
            return !itemstack.isEmpty() && itemstack.hasCustomHoverName() ? new TranslatableComponent(s1, killedEntity.getDisplayName(), itextcomponent, itemstack.getDisplayName()) : new TranslatableComponent(s, killedEntity.getDisplayName(), itextcomponent);
        }
        else {
            LivingEntity livingentity = killedEntity.getKillCredit();
            String s = "death.attack." + this.msgId;
            String s1 = s + ".player";
            return livingentity != null ? new TranslatableComponent(s1, killedEntity.getDisplayName(), livingentity.getDisplayName()) : new TranslatableComponent(s, killedEntity.getDisplayName());
        }
    }

    public static DamageTypeSource convertFromVanilla(DamageSource source) {
        switch(source.getMsgId()) {
            case "inFire": return IN_FIRE;
            case "lightningBolt": return LIGHTNING_BOLT;
            case "onFire": return ON_FIRE;
            case "lava": return LAVA;
            case "hotFloor": return HOT_FLOOR;
            case "inWall": return IN_WALL;
            case "cramming": return CRAMMING;
            case "drown": return DROWN;
            case "starve": return STARVE;
            case "cactus": return CACTUS;
            case "fall": return FALL;
            case "flyIntoWall": return FLY_INTO_WALL;
            case "outOfWorld": return OUT_OF_WORLD;
            case "generic": return GENERIC;
            case "magic": return MAGIC;
            case "wither": return WITHER_DOT;
            case "anvil": return ANVIL;
            case "fallingBlock": return FALLING_BLOCK;
            case "dragonBreath": return DRAGON_BREATH;
            case "dryout": return DRY_OUT;
            case "sweetBerryBush": return SWEET_BERRY_BUSH;
            case "sting": return new DamageTypeSource("sting", source.getEntity(), DamageType.PIERCING);
            case "mob": return new DamageTypeSource("mob", source.getEntity(), source.getDirectEntity(), DamageType.ABSOLUTE);
            case "player": {
                DamageType type = DamageType.STRIKING;
                ItemStack stack = ((Player) (source.getEntity())).getMainHandItem();
                if(stack.getItem() instanceof SwordItem) type = DamageType.SLASHING;
                else if(stack.getItem() instanceof TridentItem) type = DamageType.PIERCING;
                return new DamageTypeSource("player", source.getEntity(), type);
            }
            case "arrow": return new DamageTypeSource("arrow", source.getEntity(), source.getDirectEntity(), DamageType.PIERCING);
            case "trident": return new DamageTypeSource("trident", source.getEntity(), source.getDirectEntity(), DamageType.PIERCING);
            case "fireworks": return new DamageTypeSource("fireworks", source.getEntity(), source.getDirectEntity(), DamageType.STRIKING);
            case "fireball": return new DamageTypeSource("fireball", source.getEntity(), source.getDirectEntity(), DamageType.FIRE);
            case "witherSkull": return new DamageTypeSource("witherSkull", source.getEntity(), source.getDirectEntity(), DamageType.WITHER);
            case "thrown": return new DamageTypeSource("thrown", source.getEntity(), source.getDirectEntity(), DamageType.ABSOLUTE);
            case "indirectMagic": return new DamageTypeSource("indirectMagic", source.getEntity(), source.getDirectEntity(), DamageType.ABSOLUTE);
            case "thorns": return new DamageTypeSource("thorns", source.getEntity(), source.getDirectEntity(), DamageType.ABSOLUTE);
            case "explosion.player":
            case "explosion": return EXPLOSION;
            default: return PHYSICAL;
        }
    }
}
