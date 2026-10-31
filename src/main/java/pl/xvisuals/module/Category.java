package pl.xvisuals.module;

public enum Category {
    VISUAL("Visual"),
    SETTINGS("Settings");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
