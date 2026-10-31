package pl.xvisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import pl.xvisuals.module.Module;
import pl.xvisuals.module.Modules;
import pl.xvisuals.setting.Setting;

import java.nio.file.Files;
import java.nio.file.Path;

/** Zapis/odczyt ustawień do config/xvisuals.json. */
public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Config() {
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("xvisuals.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            for (Module m : Modules.ALL) {
                if (!root.has(m.name)) continue;
                JsonObject mo = root.getAsJsonObject(m.name);
                if (mo.has("enabled")) m.setEnabled(mo.get("enabled").getAsBoolean());
                for (Setting s : m.settings) {
                    if (mo.has(s.name)) s.fromJson(mo.get(s.name));
                }
            }
        } catch (Exception e) {
            System.err.println("[X Visuals] Nie udało się wczytać configu: " + e);
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : Modules.ALL) {
                JsonObject mo = new JsonObject();
                if (m.togglable) mo.addProperty("enabled", m.rawEnabled());
                for (Setting s : m.settings) {
                    if (s.toJson() != null) mo.add(s.name, s.toJson());
                }
                root.add(m.name, mo);
            }
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception e) {
            System.err.println("[X Visuals] Nie udało się zapisać configu: " + e);
        }
    }
}
