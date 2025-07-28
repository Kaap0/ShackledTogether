package gg.kaapo.shackledtogether.chain;

public class ChainCreationResult {

    private final Result result;
    private final Chain chain;

    public ChainCreationResult(Result result, Chain chain) {
        this.result = result;
        this.chain = chain;
    }

    public ChainCreationResult(Result result) {
        this(result, null);
    }

    public Result getResult() {
        return result;
    }

    public Chain getChain() {
        return chain;
    }

    public enum Result {
        SUCCESS, PARTIAL, NOT_ENOUGH_PLAYERS, TOO_MANY_PLAYERS,
    }

}
