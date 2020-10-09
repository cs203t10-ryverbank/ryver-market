package cs203t10.ryver.market.stock.view;

import lombok.*;

@Data
public class StockView {

    private String symbol;

    private Double lastPrice;

    private Integer bidVolume;

    private Double bid;

    private Integer askVolume;

    private Double ask;
}

