package gg.kaapo.shackledtogether.chain;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Objects;

public class PlayerPair {

    private final Player playerA;
    private final Player playerB;

    private final LinkedList<PivotPoint> pivotPoints = new LinkedList<>();

    protected PlayerPair(Player playerA, Player playerB) {
        this.playerA = playerA;
        this.playerB = playerB;
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public LinkedList<PivotPoint> getPivotPoints() {
        return pivotPoints;
    }

    public PivotPoint getLastPivotPoint(Player player) {
        if (pivotPoints.isEmpty()) {
            return null;
        }
        if (player.equals(playerA)) {
            return pivotPoints.getFirst();
        } else {
            return pivotPoints.getLast();
        }
    }

    public PivotPoint getSecondLastPivotPoint(Player player) {
        if (pivotPoints.isEmpty() || pivotPoints.size() < 2) {
            return null;
        }
        if (player.equals(playerA)) {
            return pivotPoints.get(1);
        } else {
            return pivotPoints.get(pivotPoints.size() - 2);
        }
    }

    public boolean hasPivotPoints() {
        return !pivotPoints.isEmpty();
    }

    public Player theOtherPlayer(Player player) {
        if (playerA.equals(player)) {
            return playerB;
        } else {
            return playerA;
        }
    }

    public double distance() {
        Location pALocation = playerA.getLocation();
        Location pBLocation = playerB.getLocation();
        double totalDistance = 0;

        if (!hasPivotPoints()) {
            return pALocation.distance(pBLocation);
        }

        totalDistance += pALocation.distance(pivotPoints.getFirst().getPullCorner());

        for (int i = 0; i < pivotPoints.size() - 1; i++) {
            Location current = pivotPoints.get(i).getPullCorner();
            Location next = pivotPoints.get(i + 1).getPullCorner();
            totalDistance += current.distance(next);
        }

        totalDistance += pivotPoints.getLast().getPullCorner().distance(pBLocation);

        return totalDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerPair that = (PlayerPair) o;

        return (Objects.equals(playerA, that.playerA) && Objects.equals(playerB, that.playerB)) ||
                (Objects.equals(playerA, that.playerB) && Objects.equals(playerB, that.playerA));
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerA) + Objects.hash(playerB);
    }
}