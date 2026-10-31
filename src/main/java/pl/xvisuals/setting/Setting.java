package pl.xvisuals.setting;

import com.google.gson.JsonElement;

import java.util.function.BooleanSupplier;

/** Bazowa klasa ustawienia modułu. */
public abstract class Setting {
    public final String name;
    private BooleanSupplier visible = () -> true;

    protected Setting(String name) {
        this.name = name;
    }

    /** Ustawienie pokazuje się w GUI tylko, gdy warunek jest spełniony. */
    public Setting visibleIf(BooleanSupplier condition) {
        this.visible = condition;
        return this;
    }

    public boolean isVisible() {
        return visible.getAsBoolean();
    }

    /** Wysokość wiersza w GUI (w pikselach). */
    public abstract int height();

    /** Zapis do configu; null = nie zapisuj. */
    public JsonElement toJson() {
        return null;
    }

    public void fromJson(JsonElement element) {
    }
}
