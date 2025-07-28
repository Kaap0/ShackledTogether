package gg.kaapo.shackledtogether.chain;

import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.events.ChainDisbandEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Chain {

    static final List<Chain> chains = new ArrayList<>();

    private final UUID uuid = UUID.randomUUID();

    private final List<Player> players = new ArrayList<>();
    private final List<PlayerPair> playerPairs = new ArrayList<>();
    private final DirectionTracker directionTracker = new DirectionTracker(Collections.unmodifiableList(players));
    private final ChainConfiguration chainConfiguration;
    private BukkitTask task;

    private boolean customConfiguration = true;

    protected Chain(List<Player> players, ChainConfiguration chainConfiguration) {
        if (chainConfiguration == null) {
            chainConfiguration = new ChainConfiguration();
            customConfiguration = false;
        }
        this.players.addAll(players);
        this.chainConfiguration = chainConfiguration;
        pairPlayers();
        chainConfiguration.getChainVisualization().init(Collections.unmodifiableList(players), Collections.unmodifiableList(playerPairs));
        chains.add(this);
        start();
    }

    public UUID getUuid() {
        return uuid;
    }

    public ChainConfiguration getChainConfiguration() {
        return chainConfiguration;
    }

    public boolean contains(Player player) {
        return players.contains(player);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public int playerCount() {
        return players.size();
    }

    private boolean hasEnoughParticipants() {
        return playerCount() >= 2;
    }

    public boolean remove(Player player) {
        if (!players.contains(player)) {
            return false;
        }
        players.remove(player);
        directionTracker.untrack(player);
        if (!hasEnoughParticipants()) {
            ChainDisbandEvent event = new ChainDisbandEvent(this, ChainDisbandEvent.DisbandReason.NOT_ENOUGH_PLAYERS);
            ShackledTogether.getInstance().getServer().getPluginManager().callEvent(event);
            nuke();
        } else {
            pairPlayers();
            chainConfiguration.getChainVisualization().removePlayer(player);
        }
        return true;
    }

    public void destroy() {
        ChainDisbandEvent event = new ChainDisbandEvent(this, ChainDisbandEvent.DisbandReason.API_CALL);
        ShackledTogether.getInstance().getServer().getPluginManager().callEvent(event);
        nuke();
    }

    public boolean hasCustomConfiguration() {
        return customConfiguration;
    }

    private void nuke() {
        task.cancel();
        chainConfiguration.getChainVisualization().halt();
        chains.remove(this);
    }

    private void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (PlayerPair playerPair : playerPairs) {
                    if (chainConfiguration.hasChainCollision()) {
                        directionTracker.tick();
                        hitScanPhase(playerPair);
                    }
                    mergePhase(playerPair);
                    pullPhase(playerPair);
                }
                chainConfiguration.getChainVisualization().tick();
            }
        }.runTaskTimer(ShackledTogether.getInstance(), 0, 0);
    }

    private void pairPlayers() {
        playerPairs.clear();
        if (!hasEnoughParticipants()) {
            nuke();
            return;
        }
        for (int i = 0; i < players.size() - 1; i++) {
            playerPairs.add(new PlayerPair(players.get(i), players.get(i + 1)));
        }
    }

    private Location waist(Player player) {
        return player.getLocation().clone().add(0, 1, 0); // This should be a config?
    }

    private void hitScanPhase(PlayerPair playerPair) {
        if (!playerPair.hasPivotPoints()) {
            tryCreatePivotPoint(playerPair, waist(playerPair.getPlayerA()), waist(playerPair.getPlayerB()), PivotPoint.Position.FIRST_ENTRY);
        } else {
            tryCreatePivotPoint(playerPair, waist(playerPair.getPlayerA()), playerPair.getLastPivotPoint(playerPair.getPlayerA()).getHitScanCorner(), PivotPoint.Position.HEAD);
            tryCreatePivotPoint(playerPair, waist(playerPair.getPlayerB()), playerPair.getLastPivotPoint(playerPair.getPlayerB()).getHitScanCorner(), PivotPoint.Position.TAIL);
        }
    }

    private void tryCreatePivotPoint(PlayerPair playerPair, Location start, Location end, PivotPoint.Position position) {
        TwoWayResult twoWayResult = new TwoWayResult(VectorMath.blockBetween(start, end), VectorMath.blockBetween(end, start));
        if (!twoWayResult.isSuccessful()) {
            return;
        }
        Player playerA = playerPair.getPlayerA();
        Player playerB = playerPair.getPlayerB();
        DirectionTracker.Heading headingA = directionTracker.getHeading(playerA).getOpposite();
        DirectionTracker.Heading headingB = directionTracker.getHeading(playerB).getOpposite();
        BlockFace directionalBlockFaceA = headingA.toBlockFace();
        BlockFace directionalBlockFaceB = headingB.toBlockFace();
        if (twoWayResult.isSameBlock()) {
            if (twoWayResult.isOppositeBlockFaces()) {
                if (position.equals(PivotPoint.Position.FIRST_ENTRY)) {
                    Debug.print("A " + twoWayResult, "dBFA " + directionalBlockFaceA + " dBFB " + directionalBlockFaceB);
                    if (!directionalBlockFaceA.equals(BlockFace.SELF)) {
                        createPivotPoint(playerPair, twoWayResult.getHitBlock(), twoWayResult.getBlockFaceA(), directionalBlockFaceA, PivotPoint.Position.FIRST_ENTRY);
                    }
                    if (playerPair.hasPivotPoints()) {
                        if (!directionalBlockFaceB.equals(BlockFace.SELF)) {
                            createPivotPoint(playerPair, twoWayResult.getHitBlock(), twoWayResult.getBlockFaceB(), directionalBlockFaceB, PivotPoint.Position.TAIL);
                        }
                    } else {
                        createPivotPoint(playerPair, twoWayResult.getHitBlock(), twoWayResult.getBlockFaceB(), directionalBlockFaceB, PivotPoint.Position.FIRST_ENTRY);
                    }
                } else {
                    Player player = position == PivotPoint.Position.TAIL ? playerPair.getPlayerB() : playerPair.getPlayerA();
                    DirectionTracker.Heading heading = directionTracker.getHeading(player).getOpposite();
                    BlockFace blockFace = heading.toBlockFace();
                    Debug.print("B " + twoWayResult, "BF " + blockFace);
                    if (blockFace.equals(BlockFace.SELF)) {
                        return;
                    }
                    if (position.equals(PivotPoint.Position.TAIL)) {
                        createPivotPoint(playerPair, twoWayResult.getHitBlock(), twoWayResult.getBlockFaceB(), blockFace, position);
                    } else {
                        createPivotPoint(playerPair, twoWayResult.getHitBlock(), twoWayResult.getBlockFaceA(), blockFace, position);
                    }


                }
            } else {
                Debug.print("C " + twoWayResult);
                createPivotPoint(playerPair, twoWayResult.getHitBlock(), twoWayResult.getBlockFaceA(), twoWayResult.getBlockFaceB(), position);
            }
        } else {
            if (twoWayResult.isOppositeBlockFaces()) {
                if (position.equals(PivotPoint.Position.FIRST_ENTRY)) {
                    Debug.print("D " + twoWayResult, "dBFA " + directionalBlockFaceA + " dBFB " + directionalBlockFaceB);
                    if (!directionalBlockFaceA.equals(BlockFace.SELF)) {
                        createPivotPoint(playerPair, twoWayResult.getHitBlockA(), twoWayResult.getBlockFaceA(), directionalBlockFaceA, PivotPoint.Position.FIRST_ENTRY);
                    }
                    if (playerPair.hasPivotPoints()) {
                        if (!directionalBlockFaceB.equals(BlockFace.SELF)) {
                            createPivotPoint(playerPair, twoWayResult.getHitBlockB(), twoWayResult.getBlockFaceB(), directionalBlockFaceB, PivotPoint.Position.TAIL);
                        }
                    } else {
                        createPivotPoint(playerPair, twoWayResult.getHitBlockB(), twoWayResult.getBlockFaceB(), directionalBlockFaceB, PivotPoint.Position.FIRST_ENTRY);
                    }
                } else {
                    Debug.print("E" + twoWayResult, "dBFA " + directionalBlockFaceA + " dBFB " + directionalBlockFaceB);
                    createPivotPoint(playerPair, twoWayResult.getHitBlockA(), twoWayResult.getBlockFaceA(), directionalBlockFaceA, position);
                    createPivotPoint(playerPair, twoWayResult.getHitBlockB(), twoWayResult.getBlockFaceB(), directionalBlockFaceB, position);
                }
            } else {
                if (position.equals(PivotPoint.Position.FIRST_ENTRY)) {
                    Debug.print("F " + twoWayResult);
                    createPivotPoint(playerPair, twoWayResult.getHitBlockA(), twoWayResult.getBlockFaceA(), twoWayResult.getBlockFaceB(), PivotPoint.Position.FIRST_ENTRY);
                } else {
                    Debug.print("G " + twoWayResult);
                    createPivotPoint(playerPair, twoWayResult.getHitBlockA(), twoWayResult.getBlockFaceA(), twoWayResult.getBlockFaceB(), position);
                }
            }
        }
    }

    private void createPivotPoint(PlayerPair playerPair, Block block, BlockFace blockFaceA, BlockFace blockFaceB, PivotPoint.Position position) {
        Player initiator = position == PivotPoint.Position.TAIL ? playerPair.getPlayerB() : playerPair.getPlayerA();

        //EXPERIMENTAL FIX, they don't usually go so fast horizontally so I don't we need to handle it
        if (blockFaceA.equals(BlockFace.UP) || blockFaceB.equals(BlockFace.UP) || blockFaceA.equals(BlockFace.DOWN) || blockFaceB.equals(BlockFace.DOWN)) {
            World world = block.getWorld();
            int maxY = world.getMaxHeight() - 1;

            Block current = block;

            while (current.getY() < maxY) {
                Block above = current.getRelative(BlockFace.UP);
                if (!VectorMath.valid(above)) {
                    break;
                }
                Debug.print("Wait theres more :D");
                current = above;
            }
            block = current;
        }

        PivotPoint pivotPoint = new PivotPoint(block, blockFaceA, blockFaceB, initiator);
        if (!pivotPoint.isSuccesful()) {
            return;
        }
        if (!position.equals(PivotPoint.Position.FIRST_ENTRY)) {
            if (pivotPoint.equals(playerPair.getLastPivotPoint(initiator))) {
                Debug.print("Tried to create duplicate pivot point");
                return;
            }
        }


        //Experimental mess
        BlockFace fixedBlockFace = null;
        if (pivotPoint.isObstructed()) {
            Debug.severe("Pivot point obstructed, using directions: " + initiator.getName() + " moving towards: " + directionTracker.getHeading(initiator) + " and " + playerPair.theOtherPlayer(initiator).getName() + " moving towards: " + directionTracker.getHeading(playerPair.theOtherPlayer(initiator)));
            if (position.equals(PivotPoint.Position.FIRST_ENTRY)) {
                if (!directionTracker.getHeading(initiator).equals(DirectionTracker.Heading.UNDEFINED)) {
                    fixedBlockFace = directionTracker.getHeading(initiator).getOpposite().toBlockFace();
                    pivotPoint = new PivotPoint(block, fixedBlockFace, blockFaceB, initiator);
                    if (!pivotPoint.isSuccesful() || pivotPoint.isObstructed() || hasLos(playerPair, pivotPoint, position)) {
                        if (!directionTracker.getHeading(playerPair.theOtherPlayer(initiator)).equals(DirectionTracker.Heading.UNDEFINED)) {
                            fixedBlockFace = directionTracker.getHeading(playerPair.theOtherPlayer(initiator)).getOpposite().toBlockFace();
                            pivotPoint = new PivotPoint(block, blockFaceA, fixedBlockFace, initiator);
                        }
                    } else {
                        return;
                    }
                } else {
                    if (!directionTracker.getHeading(playerPair.theOtherPlayer(initiator)).equals(DirectionTracker.Heading.UNDEFINED)) {
                        fixedBlockFace = directionTracker.getHeading(playerPair.theOtherPlayer(initiator)).getOpposite().toBlockFace();
                        pivotPoint = new PivotPoint(block, blockFaceA, fixedBlockFace, initiator);
                    }
                }

            } else {
                if (position.equals(PivotPoint.Position.HEAD)) {
                    fixedBlockFace = directionTracker.getHeading(initiator).getOpposite().toBlockFace();
                    pivotPoint = new PivotPoint(block, fixedBlockFace, blockFaceB, initiator);
                } else {
                    fixedBlockFace = directionTracker.getHeading(playerPair.theOtherPlayer(initiator)).getOpposite().toBlockFace();
                    pivotPoint = new PivotPoint(block, blockFaceA, fixedBlockFace, initiator);
                }
            }
        }
        if (!pivotPoint.isSuccesful() || pivotPoint.isObstructed()) {
            return;
        }

        //Experimental, we should find a way to handle this better, but this should be a very rare edge case
        if (!hasLos(playerPair, pivotPoint, position)) {
            return;
        }

        Location end = playerPair.getSecondLastPivotPoint(initiator) == null ? waist(playerPair.theOtherPlayer(initiator)) : playerPair.getSecondLastPivotPoint(initiator).getHitScanCorner();
        try {
            setPivotPointSign(pivotPoint, waist(initiator), end);
        } catch (Exception e) {
            return;
        }
        if (position == PivotPoint.Position.TAIL) {
            playerPair.getPivotPoints().addLast(pivotPoint);
        } else {
            playerPair.getPivotPoints().addFirst(pivotPoint);
        }
        Debug.print(position + " " + pivotPoint);
        chainConfiguration.getChainVisualization().addPivotPoint(playerPair, pivotPoint, position);
    }

    private boolean hasLos(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position) {
        if (position.equals(PivotPoint.Position.HEAD)) {
            if (VectorMath.blockBetween(pivotPoint.getLosCorner(), waist(playerPair.getPlayerA())) != null || VectorMath.blockBetween(pivotPoint.getLosCorner(), playerPair.getLastPivotPoint(playerPair.getPlayerA()).getLosCorner()) != null) {
                Debug.severe("Fail LOS A");
                return false;
            }
        } else if (position.equals(PivotPoint.Position.TAIL)) {
            if (VectorMath.blockBetween(pivotPoint.getLosCorner(), waist(playerPair.getPlayerB())) != null || VectorMath.blockBetween(pivotPoint.getLosCorner(), playerPair.getLastPivotPoint(playerPair.getPlayerB()).getLosCorner()) != null) {
                Debug.severe("Fail LOS B");
                return false;
            }
        } else if (position.equals(PivotPoint.Position.FIRST_ENTRY)) {
            if (VectorMath.blockBetween(pivotPoint.getLosCorner(), waist(playerPair.getPlayerA())) != null && VectorMath.blockBetween(pivotPoint.getLosCorner(), waist(playerPair.getPlayerB())) != null) {
                Debug.severe("Fail LOS FE");
                return false;
            }
        }
        return true;
    }

    private void setPivotPointSign(PivotPoint pivotPoint, Location start, Location end) {
        boolean positiveSign;
        if (pivotPoint.isHorizontal()) {
            positiveSign = VectorMath.calculateSignedHorizontalAngle(pivotPoint.getHitScanCorner(), start, end) > 0;
        } else {
            positiveSign = VectorMath.calculateSignedVerticalAngle(pivotPoint.getHitScanCorner(), start, end, pivotPoint.getCardinalDirection()) > 0;
        }
        pivotPoint.setStartedAsPositiveSign(positiveSign);
    }

    private void mergePhase(PlayerPair playerPair) {
        if (playerPair.getPivotPoints().size() == 1) {
            tryMergeSinglePivotPoint(playerPair);
        } else if (playerPair.getPivotPoints().size() > 1) {
            tryMergePivotPoint(playerPair, playerPair.getLastPivotPoint(playerPair.getPlayerA()), PivotPoint.Position.HEAD);
            if (playerPair.getPivotPoints().size() > 1) {
                tryMergePivotPoint(playerPair, playerPair.getLastPivotPoint(playerPair.getPlayerB()), PivotPoint.Position.TAIL);
            }
        }
    }

    private void tryMergeSinglePivotPoint(PlayerPair playerPair) {
        PivotPoint pivotPoint = playerPair.getPivotPoints().getFirst();
        if (shouldRemovePivot(pivotPoint, waist(pivotPoint.getInitiator()), waist(playerPair.theOtherPlayer(pivotPoint.getInitiator())))) {
            Debug.print("Merging pivotpoint FIRST_ENTRY\n" + playerPair.getPivotPoints().getFirst().toString());
            playerPair.getPivotPoints().removeFirst();
            chainConfiguration.getChainVisualization().removePivotPoint(playerPair, pivotPoint, PivotPoint.Position.FIRST_ENTRY);
        }
    }

    private void tryMergePivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position) {
        Player forgiver = position == PivotPoint.Position.TAIL ? playerPair.getPlayerB() : playerPair.getPlayerA();
        Location locationA;
        Location locationB;
        if (pivotPoint.getInitiator().equals(forgiver)) {
            locationA = waist(forgiver);
            locationB = playerPair.getSecondLastPivotPoint(forgiver).getHitScanCorner();
        } else {
            locationA = playerPair.getSecondLastPivotPoint(forgiver).getHitScanCorner();
            locationB = waist(forgiver);
        }
        if (shouldRemovePivot(pivotPoint, locationA, locationB)) {
            if (position.equals(PivotPoint.Position.TAIL)) {
                Debug.print("Merging pivotpoint TAIL\n" + playerPair.getPivotPoints().getLast().toString());
                playerPair.getPivotPoints().removeLast();
            } else {
                Debug.print("Merging pivotpoint HEAD\n" + playerPair.getPivotPoints().getFirst().toString());
                playerPair.getPivotPoints().removeFirst();
            }
            chainConfiguration.getChainVisualization().removePivotPoint(playerPair, pivotPoint, position);
        }
    }

    private boolean shouldRemovePivot(PivotPoint pivotPoint, Location locationA, Location locationB) {
        if (pivotPoint.getBlock() == null || pivotPoint.getBlock().getType().equals(Material.AIR)) {
            return true;
        }
        if (pivotPoint.isHorizontal()) {
            return pivotPoint.isStartedAsPositiveSign() ? VectorMath.calculateSignedHorizontalAngle(pivotPoint.getHitScanCorner(), locationA, locationB) < 0 : VectorMath.calculateSignedHorizontalAngle(pivotPoint.getHitScanCorner(), locationA, locationB) > 0;
        } else {
            return pivotPoint.isStartedAsPositiveSign() ? VectorMath.calculateSignedVerticalAngle(pivotPoint.getHitScanCorner(), locationA, locationB, pivotPoint.getCardinalDirection()) < 0 : VectorMath.calculateSignedVerticalAngle(pivotPoint.getHitScanCorner(), locationA, locationB, pivotPoint.getCardinalDirection()) > 0;
        }
    }

    private void pullPhase(PlayerPair playerPair) {
        Player playerA = playerPair.getPlayerA();
        Player playerB = playerPair.getPlayerB();

        Vector playerAPos = playerA.getLocation().toVector();
        Vector playerBPos = playerB.getLocation().toVector();

        Vector pAPullLocationVector = playerPair.hasPivotPoints() ? playerPair.getLastPivotPoint(playerA).getPullCorner().toVector() : playerBPos;
        Vector pBPullLocationVector = playerPair.hasPivotPoints() ? playerPair.getLastPivotPoint(playerB).getPullCorner().toVector() : playerAPos;

        Vector vectorToALocation = pAPullLocationVector.clone().subtract(playerAPos.clone());
        Vector vectorToBLocation = pBPullLocationVector.clone().subtract(playerBPos.clone());

        double currentDistance = playerPair.distance();
        double maxDistance = chainConfiguration.getChainLength() + chainConfiguration.getChainStretch();

        if (currentDistance > maxDistance) {

            double forceMagnitude = chainConfiguration.getChainElasticity() * (currentDistance - chainConfiguration.getChainLength());

            Vector forceA = vectorToALocation.normalize().multiply(forceMagnitude);
            Vector forceB = vectorToBLocation.normalize().multiply(forceMagnitude);

            playerA.setVelocity(playerA.getVelocity().clone().add(forceA));
            playerB.setVelocity(playerB.getVelocity().clone().add(forceB));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chain that = (Chain) o;

        return (Objects.equals(uuid, that.getUuid()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
