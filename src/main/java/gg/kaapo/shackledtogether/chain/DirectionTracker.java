package gg.kaapo.shackledtogether.chain;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;

public class DirectionTracker implements Listener {

    private final List<Player> players;
    private final HashMap<Player, Heading> headings = new HashMap<>();
    private final HashMap<Player, Location> lastLocations = new HashMap<>();

    protected DirectionTracker(List<Player> players) {
        this.players = players;
    }

    protected void tick() {
        for (Player player : players) {
            Location from = lastLocations.get(player);
            Location to = player.getLocation();
            if (from != null) {
                if (from.distanceSquared(to) > 0) {
                    Vector direction = to.toVector().subtract(from.toVector()).normalize();
                    headings.put(player, getHeading(direction));
                } else {
                    headings.put(player, Heading.UNDEFINED);
                }
            }
            lastLocations.put(player, to);
        }

    }

    protected void untrack(Player player) {
        headings.remove(player);
        lastLocations.remove(player);
    }

    private Heading getHeading(Vector vector) {
        double x = vector.getX();
        double y = vector.getY() * 0.7;
        double z = vector.getZ();

        if (Math.abs(x) > Math.abs(y) && Math.abs(x) > Math.abs(z)) {
            return x > 0 ? Heading.EAST : Heading.WEST;
        } else if (Math.abs(z) > Math.abs(y)) {
            return z > 0 ? Heading.SOUTH : Heading.NORTH;
        } else {
            return y > 0 ? Heading.UP : Heading.DOWN;
        }
    }

    protected Heading getHeading(Player player) {
        return headings.get(player);
    }

    public enum Heading {

        NORTH, EAST, SOUTH, WEST, UP, DOWN, UNDEFINED;

        public Heading getOpposite() {
            switch (this) {
                case NORTH:
                    return SOUTH;
                case SOUTH:
                    return NORTH;
                case EAST:
                    return WEST;
                case WEST:
                    return EAST;
                case UP:
                    return DOWN;
                case DOWN:
                    return UP;
                default:
                    return UNDEFINED;
            }
        }

        public BlockFace toBlockFace() {
            switch (this) {
                case NORTH:
                    return BlockFace.NORTH;
                case SOUTH:
                    return BlockFace.SOUTH;
                case EAST:
                    return BlockFace.EAST;
                case WEST:
                    return BlockFace.WEST;
                case UP:
                    return BlockFace.UP;
                case DOWN:
                    return BlockFace.DOWN;
                default:
                    return BlockFace.SELF;
            }
        }

    }
}
