package frostnox.nightfall.item;

import frostnox.nightfall.action.DamageType;
import frostnox.nightfall.action.HurtSphere;
import frostnox.nightfall.action.player.PlayerActionSet;
import frostnox.nightfall.world.ToolActionsNF;
import net.minecraftforge.common.ToolAction;

import java.util.List;

public enum Armament implements IArmament {
    ADZE(PlayerActionSet.ADZE, HurtSphere.ADZE, DamageType.SLASHING, ImpactSoundType.PIERCE, true, ToolActionsNF.STRIP, ToolActionsNF.TILL),
    AXE(PlayerActionSet.AXE, HurtSphere.AXE, DamageType.SLASHING, ImpactSoundType.STRIKE, true),
    CHISEL(PlayerActionSet.CHISEL, HurtSphere.CHISEL, DamageType.PIERCING, ImpactSoundType.PIERCE, true),
    KNIFE(PlayerActionSet.KNIFE, HurtSphere.KNIFE, DamageType.SLASHING, ImpactSoundType.SLASH, true, ToolActionsNF.SKIN),
    HAMMER(PlayerActionSet.HAMMER, HurtSphere.HAMMER, DamageType.STRIKING, ImpactSoundType.STRIKE, true),
    MACE(PlayerActionSet.MACE, HurtSphere.MACE, DamageType.STRIKING, ImpactSoundType.STRIKE, false),
    PICKAXE(PlayerActionSet.PICKAXE, HurtSphere.PICKAXE, DamageType.STRIKING, ImpactSoundType.STRIKE, true),
    SABRE(PlayerActionSet.SABRE, HurtSphere.SABRE, DamageType.SLASHING, ImpactSoundType.SLASH, false),
    SHOVEL(PlayerActionSet.SHOVEL, HurtSphere.SHOVEL, DamageType.STRIKING, ImpactSoundType.STRIKE, true),
    SICKLE(PlayerActionSet.SICKLE, HurtSphere.SICKLE, DamageType.SLASHING, ImpactSoundType.SLASH, true),
    SPEAR(PlayerActionSet.SPEAR, HurtSphere.SPEAR, DamageType.PIERCING, ImpactSoundType.PIERCE, false),
    SWORD(PlayerActionSet.SWORD, HurtSphere.SWORD, DamageType.SLASHING, ImpactSoundType.SLASH, false);

    private final PlayerActionSet actionSet;
    private final HurtSphere hurtSphere;
    private final DamageType damageType;
    private final ImpactSoundType impactSoundType;
    private final List<ToolAction> toolActions;
    private final boolean canDig;

    Armament(PlayerActionSet actionSet, HurtSphere hurtSphere, DamageType damageType, ImpactSoundType impactSoundType, boolean canDig, ToolAction... toolActions) {
        this.actionSet = actionSet;
        this.hurtSphere = hurtSphere;
        this.damageType = damageType;
        this.impactSoundType = impactSoundType;
        this.canDig = canDig;
        this.toolActions = List.of(toolActions);
    }

    @Override
    public PlayerActionSet getActionSet() {
        return actionSet;
    }

    @Override
    public HurtSphere getHurtSpheres() {
        return hurtSphere;
    }

    @Override
    public DamageType getDefaultDamageType() {
        return damageType;
    }

    @Override
    public ImpactSoundType getImpactSoundType() {
        return impactSoundType;
    }

    @Override
    public List<ToolAction> getToolActions() {
        return toolActions;
    }

    @Override
    public boolean canDig() {
        return canDig;
    }
}
