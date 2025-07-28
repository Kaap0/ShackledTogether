package gg.kaapo.shackledtogether.chain.visualizations.particle;

import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.chain.PivotPoint;
import gg.kaapo.shackledtogether.chain.PlayerPair;
import gg.kaapo.shackledtogether.chain.visualizations.ChainVisualization;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParticleImplementation extends ChainVisualization {

    //à la dtzt :)

    private final ShackledTogether shackledTogether = ShackledTogether.getInstance();
    private final Map<UUID, List<Player>> playerChains = new HashMap<>();
    int maxParticles;
    int pStartRed;
    int pStartGreen;
    int pStartBlue;
    int pEndRed;
    int pEndGreen;
    int pEndBlue;
    float pSize;

    @Override
    public void init(List<Player> players, List<PlayerPair> playerPairs) {
        maxParticles = shackledTogether.getConfig().getInt("chain.particleEffect.max-particles", 50);
        pStartRed = shackledTogether.getConfig().getInt("chain.particleEffect.start_color.red", 128);
        pStartGreen = shackledTogether.getConfig().getInt("chain.particleEffect.start_color.green", 128);
        pStartBlue = shackledTogether.getConfig().getInt("chain.particleEffect.start_color.blue", 128);
        pEndRed = shackledTogether.getConfig().getInt("chain.particleEffect.end_color.red", 0);
        pEndGreen = shackledTogether.getConfig().getInt("chain.particleEffect.end_color.green", 0);
        pEndBlue = shackledTogether.getConfig().getInt("chain.particleEffect.end_color.blue", 0);
        pSize = (float) shackledTogether.getConfig().getDouble("chain.particleEffect.size", 1.0);

        playerChains.clear();

        for (Player player : players) {
            playerChains.put(player.getUniqueId(), players);
        }
    }


    @Override
    public void tick() {
        for (Map.Entry<UUID, List<Player>> entry : playerChains.entrySet()) {
            UUID playerId = entry.getKey();
            Player player = Bukkit.getPlayer(playerId);

            if (player == null) continue;

            List<Player> chain = entry.getValue();
            for (int i = 0; i < chain.size() - 1; i++) {
                Player from = chain.get(i);
                Player to = chain.get(i + 1);
                drawParticles(from, to);
            }
        }
    }

    private void drawParticles(Player from, Player to) {
        double distance = from.getLocation().distance(to.getLocation());
        int numParticles = Math.min((int) distance * 10, maxParticles);

        // Make particles at center (0.9)
        double fromY = from.getLocation().getY() + 0.7;
        double toY = to.getLocation().getY() + 0.7;

        // Particle Settings From Config
        Color startColor = Color.fromRGB(pStartRed, pStartGreen, pStartBlue);
        Color endColor = Color.fromRGB(pEndRed, pEndGreen, pEndBlue);
        DustTransition dustTransition = new DustTransition(startColor, endColor, pSize);

        // Create all of the particles dependign on the config maxNumber of them
        for (int i = 0; i < numParticles; i++) {
            double progress = i / (double) numParticles;
            double x = from.getLocation().getX() + progress * (to.getLocation().getX() - from.getLocation().getX());
            double y = fromY + progress * (toY - fromY);
            double z = from.getLocation().getZ() + progress * (to.getLocation().getZ() - from.getLocation().getZ());
            from.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, x, y, z, 0, 0, 0, 0, 1, dustTransition);
        }
    }

    @Override
    public void halt() {
        playerChains.clear();
    }

    @Override
    public void addPlayer(Player player) {
        playerChains.put(player.getUniqueId(), players);
    }


    @Override
    public void removePlayer(Player player) {
        playerChains.remove(player.getUniqueId());
        for (List<Player> chain : playerChains.values()) {
            chain.remove(player);
        }
        init(players, playerPairs);
    }


    @Override
    public void addPivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position) {
        // TODO
    }

    @Override
    public void removePivotPoint(PlayerPair playerPair, PivotPoint pivotPoint, PivotPoint.Position position) {
        // TODO
    }
}
