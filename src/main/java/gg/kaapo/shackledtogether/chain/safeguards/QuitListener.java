package gg.kaapo.shackledtogether.chain.safeguards;

import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.chain.Chain;
import gg.kaapo.shackledtogether.events.ChainLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final ShackledTogether shackledTogether = ShackledTogether.getInstance();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Chain chain = shackledTogether.getAPI().getChain(event.getPlayer());
        if (chain != null) {
            ChainLeaveEvent chainLeaveEvent = new ChainLeaveEvent(event.getPlayer(), chain, ChainLeaveEvent.LeaveReason.DISCONNECT);
            ShackledTogether.getInstance().getServer().getPluginManager().callEvent(chainLeaveEvent);
            chain.remove(event.getPlayer());
        }
    }
}
