package gg.kaapo.shackledtogether.chain.visualizations.lead.harness;

import gg.kaapo.shackledtogether.chain.visualizations.lead.LeadImplementation;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;

public class Carabiner {

    private final Entity carabiner;

    public Carabiner(Location location) {
        Rabbit rabbit = (Rabbit) location.getWorld().spawnEntity(location, EntityType.RABBIT);
        rabbit.setAI(false);
        rabbit.setGravity(false);
        rabbit.setInvisible(true);
        rabbit.setInvulnerable(true);
        rabbit.setCustomNameVisible(false);
        rabbit.setSilent(true);
        rabbit.setAware(false);
        rabbit.setLootTable(null);
        rabbit.setBaby();
        rabbit.setAgeLock(true);
        rabbit.setPersistent(false);
        rabbit.setCollidable(false);
        rabbit.setCustomName("shackledtogetheraeaeaeae");
        LeadImplementation.getMasterScoreboard().getTeam("shackledtogether").addEntry(rabbit.getUniqueId().toString());
        this.carabiner = rabbit;
    }

    public void nuke() {
        LeadImplementation.getMasterScoreboard().getTeam("shackledtogether").removeEntry(carabiner.getUniqueId().toString());
        carabiner.remove();
    }

    public Entity getCarabiner() {
        return carabiner;
    }
}
