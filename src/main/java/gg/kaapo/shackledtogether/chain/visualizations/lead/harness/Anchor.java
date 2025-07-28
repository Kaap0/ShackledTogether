package gg.kaapo.shackledtogether.chain.visualizations.lead.harness;

import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Anchor {

    private final ArrayList<Carabiner> carabiners;

    public Anchor(Carabiner... carabiners) {
        this.carabiners = new ArrayList<>(List.of(carabiners));
    }

    public Entity getCarabiner(int slot) {
        return carabiners.get(slot).getCarabiner();
    }

    public ArrayList<Carabiner> getCarabiners() {
        return carabiners;
    }

}
