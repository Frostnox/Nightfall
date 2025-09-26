package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.Sex;
import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.animal.TamableAnimalEntity;
import frostnox.nightfall.util.MathUtil;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class BreedGoal extends Goal {
   private static final TargetingConditions PARTNER_TARGETING = TargetingConditions.forNonCombat().range(15.0D).ignoreLineOfSight();
   protected final TamableAnimalEntity entity;
   protected final Level level;
   protected final double speedModifier;
   protected @Nullable TamableAnimalEntity partner;
   protected ReversePath path;
   protected long lastCanUseCheck;
   protected int breedTimer;

   public BreedGoal(TamableAnimalEntity entity, double speedModifier) {
      this.entity = entity;
      this.level = entity.level;
      this.speedModifier = speedModifier;
      setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      if(!entity.canBreed()) return false;
      else {
         long time = entity.level.getGameTime();
         if(time - lastCanUseCheck < 20L) return false;
         lastCanUseCheck = time;
         double bestDistSqr = Double.MAX_VALUE;
         TamableAnimalEntity partner = null;
         for(TamableAnimalEntity animal : level.getNearbyEntities(TamableAnimalEntity.class, PARTNER_TARGETING, entity, entity.getBoundingBox().inflate(15))) {
            if(entity.canBreedWith(animal) && animal.canBreed() && entity.distanceToSqr(animal) < bestDistSqr) {
               path = entity.getNavigator().findPath(animal, 0);
               if(path != null && path.reachesGoal()) {
                  partner = animal;
                  bestDistSqr = entity.distanceToSqr(animal);
               }
            }
         }
         this.partner = partner;
         return this.partner != null;
      }
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public boolean canContinueToUse() {
      return entity.canBreed() && partner != null && partner.isAlive() && partner.canBreed();
   }

   @Override
   public void start() {
      if(entity.sex == Sex.MALE) entity.getNavigator().moveTo(path, speedModifier);
      else entity.getNavigator().stop();
      path = null;
   }

   @Override
   public void stop() {
      partner = null;
      entity.getNavigator().stop();
      breedTimer = 0;
   }

   @Override
   public void tick() {
      if(entity.sex == Sex.MALE) {
         entity.getLookControl().setLookAt(partner, entity.getMaxYRotPerTick(), entity.getMaxXRotPerTick());
         if(MathUtil.getShortestDistanceSqrBoxToBox(entity.getBoundingBox(), partner.getBoundingBox()) < 0.5 * 0.5) {
            breedTimer++;
            if(breedTimer > 30) entity.breedPair(partner);
            else entity.startAction(entity.getBreedAction());
         }
         else {
            if(entity.getBreedTime() % 10 == 0 || entity.refreshPath || entity.getNavigator().isDone()) {
               ReversePath path = entity.getNavigator().findPath(partner, 0);
               if(path != null && path.reachesGoal()) entity.getNavigator().moveTo(path, speedModifier);
               else partner = null;
            }
         }
      }
      else entity.getLookControl().setLookAt(new Vec3(entity.getX() + (entity.getX() - partner.getX()), entity.getEyeY(), entity.getZ() + (entity.getZ() - partner.getZ())));
   }
}