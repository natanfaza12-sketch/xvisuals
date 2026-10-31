package pl.xvisuals.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ModeSetting extends Setting {
    public final String[] modes;
    private int index;

    public ModeSetting(String name, String... modes) {
        super(name);
        this.modes = modes;
    }

    public String get() {
        return modes[index];
    }

    public int index() {
        return index;
    }

    public boolean is(String mode) {
        return modes[index].equalsIgnoreCase(mode);
    }

    public void next() {
        index = (index + 1) % modes.length;
    }

    public void previous() {
        index = (index - 1 + modes.length) % modes.length;
    }

    @Override
    public int height() {
        return 26;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(modes[index]);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) return;
        String s = element.getAsString();
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(s)) {
                index = i;
                return;
            }
        }
    }

    public void select(String mode) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(mode)) {
                index = i;
                return;
            }
        }
    }
}
