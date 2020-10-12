package cs203t10.ryver.market.trade;

public class BuyTrade extends Trade {

    @Override
    public Action getAction() {
        return Trade.Action.BUY;
    }

    public Double getBidPrice() {
        return getPrice();
    }

    public void setBidPrice(Double price) {
        setPrice(price);
    }

}
