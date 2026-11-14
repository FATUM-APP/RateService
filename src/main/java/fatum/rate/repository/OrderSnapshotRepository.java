package fatum.rate.repository;

import fatum.rate.model.OrderSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSnapshotRepository extends JpaRepository<OrderSnapshot, String> {
    boolean existsByServiceIdAndCompletedTrue(String serviceId);

}
