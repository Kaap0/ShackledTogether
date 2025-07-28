package gg.kaapo.shackledtogether.chain.visualizations.lead;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.chain.PivotPoint;
import gg.kaapo.shackledtogether.chain.PlayerPair;
import gg.kaapo.shackledtogether.chain.visualizations.ChainVisualization;
import gg.kaapo.shackledtogether.chain.visualizations.lead.harness.Anchor;
import gg.kaapo.shackledtogether.chain.visualizations.lead.harness.Carabiner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class LeadImplementation extends ChainVisualization {

    private static final Scoreboard masterScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final ShackledTogether shackledTogether = ShackledTogether.getInstance();
    private final Map<Player, Anchor> belts = new HashMap<>();
    private final Map<PlayerPair, LinkedList<Anchor>> pitons = new HashMap<>();
    private PacketListener packetListener;

    public LeadImplementation() {
        Team masterTeam = masterScoreboard.getTeam("shackledtogether");
        if (masterTeam == null) {
            masterTeam = masterScoreboard.registerNewTeam("shackledtogether");
        }
        masterTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

    }

    public static Scoreboard getMasterScoreboard() {
        return masterScoreboard;
    }

    @Override
    public void init(List<Player> players, List<PlayerPair> playerPairs) {
        this.players = players;
        this.playerPairs = playerPairs;

        belts.clear();
        pitons.clear();
        players.forEach(player -> player.setScoreboard(masterScoreboard));
        players.forEach(player -> masterScoreboard.getTeam("shackledtogether").addEntry(player.getUniqueId().toString()));
        players.forEach(player -> belts.put(player, createAnchor(player.getLocation())));

        List<RuleBoundPacket> structure = structure();
        Bukkit.getOnlinePlayers().forEach(player -> structure.forEach(ruleBoundPacket -> send(ruleBoundPacket, player)));

        //THIS IS VERY INEFFICIENT ;( BUT FIXES A LOT OF THINGS ;)
        packetListener = new PacketAdapter(ShackledTogether.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                UUID uuid = event.getPacket().getUUIDs().read(0);
                EntityType entityType = Bukkit.getEntity(uuid).getType();

                if (entityType.equals(EntityType.RABBIT)) {
                    refreshPackets(event.getPlayer());
                }
            }
        };
        ShackledTogether.getInstance().getProtocolManager().addPacketListener(packetListener);

    }

    @Override
    public void tick() {
        for (Player player : belts.keySet()) {
            for (Carabiner carabiner : belts.get(player).getCarabiners()) {
                carabiner.getCarabiner().teleport(player.getLocation().clone().add(-Math.sin(Math.toRadians(player.getLocation().getYaw() + 180)) * 0.3, 0.8, Math.cos(Math.toRadians(player.getLocation().getYaw() + 180)) * 0.3));
            }
        }
    }

    @Override
    public void halt() {
        ShackledTogether.getInstance().getProtocolManager().removePacketListener(packetListener);
        for (Anchor anchor : belts.values()) {
            for (Carabiner carabiner : anchor.getCarabiners()) {
                carabiner.nuke();
            }
        }
        for (LinkedList<Anchor> anchorLinkedList : pitons.values()) {
            for (Anchor anchor : anchorLinkedList) {
                for (Carabiner carabiner : anchor.getCarabiners()) {
                    carabiner.nuke();
                }
            }
        }
        for (Player player : players) {
            masterScoreboard.getTeam("shackledtogether").removeEntry(player.getUniqueId().toString());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        pitons.clear();
        belts.clear();
    }

    @Override
    public void addPlayer(Player player) {
    }

    @Override
    public void removePlayer(Player player) {
        halt();
        init(players, playerPairs);
    }

    @Override
    public void addPivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position) {
        Location midYawPointLocation = pivotPoint.getVisualCorner();
        midYawPointLocation.setYaw(getMidYaw(pivotPoint.getBlockFaceA(), pivotPoint.getBlockFaceB()));
        Anchor anchor = createAnchor(midYawPointLocation);
        if (!pitons.containsKey(playerPair)) {
            pitons.put(playerPair, new LinkedList<>());
        }
        if (position.equals(PivotPoint.Position.FIRST_ENTRY)) {
            pitons.get(playerPair).add(anchor);
            broadcast(new RuleBoundPacket(detach(belts.get(playerPair.getPlayerA()).getCarabiner(1))));
            broadcast(new RuleBoundPacket(detach(belts.get(playerPair.getPlayerB()).getCarabiner(0))));
            broadcast(new RuleBoundPacket(attach(anchor.getCarabiner(0), playerPair.getPlayerA())));
            broadcast(new RuleBoundPacket(attach(anchor.getCarabiner(1), playerPair.getPlayerB())));
        } else if (position.equals(PivotPoint.Position.HEAD)) {
            broadcast(new RuleBoundPacket(detach(pitons.get(playerPair).getFirst().getCarabiner(0))));
            broadcast(new RuleBoundPacket(attach(pitons.get(playerPair).getFirst().getCarabiner(0), anchor.getCarabiner(0))));
            pitons.get(playerPair).addFirst(anchor);
            broadcast(new RuleBoundPacket(attach(anchor.getCarabiner(0), playerPair.getPlayerA())));
        } else if (position.equals(PivotPoint.Position.TAIL)) {
            broadcast(new RuleBoundPacket(detach(pitons.get(playerPair).getLast().getCarabiner(1))));
            pitons.get(playerPair).addLast(anchor);
            broadcast(new RuleBoundPacket(attach(anchor.getCarabiner(0), pitons.get(playerPair).get(pitons.get(playerPair).size() - 2).getCarabiner(0))));
            broadcast(new RuleBoundPacket(attach(anchor.getCarabiner(1), playerPair.getPlayerB())));
        }
    }

    @Override
    public void removePivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position) {
        Anchor anchor = position.equals(PivotPoint.Position.FIRST_ENTRY) || position.equals(PivotPoint.Position.HEAD) ? pitons.get(playerPair).getFirst() : pitons.get(playerPair).getLast();
        anchor.getCarabiners().forEach(carabiner -> carabiner.nuke());
        if (position.equals(PivotPoint.Position.FIRST_ENTRY)) {
            pitons.get(playerPair).removeFirst();
            broadcast(new RuleBoundPacket(attach(belts.get(playerPair.getPlayerB()).getCarabiner(0), playerPair.getPlayerA()), RuleBoundPacket.PacketRule.EXEMPTED, playerPair.getPlayerB()));
            broadcast(new RuleBoundPacket(attach(belts.get(playerPair.getPlayerA()).getCarabiner(1), playerPair.getPlayerB()), RuleBoundPacket.PacketRule.PERSONAL, playerPair.getPlayerB()));
        } else if (position.equals(PivotPoint.Position.HEAD)) {
            pitons.get(playerPair).removeFirst();
            broadcast(new RuleBoundPacket(attach(pitons.get(playerPair).getFirst().getCarabiner(0), playerPair.getPlayerA())));
        } else if (position.equals(PivotPoint.Position.TAIL)) {
            pitons.get(playerPair).removeLast();
            broadcast(new RuleBoundPacket(attach(pitons.get(playerPair).getLast().getCarabiner(1), playerPair.getPlayerB())));
        }
    }

    public void refreshPackets(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                structure().forEach(ruleBoundPacket -> send(ruleBoundPacket, player));
            }
        }.runTaskLater(ShackledTogether.getInstance(), 1);
    }

    private Carabiner createCarabiner(Location location) {
        return new Carabiner(location);
    }

    private Anchor createAnchor(Location location) {
        return new Anchor(createCarabiner(location), createCarabiner(location));
    }

    private PacketContainer attach(Entity attached, Entity holder) {
        PacketContainer attachPacket = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
        attachPacket.getIntegers().write(0, attached.getEntityId()).write(1, holder.getEntityId());
        return attachPacket;
    }

    private PacketContainer detach(Entity attached) {
        PacketContainer attachPacket = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
        attachPacket.getIntegers().write(0, attached.getEntityId()).write(1, -1);
        return attachPacket;
    }

    private void broadcast(RuleBoundPacket ruleBoundPacket) {
        switch (ruleBoundPacket.getPacketRule()) {
            case EXEMPTED: {
                List<Player> recipients = new ArrayList<>(Bukkit.getOnlinePlayers());
                recipients.remove(ruleBoundPacket.getPlayer());
                for (Player player : recipients) {
                    send(ruleBoundPacket.getPacketContainer(), player);
                }
                break;
            }
            case GLOBAL: {
                List<Player> recipients = new ArrayList<>(Bukkit.getOnlinePlayers());
                for (Player player : recipients) {
                    send(ruleBoundPacket.getPacketContainer(), player);
                }
                break;
            }
            case PERSONAL: {
                send(ruleBoundPacket.getPacketContainer(), ruleBoundPacket.getPlayer());
                break;
            }
        }
    }

    private void send(RuleBoundPacket ruleBoundPacket, Player player) {
        switch (ruleBoundPacket.getPacketRule()) {
            case EXEMPTED: {
                if (!ruleBoundPacket.getPlayer().equals(player)) {
                    send(ruleBoundPacket.getPacketContainer(), player);
                }
                break;
            }
            case GLOBAL: {
                send(ruleBoundPacket.getPacketContainer(), player);
                break;
            }
            case PERSONAL: {
                if (ruleBoundPacket.getPlayer().equals(player)) {
                    send(ruleBoundPacket.getPacketContainer(), player);
                }
                break;
            }
        }
    }

    private void send(PacketContainer packetContainer, Player player) {
        shackledTogether.getProtocolManager().sendServerPacket(player, packetContainer);
    }

    private List<RuleBoundPacket> structure() {
        List<RuleBoundPacket> packets = new ArrayList<>();

        if (belts.isEmpty()) {
            return packets;
        }

        for (PlayerPair playerPair : playerPairs) {
            if (pitons.get(playerPair) == null || pitons.get(playerPair).isEmpty()) {
                packets.add(new RuleBoundPacket(attach(belts.get(playerPair.getPlayerB()).getCarabiner(0), playerPair.getPlayerA()), RuleBoundPacket.PacketRule.EXEMPTED, playerPair.getPlayerB()));
                packets.add(new RuleBoundPacket(attach(belts.get(playerPair.getPlayerA()).getCarabiner(1), playerPair.getPlayerB()), RuleBoundPacket.PacketRule.PERSONAL, playerPair.getPlayerB()));
            } else {
                packets.add(new RuleBoundPacket(attach(pitons.get(playerPair).getFirst().getCarabiner(0), playerPair.getPlayerA())));
                packets.add(new RuleBoundPacket(attach(pitons.get(playerPair).getLast().getCarabiner(1), playerPair.getPlayerB())));
                if (pitons.get(playerPair).size() > 1) {
                    Iterator<Anchor> iterator = pitons.get(playerPair).iterator();
                    Anchor current = null;
                    while (iterator.hasNext()) {
                        Anchor next = iterator.next();
                        if (current != null) {
                            packets.add(new RuleBoundPacket(attach(next.getCarabiner(0), current.getCarabiner(0))));
                        }
                        current = next;
                        if (!iterator.hasNext()) {
                            break;
                        }
                    }
                }
            }
        }
        return packets;
    }

    private float getMidYaw(BlockFace face1, BlockFace face2) {
        float yaw1 = getYawFromFace(face1);
        float yaw2 = getYawFromFace(face2);

        float delta = ((yaw2 - yaw1 + 540) % 360) - 180;
        return yaw1 + delta / 2;
    }

    private float getYawFromFace(BlockFace face) {
        switch (face) {
            case NORTH:
                return 180f;
            case EAST:
                return -90f;
            case SOUTH:
                return 0f;
            case WEST:
                return 90f;
            default:
                return 0f;
        }
    }
}
