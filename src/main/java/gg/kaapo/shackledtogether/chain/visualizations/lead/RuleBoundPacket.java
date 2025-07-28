package gg.kaapo.shackledtogether.chain.visualizations.lead;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

public class RuleBoundPacket {

    private final PacketContainer packetContainer;
    private final Player player;
    private final PacketRule packetRule;

    public RuleBoundPacket(PacketContainer packetContainer, PacketRule packetRule, Player player) {
        this.packetContainer = packetContainer;
        this.player = player;
        this.packetRule = packetRule;
    }

    public RuleBoundPacket(PacketContainer packetContainer) {
        this.packetContainer = packetContainer;
        this.player = null;
        this.packetRule = PacketRule.GLOBAL;
    }

    public PacketContainer getPacketContainer() {
        return packetContainer;
    }

    public Player getPlayer() {
        return player;
    }

    public PacketRule getPacketRule() {
        return packetRule;
    }

    public enum PacketRule {
        PERSONAL, GLOBAL, EXEMPTED
    }
}
