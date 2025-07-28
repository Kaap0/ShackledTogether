package gg.kaapo.shackledtogether;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import gg.kaapo.shackledtogether.chain.PullMechanic;
import gg.kaapo.shackledtogether.chain.ShackledTogetherAPI;
import gg.kaapo.shackledtogether.chain.safeguards.DeathListener;
import gg.kaapo.shackledtogether.chain.safeguards.KickListener;
import gg.kaapo.shackledtogether.chain.safeguards.QuitListener;
import gg.kaapo.shackledtogether.chain.safeguards.TeleportListener;
import gg.kaapo.shackledtogether.commands.ShackleCommand;
import gg.kaapo.shackledtogether.commands.TabComplete;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShackledTogether extends JavaPlugin {

    public static String prefix;
    private static ShackledTogether instance;
    private static LanguageManager languageManager;
    private ShackledTogetherAPI api;
    private ProtocolManager protocolManager;

    public static ShackledTogether getInstance() {
        return instance;
    }

    public static LanguageManager getLanguageManager() {
        return languageManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        api = new ShackledTogetherAPI();
        prefix = Utility.translateColorCodes("&#4F453F[&#504640S&#514740h&#524941a&#534A42c&#534B43k&#544C43l&#554D44e&#564F45d&#575046T&#585146o&#595247g&#5A5448e&#5B5549t&#5C5649h&#5C574Ae&#5D584Br&#5E5A4C]&#5F5B4C: ");
        saveDefaultConfig();
        LanguageManager.createDefaultLanguageFiles(this);
        languageManager = new LanguageManager(this);

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolManager = ProtocolLibrary.getProtocolManager();
        }


        getCommand("shackle").setExecutor(new ShackleCommand());
        getCommand("shackle").setTabCompleter(new TabComplete());

        Bukkit.getPluginManager().registerEvents(new PullMechanic(), this);
        Bukkit.getPluginManager().registerEvents(new QuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new KickListener(), this);
        Bukkit.getPluginManager().registerEvents(new TeleportListener(), this);

        Bukkit.getLogger().info("[ShackledTogether] Version: " + getDescription().getVersion());
        Bukkit.getLogger().info("[ShackledTogether] Authors: " + getDescription().getAuthors());
        Bukkit.getLogger().info("[ShackledTogether] Github: https://github.com/Kaap0/ShackledTogether");
        Bukkit.getLogger().info("[ShackledTogether] Donate: https://github.com/sponsors/Kaap0");
        Bukkit.getLogger().info("[ShackledTogether] Licence: GNU AGPL");


    }

    @Override
    public void onDisable() {

    }

    public ShackledTogetherAPI getAPI() {
        return api;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

}
