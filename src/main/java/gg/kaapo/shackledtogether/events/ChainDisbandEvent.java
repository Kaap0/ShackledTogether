package gg.kaapo.shackledtogether.events;

import gg.kaapo.shackledtogether.chain.Chain;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChainDisbandEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Chain chain;
    private final DisbandReason disbandReason;

    public ChainDisbandEvent(Chain chain, DisbandReason disbandReason) {
        this.chain = chain;
        this.disbandReason = disbandReason;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Chain getChain() {
        return chain;
    }

    public DisbandReason getReason() {
        return disbandReason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public enum DisbandReason {
        API_CALL, NOT_ENOUGH_PLAYERS
    }
}