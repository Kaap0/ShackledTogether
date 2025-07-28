package gg.kaapo.shackledtogether.chain;

import gg.kaapo.shackledtogether.ShackledTogether;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Objects;

public class PullMechanic implements Listener {

    //This class is a mess and works poorly

    private final ShackledTogether shackledTogether = ShackledTogether.getInstance();

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player target) {
            Chain chain = shackledTogether.getAPI().getSameChain(event.getPlayer(), target);
            if (chain == null) {
                return;
            }
            if (chain.getChainConfiguration().hasPullMechanic() && chain.getChainConfiguration().getPullMethod().equals(ChainConfiguration.PullMethod.RIGHT_CLICK)) {
                if (event.getHand().equals(EquipmentSlot.HAND)) {
                    double pullEfficiency = chain.getChainConfiguration().getPullEfficiency();
                    target.setVelocity(event.getPlayer().getLocation().getDirection().multiply(-1).multiply(pullEfficiency));
                }
            }
        }
    }

    @EventHandler
    public void onArmSwing(PlayerAnimationEvent event) {
        Player puller = event.getPlayer();
        Player target = getLineOfSight(puller);

        Chain chain = shackledTogether.getAPI().getSameChain(puller, target);

        if (chain != null && chain.getChainConfiguration().hasPullMechanic() && chain.getChainConfiguration().getPullMethod().equals(ChainConfiguration.PullMethod.LEFT_CLICK)) {
            double pullEfficiency = chain.getChainConfiguration().getPullEfficiency();
            target.setVelocity(event.getPlayer().getLocation().getDirection().multiply(-1).multiply(pullEfficiency));
        }

    }


    @EventHandler
    public void onInteract2(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Player target = getLineOfSight(player);
        if (target == null) {
            return;
        }
        Chain chain = shackledTogether.getAPI().getSameChain(player, target);
        if (chain != null && chain.getChainConfiguration().hasPullMechanic() && event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)) {
            if (Objects.requireNonNull(chain.getChainConfiguration().getPullMethod()) == ChainConfiguration.PullMethod.RIGHT_CLICK) {
                if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                } else {
                    return;
                }
            }
            double pullEfficiency = chain.getChainConfiguration().getPullEfficiency();
            target.setVelocity(event.getPlayer().getLocation().getDirection().multiply(-1).multiply(pullEfficiency));
        }

    }


    public Player getLineOfSight(Player pullerPlayer) {
        Chain chain = ShackledTogether.getInstance().getAPI().getChain(pullerPlayer);
        if (chain == null) {
            return null;
        }
        double pullRange = chain.getChainConfiguration().getPullRange();

        Vector direction = pullerPlayer.getLocation().getDirection();

        RayTraceResult result = pullerPlayer.getWorld().rayTraceEntities(pullerPlayer.getEyeLocation(), direction, pullRange, e -> e != pullerPlayer && !(e instanceof Rabbit));

        if (result == null || result.getHitEntity() == null) {
            return null;
        }

        if (result.getHitEntity() instanceof Player targetPlayer) {
            return targetPlayer;
        }
        return null;
    }

}

