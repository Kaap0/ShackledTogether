package gg.kaapo.shackledtogether.chain.safeguards;

import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.chain.Chain;
import gg.kaapo.shackledtogether.events.ChainLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final ShackledTogether shackledTogether = ShackledTogether.getInstance();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Chain chain = shackledTogether.getAPI().getChain(event.getEntity());
        if (chain != null) {
            if (shackledTogether.getConfig().getBoolean("safeguard-death")) {
                ChainLeaveEvent chainLeaveEvent = new ChainLeaveEvent(event.getEntity(), chain, ChainLeaveEvent.LeaveReason.DEATH);
                ShackledTogether.getInstance().getServer().getPluginManager().callEvent(chainLeaveEvent);
                chain.remove(event.getEntity());
            }
        }
    }
}
