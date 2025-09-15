package app.tradeflows.api.market_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Table(name = "tf_products")
@Entity
@Setter
@Getter
@NoArgsConstructor
public class Product extends Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(unique = true, nullable = false)
    private String ticker;
    private int buyLimit;
    private int sellLimit;
    private double lastTradedPrice;
    private double askPrice;
    private double bidPrice;
    private double maxShiftPrice;
    @Column(columnDefinition = "false")
    private boolean isTrading;

    public Product(String ticker, int buyLimit, int sellLimit, double lastTradedPrice, double askPrice, double bidPrice, double maxShiftPrice) {
        this.ticker = ticker;
        this.buyLimit = buyLimit;
        this.sellLimit = sellLimit;
        this.lastTradedPrice = lastTradedPrice;
        this.askPrice = askPrice;
        this.bidPrice = bidPrice;
        this.maxShiftPrice = maxShiftPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Product product = (Product) o;
        return buyLimit == product.buyLimit && sellLimit == product.sellLimit && Double.compare(lastTradedPrice, product.lastTradedPrice) == 0 && Double.compare(askPrice, product.askPrice) == 0 && Double.compare(bidPrice, product.bidPrice) == 0 && Double.compare(maxShiftPrice, product.maxShiftPrice) == 0 && isTrading == product.isTrading && Objects.equals(id, product.id) && Objects.equals(ticker, product.ticker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, ticker, buyLimit, sellLimit, lastTradedPrice, askPrice, bidPrice, maxShiftPrice, isTrading);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", ticker='" + ticker + '\'' +
                ", buyLimit=" + buyLimit +
                ", sellLimit=" + sellLimit +
                ", lastTradedPrice=" + lastTradedPrice +
                ", askPrice=" + askPrice +
                ", bidPrice=" + bidPrice +
                ", maxShiftPrice=" + maxShiftPrice +
                ", isTrading=" + isTrading +
                '}';
    }
}
