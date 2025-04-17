package frostnox.nightfall.encyclopedia;

import frostnox.nightfall.capability.IPlayerData;
import frostnox.nightfall.encyclopedia.knowledge.Knowledge;
import frostnox.nightfall.registry.RegistriesNF;
import net.minecraft.Util;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Entry extends ForgeRegistryEntry<Entry> {
    public final List<RegistryObject<? extends Entry>> parents;
    public final Set<RegistryObject<? extends Knowledge>> prerequisites;
    public final @Nullable Puzzle puzzle;
    public final boolean isHidden, isAddendum;
    private String descriptionId;

    protected Entry(List<RegistryObject<? extends Entry>> parents, Set<RegistryObject<? extends Knowledge>> prerequisites, @Nullable Puzzle puzzle, boolean isHidden, boolean isAddendum) {
        this.parents = parents;
        this.prerequisites = prerequisites;
        this.puzzle = puzzle;
        this.isHidden = isHidden;
        this.isAddendum = isAddendum;
    }

    public static Entry create(List<RegistryObject<? extends Entry>> parents, Set<RegistryObject<? extends Knowledge>> prerequisites, Puzzle puzzle) {
        return new Entry(parents, prerequisites, puzzle, false, false);
    }

    public static Entry createHidden(List<RegistryObject<? extends Entry>> parents, Set<RegistryObject<? extends Knowledge>> prerequisites, Puzzle puzzle) {
        return new Entry(parents, prerequisites, puzzle, true, false);
    }

    public static Entry createAddendum(List<RegistryObject<? extends Entry>> parents, RegistryObject<? extends Knowledge>... prerequisites) {
        return new Entry(parents, Set.of(prerequisites), null, true, true);
    }

    public boolean shouldReveal(IPlayerData encyclopedia) {
        if(isHidden) return false;
        boolean puzzle = false;
        for(RegistryObject<? extends Entry> parent : parents) {
            EntryStage parentStage = encyclopedia.getStage(parent.getId());
            if(parentStage == EntryStage.HIDDEN || parentStage == EntryStage.LOCKED) return false;
            else if(parentStage == EntryStage.PUZZLE) puzzle = true;
        }
        if(puzzle) {
            if(prerequisites.isEmpty()) return false;
            else for(RegistryObject<? extends Knowledge> knowledge : prerequisites) {
                if(!encyclopedia.hasKnowledge(knowledge.getId())) return false;
            }
        }
        return true;
    }

    public boolean shouldUnlock(IPlayerData encyclopedia) {
        for(RegistryObject<? extends Knowledge> knowledge : prerequisites) {
            if(!encyclopedia.hasKnowledge(knowledge.getId())) return false;
        }
        for(RegistryObject<? extends Entry> parent : parents) {
            if(!encyclopedia.hasEntryStage(parent.getId(), EntryStage.COMPLETED)) return false;
        }
        return true;
    }

    public Set<RegistryObject<? extends Knowledge>> getAssociatedKnowledge() {
        if(puzzle == null) return prerequisites;
        else return Stream.concat(prerequisites.stream(), puzzle.knowledge().stream()).collect(Collectors.toUnmodifiableSet());
    }

    public String getDescriptionId() {
        if(descriptionId == null) {
            descriptionId = Util.makeDescriptionId("entry", RegistriesNF.getEntries().getKey(this));
        }
        return descriptionId;
    }
}
