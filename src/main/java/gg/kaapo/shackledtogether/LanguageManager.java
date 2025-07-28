package gg.kaapo.shackledtogether;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public class LanguageManager {

    private static JavaPlugin plugin;
    private FileConfiguration languageConfig;

    public LanguageManager(JavaPlugin plugifn) {
        plugin = plugifn;
        loadLanguage();
    }

    public static void createDefaultLanguageFiles(JavaPlugin plugifn) {
        plugin = plugifn;
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // load language files to /lang/ folder
        copyDefaultLangFile("lang_en_us.yml"); // ENGLISH - United States
        copyDefaultLangFile("lang_fi_fi.yml"); // FINLAND
    }

    private static void copyDefaultLangFile(String fileName) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + fileName);
        if (!langFile.exists()) {
            try (InputStream inputStream = plugin.getResource("lang/" + fileName)) {
                if (inputStream == null) {
                    plugin.getLogger().log(Level.WARNING, "Default language file " + fileName + " not found in the JAR!");
                    return;
                }
                Files.copy(inputStream, langFile.toPath());
                plugin.getLogger().log(Level.INFO, "Created default language file: " + fileName);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create language file: " + fileName, e);
            }
        }
    }

    public void loadLanguage() {
        String langCode = plugin.getConfig().getString("language", "en_us");
        File langFile = new File(plugin.getDataFolder(), "lang/lang_" + langCode + ".yml");

        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file " + langFile.getName() + " not found! Defaulting to 'en_us'.");
            langFile = new File(plugin.getDataFolder(), "lang/lang_en_us.yml");
        }

        languageConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String key) {
        return languageConfig.getString(key, "Message not found for key: " + key);
    }
}
