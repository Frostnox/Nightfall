package frostnox.nightfall.world.generation;

import frostnox.nightfall.block.Tree;

public record TreePool(Entry[] trees, int size, int totalWeight) {
    public record Entry(Tree tree, int weight) {}
}
