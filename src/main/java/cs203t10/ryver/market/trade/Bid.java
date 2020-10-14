package cs203t10.ryver.market.trade;

public class Bid extends Trade {

    @Override
    public Type getType() {
        return Trade.Type.BID;
    }

}
