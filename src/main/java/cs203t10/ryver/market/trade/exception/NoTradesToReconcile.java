package cs203t10.ryver.market.trade.exception;

public class NoTradesToReconcile extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoTradesToReconcile(String symbol) {
        super(String.format("No more trades %s to reconcile", symbol));
    }

}

