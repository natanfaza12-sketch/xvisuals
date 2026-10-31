package pl.xvisuals.setting;

/** Przycisk w GUI (np. "Podgląd dźwięku"). Nie jest zapisywany do configu. */
public class ButtonSetting extends Setting {
    private final Runnable action;

    public ButtonSetting(String name, Runnable action) {
        super(name);
        this.action = action;
    }

    public void click() {
        action.run();
    }

    @Override
    public int height() {
        return 28;
    }
}
