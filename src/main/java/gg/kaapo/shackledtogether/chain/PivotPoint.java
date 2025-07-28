package gg.kaapo.shackledtogether.chain;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PivotPoint {

    private final Block block;

    private final Player initiator;
    private final Location visualCorner;
    private final Location hitScanCorner;
    private final Location pullCorner;
    private final Location losCorner;
    private final BlockFace blockFaceA;
    private final BlockFace blockFaceB;
    private boolean startedAsPositiveSign;
    private boolean isHorizontal;
    private BlockFace cardinalDirection;
    private boolean isSuccesful;
    private boolean isObstructed;

    protected PivotPoint(Block block, BlockFace blockFaceA, BlockFace blockFaceB, Player initiator) {
        this.block = block;
        this.initiator = initiator;
        this.blockFaceA = blockFaceA;
        this.blockFaceB = blockFaceB;
        this.hitScanCorner = block.getLocation().clone().add(0.5, 0.5, 0.5);
        this.pullCorner = block.getLocation().clone().add(0.5, 0.5, 0.5);
        this.visualCorner = block.getLocation().clone().add(0.5, 0.5, 0.5);
        this.losCorner = block.getLocation().clone().add(0.5, 0.5, 0.5);

        double x = blockFaceA.getModX() + blockFaceB.getModX();
        if (x != 0) {
            hitScanCorner.add(x / 1.625, 0, 0); //BETWEEN 1.5 AND 1.75 SEEMS GOOD?
            pullCorner.add(x, 0, 0);
            visualCorner.add(x / 2, 0, 0);
            losCorner.add(x / 1.25, 0, 0);
        }
        double y = blockFaceA.getModY() + blockFaceB.getModY();
        if (y != 0) {
            hitScanCorner.add(0, y / 1.625, 0); //BETWEEN 1.5 AND 1.75 SEEMS GOOD?
            pullCorner.add(0, y, 0);
            visualCorner.add(0, y / 2, 0);
            losCorner.add(0, y / 1.25, 0);
            if (blockFaceA.equals(BlockFace.UP) || blockFaceA.equals(BlockFace.DOWN)) {
                cardinalDirection = blockFaceB;
            } else {
                cardinalDirection = blockFaceA;
            }
        }
        double z = blockFaceA.getModZ() + blockFaceB.getModZ();
        if (z != 0) {
            hitScanCorner.add(0, 0, z / 1.625); //BETWEEN 1.5 AND 1.75 SEEMS GOOD?
            pullCorner.add(0, 0, z);
            visualCorner.add(0, 0, z / 2);
            losCorner.add(0, 0, z / 1.25);
        }


        if (x == 0 && y == 0 && z == 0 || block.getType().equals(Material.AIR) || blockFaceA.equals(BlockFace.SELF) || blockFaceB.equals(BlockFace.SELF) || blockFaceA.equals(blockFaceB)) {
            Debug.severe("Valid PivotPoint may not be calculated from faces: " + blockFaceA + " and " + blockFaceB);
            isSuccesful = false;
            return;
        }
        World world = block.getWorld();
        //TODO or valid?
        if (world.getBlockAt(hitScanCorner).getType().isSolid() || VectorMath.valid(block.getRelative(blockFaceA)) || VectorMath.valid(block.getRelative(blockFaceB))) {
            isObstructed = true;
        }
        isHorizontal = y == 0;
        isSuccesful = true;

    }

    public Block getBlock() {
        return block;
    }

    public Player getInitiator() {
        return initiator;
    }

    public boolean isStartedAsPositiveSign() {
        return startedAsPositiveSign;
    }

    public void setStartedAsPositiveSign(boolean startedAsPositiveSign) {
        this.startedAsPositiveSign = startedAsPositiveSign;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public Location getHitScanCorner() {
        return hitScanCorner;
    }

    public Location getPullCorner() {
        return pullCorner;
    }

    public Location getLosCorner() {
        return losCorner;
    }

    public Location getVisualCorner() {
        return visualCorner;
    }

    public BlockFace getCardinalDirection() {
        return cardinalDirection;
    }

    public BlockFace getBlockFaceA() {
        return blockFaceA;
    }

    public BlockFace getBlockFaceB() {
        return blockFaceB;
    }

    public boolean isSuccesful() {
        return isSuccesful;
    }

    public boolean isObstructed() {
        return isObstructed;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PivotPoint that = (PivotPoint) o;

        return (Objects.equals(visualCorner, that.getVisualCorner()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(visualCorner);
    }

    @Override
    public String toString() {
        return "PivotPoint {\n" +
                "  block = " + (block != null ? block.getType() + " at " + block.getLocation().toVector() : "null") + ",\n" +
                "  initiator = " + (initiator != null ? initiator.getName() : "null") + ",\n" +
                "  visualCorner = " + formatLocation(visualCorner) + ",\n" +
                "  hitScanCorner = " + formatLocation(hitScanCorner) + ",\n" +
                "  pullCorner = " + formatLocation(pullCorner) + ",\n" +
                "  blockFaceA = " + blockFaceA + ",\n" +
                "  blockFaceB = " + blockFaceB + ",\n" +
                "  startedAsPositiveSign = " + startedAsPositiveSign + ",\n" +
                "  isHorizontal = " + isHorizontal + ",\n" +
                "  cardinalDirection = " + cardinalDirection + ",\n" +
                "  isSuccesful = " + isSuccesful + "\n" +
                "  isObstructed = " + isObstructed + "\n" +
                '}';
    }

    private String formatLocation(Location loc) {
        if (loc == null) return "null";
        return String.format("(%.2f, %.2f, %.2f)", loc.getX(), loc.getY(), loc.getZ());
    }

    public enum Position {
        FIRST_ENTRY, HEAD, TAIL;
    }
}
