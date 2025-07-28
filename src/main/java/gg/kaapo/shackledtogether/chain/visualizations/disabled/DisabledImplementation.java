package gg.kaapo.shackledtogether.chain.visualizations.disabled;

import gg.kaapo.shackledtogether.chain.PivotPoint;
import gg.kaapo.shackledtogether.chain.PlayerPair;
import gg.kaapo.shackledtogether.chain.visualizations.ChainVisualization;
import org.bukkit.entity.Player;

import java.util.List;

public class DisabledImplementation extends ChainVisualization {


    @Override
    public void init(List<Player> players, List<PlayerPair> playerPairs) {

    }

    @Override
    public void tick() {

    }

    @Override
    public void halt() {

    }

    @Override
    public void addPlayer(Player player) {

    }

    @Override
    public void removePlayer(Player player) {

    }

    @Override
    public void addPivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position) {

    }

    @Override
    public void removePivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position) {

    }
}
