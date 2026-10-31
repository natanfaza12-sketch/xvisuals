package pl.xvisuals.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import pl.xvisuals.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    /** false = moduł jest zawsze aktywny i nie ma przełącznika w GUI. */
    public final boolean togglable;
    public final List<Setting> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, String description, Category category, boolean togglable) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.togglable = togglable;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public boolean isEnabled() {
        return !togglable || enabled;
    }

    public boolean rawEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void onTick(MinecraftClient mc) {
    }

    public void onAttack(MinecraftClient mc, Entity target) {
    }
}
