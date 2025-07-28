package gg.kaapo.shackledtogether.chain.safeguards;

import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.chain.Chain;
import gg.kaapo.shackledtogether.events.ChainLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

    private final ShackledTogether shackledTogether = ShackledTogether.getInstance();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Chain chain = shackledTogether.getAPI().getChain(event.getPlayer());
        if (chain == null) {
            return;
        }

        switch (shackledTogether.getConfig().getString("safeguard-teleport", "ANY").toUpperCase()) {
            case "DISABLED":
                return;

            case "WORLD_CHANGE":
                if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
                    return;
                }
                break;

            case "ANY":
                break;
        }

        ChainLeaveEvent leaveEvent = new ChainLeaveEvent(event.getPlayer(), chain, ChainLeaveEvent.LeaveReason.TELEPORTATION);
        shackledTogether.getServer().getPluginManager().callEvent(leaveEvent);
        chain.remove(event.getPlayer());
    }
}
