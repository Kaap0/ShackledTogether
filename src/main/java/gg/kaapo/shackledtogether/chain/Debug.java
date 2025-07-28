package gg.kaapo.shackledtogether.chain;

import gg.kaapo.shackledtogether.ShackledTogether;
import org.bukkit.Bukkit;

public class Debug {

    public static void print(String... s) {
        if (ShackledTogether.getInstance().getConfig().getBoolean("debug")) {
            for (String string : s) {
                Bukkit.getLogger().info(string);
            }
        }
    }

    public static void severe(String... s) {
        if (ShackledTogether.getInstance().getConfig().getBoolean("debug")) {
            for (String string : s) {
                Bukkit.getLogger().severe(string);
            }
        }
    }
}
