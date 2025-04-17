package frostnox.nightfall.item;

public enum Weight {
    NONE(0),
    LIGHT(1),
    MEDIUM(2),
    HEAVY(3);

    private int id;

    private Weight(int id) {
        this.id = id;
    }

    public int getSpeedModifier() {
        switch(id) {
            case 0:
            case 1: {
                return -1;
            }
            case 2: {
                return 0;
            }
            case 3: {
                return 1;
            }
        }
        return 0;
    }
}
