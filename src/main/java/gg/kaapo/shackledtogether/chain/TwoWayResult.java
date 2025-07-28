package gg.kaapo.shackledtogether.chain;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.RayTraceResult;

public class TwoWayResult {

    private final RayTraceResult resultA;
    private final RayTraceResult resultB;

    private final boolean isSuccessful;
    private boolean isSameBlock;
    private boolean isOppositeBlockFaces;
    private Block hitBlock;
    private Block hitBlockA;
    private Block hitBlockB;
    private BlockFace blockFaceA;
    private BlockFace blockFaceB;

    protected TwoWayResult(RayTraceResult resultA, RayTraceResult resultB) {
        this.resultA = resultA;
        this.resultB = resultB;
        if (resultA == null || resultB == null) {
            isSuccessful = false;
            return;
        }

        if (resultA.getHitBlock() == null || resultB.getHitBlock() == null) {
            isSuccessful = false;
            return;
        }

        if (resultA.getHitBlockFace() == null || resultB.getHitBlockFace() == null) {
            isSuccessful = false;
            return;
        }

        isSuccessful = true;
        blockFaceA = resultA.getHitBlockFace();
        blockFaceB = resultB.getHitBlockFace();
        if (!resultA.getHitBlock().equals(resultB.getHitBlock())) {
            isSameBlock = false;
            hitBlockA = resultA.getHitBlock();
            hitBlockB = resultB.getHitBlock();
            hitBlock = null;
        } else {
            isSameBlock = true;
            hitBlock = resultA.getHitBlock();
        }
        if (blockFaceA.getOppositeFace().equals(blockFaceB)) {
            isOppositeBlockFaces = true;
        }
    }


    public Block getHitBlock() {
        return hitBlock;
    }

    public Block getHitBlockA() {
        return hitBlockA;
    }

    public Block getHitBlockB() {
        return hitBlockB;
    }

    public BlockFace getBlockFaceA() {
        return blockFaceA;
    }

    public BlockFace getBlockFaceB() {
        return blockFaceB;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public boolean isSameBlock() {
        return isSameBlock;
    }

    public boolean isOppositeBlockFaces() {
        return isOppositeBlockFaces;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TwoWayResult{");
        sb.append("isSuccessful=").append(isSuccessful);

        if (!isSuccessful) {
            sb.append(", resultA=").append(resultA == null ? "null" : "present");
            sb.append(", resultB=").append(resultB == null ? "null" : "present");
            sb.append("}");
            return sb.toString();
        }

        sb.append(", isSameBlock=").append(isSameBlock);
        sb.append(", isOppositeBlockFaces=").append(isOppositeBlockFaces);

        if (isSameBlock) {
            sb.append(", hitBlock=").append(hitBlock != null ? formatBlock(hitBlock) : "null");
        } else {
            sb.append(", hitBlockA=").append(hitBlockA != null ? formatBlock(hitBlockA) : "null");
            sb.append(", hitBlockB=").append(hitBlockB != null ? formatBlock(hitBlockB) : "null");
        }

        sb.append(", blockFaceA=").append(blockFaceA);
        sb.append(", blockFaceB=").append(blockFaceB);
        sb.append("}");
        return sb.toString();
    }

    private String formatBlock(Block block) {
        return String.format("%s[x=%d,y=%d,z=%d]",
                block.getType(),
                block.getX(),
                block.getY(),
                block.getZ());
    }

}
