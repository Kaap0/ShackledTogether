package gg.kaapo.shackledtogether.chain.visualizations;

import gg.kaapo.shackledtogether.chain.PivotPoint;
import gg.kaapo.shackledtogether.chain.PlayerPair;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class ChainVisualization {

    protected List<Player> players;
    protected List<PlayerPair> playerPairs;

    public abstract void init(List<Player> players, List<PlayerPair> playerPairs);

    public abstract void tick();

    public abstract void halt();

    public abstract void addPlayer(Player player);

    public abstract void removePlayer(Player player);

    public abstract void addPivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position);

    public abstract void removePivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position);

}
