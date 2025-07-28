package gg.kaapo.shackledtogether.chain;

import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.chain.visualizations.ChainVisualization;
import gg.kaapo.shackledtogether.chain.visualizations.disabled.DisabledImplementation;
import gg.kaapo.shackledtogether.chain.visualizations.lead.LeadImplementation;
import gg.kaapo.shackledtogether.chain.visualizations.particle.ParticleImplementation;
import org.bukkit.configuration.file.FileConfiguration;

public class ChainConfiguration {

    private final ChainVisualization chainVisualization;

    private boolean pullMechanic;
    private PullMethod pullMethod;
    private double pullRange;
    private double pullEfficiency;

    private boolean chainCollision;

    private double chainLength;
    private double chainElasticity;
    private double chainStretch;

    public ChainConfiguration() {
        FileConfiguration config = ShackledTogether.getInstance().getConfig();

        String chainVisualizationString = config.getString("chain-visualization");
        switch (chainVisualizationString.toUpperCase()) {
            case "LEAD":
                if (ShackledTogether.getInstance().getProtocolManager() == null) {
                    ShackledTogether.getInstance().getLogger().severe("ProtocolLib not Available, LEAD Visualization can't be used.");
                    throw new RuntimeException();
                }
                chainVisualization = new LeadImplementation();
                break;
            case "PARTICLE":
                chainVisualization = new ParticleImplementation();
                break;
            default:
                ShackledTogether.getInstance().getLogger().severe("Invalid Visualization specified in config.yml defaulting to DISABLED");
                chainVisualization = new DisabledImplementation();
                break;
        }

        chainCollision = config.getBoolean("chain-collision");

        pullMechanic = config.getBoolean("pull-mechanic");
        pullMethod = PullMethod.fromString(config.getString("pull-method"));
        pullRange = config.getDouble("pull-range");
        pullEfficiency = config.getDouble("pull-efficiency");

        chainLength = config.getDouble("chain-length");
        chainElasticity = config.getDouble("chain-elasticity");
        chainStretch = config.getDouble("chain-stretch");
    }

    public ChainVisualization getChainVisualization() {
        return chainVisualization;
    }

    public boolean hasPullMechanic() {
        return pullMechanic;
    }

    public double getPullRange() {
        return pullRange;
    }

    public void setPullRange(double pullRange) {
        this.pullRange = pullRange;
    }

    public double getPullEfficiency() {
        return pullEfficiency;
    }

    public void setPullEfficiency(double pullEfficiency) {
        this.pullEfficiency = pullEfficiency;
    }

    public boolean hasChainCollision() {
        return chainCollision;
    }

    public double getChainLength() {
        return chainLength;
    }

    public void setChainLength(double chainLength) {
        this.chainLength = chainLength;
    }

    public double getChainElasticity() {
        return chainElasticity;
    }

    public void setChainElasticity(double chainElasticity) {
        this.chainElasticity = chainElasticity;
    }

    public double getChainStretch() {
        return chainStretch;
    }

    public void setChainStretch(double chainStretch) {
        this.chainStretch = chainStretch;
    }

    public void setPullMechanic(boolean pullMechanic) {
        this.pullMechanic = pullMechanic;
    }

    public void setChainCollision(boolean chainCollision) {
        this.chainCollision = chainCollision;
    }

    public PullMethod getPullMethod() {
        return pullMethod;
    }

    public void setPullMethod(PullMethod pullMethod) {
        this.pullMethod = pullMethod;
    }

    public enum PullMethod {
        LEFT_CLICK,
        RIGHT_CLICK;

        public static PullMethod fromString(String input) {
            if (input == null) return null;

            switch (input.toUpperCase()) {
                case "LEFT_CLICK":
                    return LEFT_CLICK;
                case "RIGHT_CLICK":
                    return RIGHT_CLICK;
                default:
                    throw new IllegalArgumentException("Unknown PullMethod: " + input);
            }
        }
    }
}