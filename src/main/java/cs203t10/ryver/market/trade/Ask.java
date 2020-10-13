package cs203t10.ryver.market.trade;

public class Ask extends Trade {

    @Override
    public Type getType() {
        return Trade.Type.ASK;
    }

}
