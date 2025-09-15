package app.tradeflows.api.market_data_service.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tf_product_histories")
@Entity
@Setter
@Getter
public class ProductHistory extends Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private Product product;
    private int buyLimit;
    private int sellLimit;
    private double lastTradedPrice;
    private double askPrice;
    private double bidPrice;
    private double maxShiftPrice;
}
