package app.tradeflows.api.market_data_service.repositories;

import app.tradeflows.api.market_data_service.entities.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, String> {
    List<ProductHistory> findByProduct_IdOrderByCreatedAtAsc(@NonNull String id);

    List<ProductHistory> findByProduct_TickerNotNullOrderByCreatedAtAsc();


}
