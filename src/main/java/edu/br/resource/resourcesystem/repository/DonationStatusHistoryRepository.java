package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.DonationStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationStatusHistoryRepository extends JpaRepository<DonationStatusHistory, Integer> {
    List<DonationStatusHistory> findAllByDonationIdOrderByChangedAtAscIdAsc(Integer donationId);
}
