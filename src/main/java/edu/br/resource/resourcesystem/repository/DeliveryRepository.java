package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.Delivery;
import edu.br.resource.resourcesystem.model.enums.DeliveryMethod;
import edu.br.resource.resourcesystem.model.enums.DeliveryStatus;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    Optional<Delivery> findByMatchId(Integer matchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Delivery d where d.match.id = :matchId")
    Optional<Delivery> findForUpdateByMatchId(@Param("matchId") Integer matchId);

    @Query(
            """
        select d from Delivery d join d.match m
        where d.method = :method and d.status in :deliveryStatuses and m.status in :matchStatuses
          and d.inPersonDeadline <= :deadline
        """)
    Slice<Delivery> findExpiredInPersonDeliveries(
            @Param("method") DeliveryMethod method,
            @Param("deliveryStatuses") Collection<DeliveryStatus> deliveryStatuses,
            @Param("matchStatuses") Collection<MatchStatus> matchStatuses,
            @Param("deadline") Instant deadline,
            Pageable pageable);

    @Query(
            """
        select d from Delivery d join d.match m
        where d.method = :method and d.status in :deliveryStatuses and m.status in :matchStatuses
          and d.shippingDeadline <= :deadline
        """)
    Slice<Delivery> findExpiredShipments(
            @Param("method") DeliveryMethod method,
            @Param("deliveryStatuses") Collection<DeliveryStatus> deliveryStatuses,
            @Param("matchStatuses") Collection<MatchStatus> matchStatuses,
            @Param("deadline") Instant deadline,
            Pageable pageable);
}
