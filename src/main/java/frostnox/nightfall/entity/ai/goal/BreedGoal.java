package frostnox.nightfall.entity.ai.goal;

import frostnox.nightfall.entity.ai.pathfinding.ReversePath;
import frostnox.nightfall.entity.entity.animal.TamableAnimalEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class BreedGoal extends Goal {
   private static final TargetingConditions PARTNER_TARGETING = TargetingConditions.forNonCombat().range(8.0D).ignoreLineOfSight();
   protected final TamableAnimalEntity entity;
   protected final Level level;
   protected final double speedModifier;
   protected @Nullable TamableAnimalEntity partner;
   protected ReversePath path;
   protected long lastCanUseCheck;

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
      entity.getNavigator().moveTo(path, speedModifier);
      path = null;
   }

   @Override
   public void stop() {
      partner = null;
      entity.getNavigator().stop();
   }

   @Override
   public void tick() {
      if(entity.getBoundingBox().intersects(partner.getBoundingBox())) {
         entity.breedPair(partner);
      }
      else {
         entity.getLookControl().setLookAt(partner, entity.getMaxYRotPerTick(), entity.getMaxXRotPerTick());
         if(entity.getBreedTime() % 10 == 0 || entity.refreshPath || entity.getNavigator().isDone()) {
            ReversePath path = entity.getNavigator().findPath(partner, 0);
            if(path != null && path.reachesGoal()) entity.getNavigator().moveTo(path, speedModifier);
            else partner = null;
         }
      }
   }
}