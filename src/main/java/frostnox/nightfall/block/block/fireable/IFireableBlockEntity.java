package frostnox.nightfall.block.block.fireable;

public interface IFireableBlockEntity {
    void setCookTicks(int cookTicks);

    void setInStructure(boolean inStructure);

    int getCookTicks();

    boolean inStructure();
}
