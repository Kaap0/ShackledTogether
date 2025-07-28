package gg.kaapo.shackledtogether.chain;

import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.events.ChainCreateEvent;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShackledTogetherAPI {

    /**
     * The ShackledTogetherAPI provides public access to all supported
     * features related to chain creation and querying in the ShackledTogether plugin.
     * <p>
     * This API is designed for developers building their own integrations.
     */

    private static final ShackledTogether shackledTogether = ShackledTogether.getInstance();

    /**
     * Returns an immutable list of all currently active chains.
     * <p>
     *
     * @return list of active {@link Chain} objects
     */
    public List<Chain> getChains() {
        return Collections.unmodifiableList(Chain.chains);
    }

    /**
     * Attempts to create a new chain from the given list of players with the specified configuration.
     * ChainConfiguration can be set null, then it will use and follow config.yml changes
     * <p>
     * Only players who are not already chained will be chained.
     * If fewer than two valid players are provided, creation fails.
     * Safety checks are made based on config.yml
     * If some players were skipped, the result is marked as partial.
     * Triggers a {@link ChainCreateEvent} on successful creation.
     * <p>
     *
     * @param players            list of players to chain
     * @param chainConfiguration chain behavior settings
     * @return {@link ChainCreationResult} containing status and chain (if created)
     */
    public ChainCreationResult createChain(List<Player> players, ChainConfiguration chainConfiguration) {


        List<Player> participants = checkSafeguards(players, chainConfiguration);
        boolean partial = !players.equals(participants);

        if (participants.size() < 2) {
            return new ChainCreationResult(ChainCreationResult.Result.NOT_ENOUGH_PLAYERS);
        }
        if (participants.size() > shackledTogether.getConfig().getInt("chain-max-players")) {
            return new ChainCreationResult(ChainCreationResult.Result.TOO_MANY_PLAYERS);
        }

        Chain chain = new Chain(participants, chainConfiguration);
        ChainCreateEvent chainCreateEvent = new ChainCreateEvent(chain);
        ShackledTogether.getInstance().getServer().getPluginManager().callEvent(chainCreateEvent);
        return partial ? new ChainCreationResult(ChainCreationResult.Result.PARTIAL, chain) : new ChainCreationResult(ChainCreationResult.Result.SUCCESS, chain);
    }

    /**
     * Returns the chain that the given player is part of, or {@code null} if they are not chained.
     *
     * @param player the player to check
     * @return the player's {@link Chain}, or null
     */
    public Chain getChain(Player player) {
        for (Chain chain : getChains()) {
            if (chain.contains(player)) {
                return chain;
            }
        }
        return null;
    }

    /**
     * Checks whether the given player is currently part of any chain.
     *
     * @param player the player to check
     * @return true if the player is chained, false otherwise
     */
    public boolean isChained(Player player) {
        return getChain(player) != null;
    }

    /**
     * Returns the chain shared by both players, returns null when they are not in same chain.
     *
     * @param a first player
     * @param b second player
     * @return shared {@link Chain}, or null if they are not in the same chain
     */
    public Chain getSameChain(Player a, Player b) {
        Chain chainA = getChain(a);
        Chain chainB = getChain(b);
        if (chainA != null && chainA.equals(chainB)) {
            return chainA;
        }
        return null;
    }

    /**
     * Checks whether two players are in the same chain.
     *
     * @param a first player
     * @param b second player
     * @return true if both players share the same chain, false otherwise
     */
    public boolean isSameChain(Player a, Player b) {
        return getSameChain(a, b) != null;
    }


    /**
     * Checks if it's safe to create a chain.
     *
     * @param players            list of players to chain
     * @param chainConfiguration chain behavior settings
     * @return {@link List<Player>} Returns list of players after safety checks.
     */
    public List<Player> checkSafeguards(List<Player> players, ChainConfiguration chainConfiguration) {
        ArrayList<Player> participants = new ArrayList<>();

        boolean checkDistance = shackledTogether.getConfig().getBoolean("safeguard-proximity");
        boolean checkLOS = shackledTogether.getConfig().getBoolean("safeguard-line-of-sight");

        double chainLength = chainConfiguration == null ? shackledTogether.getConfig().getDouble("chain-length") : chainConfiguration.getChainLength();
        double chainStretch = chainConfiguration == null ? shackledTogether.getConfig().getDouble("chain-stretch") : chainConfiguration.getChainStretch();

        World world = players.getFirst().getWorld();

        for (int i = 0; i < players.size(); i++) {
            Player current = players.get(i);

            if (i < players.size() - 1) {
                Player next = players.get(i + 1);

                if (checkDistance && current.getLocation().distance(next.getLocation()) > chainLength + chainStretch) {
                    break;
                }
                if (checkLOS && !current.hasLineOfSight(next)) {
                    break;
                }
            }

            if (!isChained(current) && current.getWorld().equals(world)) {
                participants.add(current);
            }
        }
        return participants;
    }


}
