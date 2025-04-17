package frostnox.nightfall.item;

public enum Dye implements IDye {
    RED(0xb93333),
    BLUE(0x334cd2),
    YELLOW(0xe9e911),
    GREEN(0x56a233), //Blue & yellow
    ORANGE(0xf18833), //Yellow & red
    PURPLE(0xa23fa8), //Red & blue
    TEAL(0x33bfbf), //Blue & green
    INDIGO(0x7433e3), //Blue & purple
    VERMILION(0xca4c2f), //Red & orange
    OCHRE(0xdf9725), //Yellow & orange
    LIME(0xb5e12d), //Yellow & green
    MAGENTA(0xb20c73), //Red & purple
    PINK(0xf7bfc9),
    BROWN(0x6b4a2b),
    GRAY(0x727272),
    BLACK(0x313131);

    private final int color;

    Dye(int color) {
        this.color = color;
    }

    @Override
    public int getColor() {
        return color;
    }
}
