package cs203t10.ryver.market.trade;

public class SellTrade extends Trade {

    @Override
    public Action getAction() {
        return Trade.Action.SELL;
    }

    public Double getAskPrice() {
        return getPrice();
    }

    public void setAskPrice(Double price) {
        setPrice(price);
    }

}
