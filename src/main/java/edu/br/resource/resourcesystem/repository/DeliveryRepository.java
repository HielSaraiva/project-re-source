package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.Delivery;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    Optional<Delivery> findByMatchId(Integer matchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Delivery d where d.match.id = :matchId")
    Optional<Delivery> findForUpdateByMatchId(@Param("matchId") Integer matchId);
}
