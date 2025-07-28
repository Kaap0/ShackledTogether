package gg.kaapo.shackledtogether.events;

import gg.kaapo.shackledtogether.chain.Chain;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChainLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Chain chain;
    private final LeaveReason leaveReason;

    public ChainLeaveEvent(Player player, Chain chain, LeaveReason leaveReason) {
        this.player = player;
        this.chain = chain;
        this.leaveReason = leaveReason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Chain getChain() {
        return chain;
    }

    public LeaveReason getReason() {
        return leaveReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum LeaveReason {
        DEATH, DISCONNECT, TELEPORTATION, COMMAND
    }
}