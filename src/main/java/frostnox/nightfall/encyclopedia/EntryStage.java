package frostnox.nightfall.encyclopedia;

public enum EntryStage {
    HIDDEN, LOCKED, PUZZLE, COMPLETED;

    public EntryStage advance() {
        if(this == COMPLETED) return COMPLETED;
        else return values()[ordinal() + 1];
    }
}
