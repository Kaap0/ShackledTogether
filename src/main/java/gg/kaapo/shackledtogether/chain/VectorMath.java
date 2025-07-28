package gg.kaapo.shackledtogether.chain;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

public class VectorMath {

    /*
     https://www.youtube.com/watch?v=Krlf1XnzZGc
     */

    public static double calculateSignedHorizontalAngle(Location pivot, Location locationA, Location locationB) {
        Vector vectorA = locationA.toVector().subtract(pivot.toVector());
        Vector vectorB = locationB.toVector().subtract(pivot.toVector());

        Vector normalizedA = vectorA.clone().setY(0).normalize();
        Vector normalizedB = vectorB.clone().setY(0).normalize();

        double dotProduct = normalizedA.dot(normalizedB);

        double angle = Math.acos(dotProduct);

        angle = Math.toDegrees(angle);

        Vector crossProduct = normalizedA.crossProduct(normalizedB);

        if (crossProduct.getY() < 0) {
            angle = -angle;
        }

        return angle;
    }

    public static double calculateSignedVerticalAngle(Location pivot, Location locationA, Location locationB, BlockFace cardinalDirection) {
        Vector vectorA = locationA.toVector().subtract(pivot.toVector());
        Vector vectorB = locationB.toVector().subtract(pivot.toVector());

        double axisA, axisB;

        switch (cardinalDirection) {
            case NORTH:
            case SOUTH:
                axisA = vectorA.getZ();
                axisB = vectorB.getZ();
                break;
            case EAST:
            case WEST:
                axisA = vectorA.getX();
                axisB = vectorB.getX();
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: must be NORTH, EAST, SOUTH, or WEST.");
        }

        Vector normalizedA = new Vector(axisA, vectorA.getY(), 0).normalize();
        Vector normalizedB = new Vector(axisB, vectorB.getY(), 0).normalize();

        double dotProduct = normalizedA.dot(normalizedB);

        double angle = Math.acos(dotProduct);

        angle = Math.toDegrees(angle);

        Vector crossProduct = normalizedA.crossProduct(normalizedB);

        if (crossProduct.getZ() < 0) {
            angle = -angle;
        }

        return angle;
    }


    /*
    Credit eccentric spigotmc.org
     */
    public static boolean isCube(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        BoundingBox boundingBox = block.getBoundingBox();
        return (voxelShape.getBoundingBoxes().size() == 1 && boundingBox.getWidthX() == 1.0 && boundingBox.getHeight() == 1.0 && boundingBox.getWidthZ() == 1.0);
    }

    public static RayTraceResult blockBetween(Location start, Location end) {
        if (start.getWorld() == null) {
            return null;
        }

        double distance = end.toVector().subtract(start.toVector()).length();

        if (distance == 0) {
            return null;
        }

        Vector directionAToB = end.toVector().subtract(start.toVector()).normalize();

        RayTraceResult resultAtoB = start.getWorld().rayTraceBlocks(start, directionAToB, start.distance(end), FluidCollisionMode.NEVER, true);

        if (resultAtoB != null && resultAtoB.getHitBlock() != null && valid(resultAtoB.getHitBlock())) {
            return resultAtoB;
        }
        return null;
    }

    public static boolean valid(Block block) {
        return block.getType().isSolid() && block.getType().isBlock() && isCube(block) && !block.getType().equals(Material.AIR);
    }

}
