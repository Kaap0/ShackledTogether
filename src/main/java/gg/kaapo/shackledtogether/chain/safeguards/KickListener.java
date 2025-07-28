package gg.kaapo.shackledtogether.chain.safeguards;

import gg.kaapo.shackledtogether.ShackledTogether;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class KickListener implements Listener {

    private final ShackledTogether shackledTogether = ShackledTogether.getInstance();

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.getReason().equalsIgnoreCase(("Flying is not enabled on this server")) && shackledTogether.getAPI().isChained(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

}
