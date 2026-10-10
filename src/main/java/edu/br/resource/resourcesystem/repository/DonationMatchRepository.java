package edu.br.resource.resourcesystem.repository;

import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.enums.MatchStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationMatchRepository
        extends JpaRepository<DonationMatch, Integer>, JpaSpecificationExecutor<DonationMatch> {
    @Override
    @EntityGraph(
            attributePaths = {
                "donation.donor",
                "donation.donationPackage.item.itemType",
                "necessity.institution",
                "necessity.itemType"
            })
    Page<DonationMatch> findAll(Specification<DonationMatch> specification, Pageable pageable);

    @EntityGraph(
            attributePaths = {
                "donation",
                "donation.donor",
                "donation.donationPackage.item.itemType",
                "necessity",
                "necessity.itemType",
                "necessity.institution"
            })
    Optional<DonationMatch> findByProtocolAndDonationDonorId(String protocol, Integer donorId);

    @EntityGraph(
            attributePaths = {
                "donation",
                "donation.donor",
                "donation.donationPackage.item.itemType",
                "necessity",
                "necessity.itemType",
                "necessity.institution"
            })
    Optional<DonationMatch> findByProtocolAndNecessityInstitutionId(
            String protocol, Integer institutionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from DonationMatch m where m.protocol = :protocol")
    Optional<DonationMatch> findForUpdate(@Param("protocol") String protocol);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select m from DonationMatch m where m.protocol = :protocol and m.donation.donor.id = :donorId")
    Optional<DonationMatch> findForUpdateForDonor(
            @Param("protocol") String protocol, @Param("donorId") Integer donorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select m from DonationMatch m where m.protocol = :protocol and m.necessity.institution.id = :institutionId")
    Optional<DonationMatch> findForUpdateForInstitution(
            @Param("protocol") String protocol, @Param("institutionId") Integer institutionId);

    @Modifying(flushAutomatically = true)
    @Query(
            value = "update matches set updated_at = clock_timestamp() where id = :matchId",
            nativeQuery = true)
    void recordFlowUpdate(@Param("matchId") Integer matchId);

    Optional<DonationMatch> findByProtocol(String protocol);

    @EntityGraph(attributePaths = {"donation", "necessity"})
    java.util.List<DonationMatch> findAllByDonationId(Integer donationId);

    boolean existsByDonationIdAndNecessityIdAndStatusIn(
            Integer donationId, Integer necessityId, Collection<MatchStatus> statuses);

    long countByNecessityInstitutionIdAndStatus(Integer institutionId, MatchStatus status);

    @Query(
            "select coalesce(sum(m.allocatedQuantity), 0) from DonationMatch m where m.donation.id = :donationId and m.status in :statuses")
    long sumAllocatedByDonation(
            @Param("donationId") Integer donationId,
            @Param("statuses") Collection<MatchStatus> statuses);

    @Query(
            "select coalesce(sum(m.allocatedQuantity), 0) from DonationMatch m where m.necessity.id = :necessityId and m.status in :statuses")
    long sumAllocatedByNecessity(
            @Param("necessityId") Integer necessityId,
            @Param("statuses") Collection<MatchStatus> statuses);

    Slice<DonationMatch> findByStatusAndAcceptanceDeadlineLessThanEqual(
            MatchStatus status, Instant deadline, Pageable pageable);

    @Query(
            """
        select m from DonationMatch m
        where m.status in :statuses and m.deliveryMethodDeadline <= :deadline
          and not exists (select d.id from Delivery d where d.match = m)
        """)
    Slice<DonationMatch> findExpiredMethodSelections(
            @Param("statuses") Collection<MatchStatus> statuses,
            @Param("deadline") Instant deadline,
            Pageable pageable);

    default Page<DonationMatch> findForInstitutionOrderedByDeadline(
            Integer institutionId, MatchStatus status, String searchPattern, Pageable pageable) {
        return findForInstitutionOrderedByDeadline(
                institutionId,
                status != null,
                status == null ? MatchStatus.AWAITING_ACCEPTANCE : status,
                searchPattern,
                MatchStatus.AWAITING_ACCEPTANCE,
                MatchStatus.AWAITING_DELIVERY,
                MatchStatus.AWAITING_SHIPMENT,
                pageable);
    }

    @EntityGraph(
            attributePaths = {
                "donation.donor",
                "donation.donationPackage.item.itemType",
                "necessity.institution",
                "necessity.itemType"
            })
    @Query(
            value =
                    """
        select m from DonationMatch m left join Delivery d on d.match = m
        where m.necessity.institution.id = :institutionId
          and (:filterByStatus = false or m.status = :status)
          and (:searchPattern is null or lower(m.protocol) like :searchPattern escape '\\'
            or lower(m.donation.donor.fullName) like :searchPattern escape '\\'
            or lower(m.donation.donationPackage.item.title) like :searchPattern escape '\\'
            or lower(m.necessity.itemType.name) like :searchPattern escape '\\')
        order by case
          when m.status = :awaitingAcceptance then m.acceptanceDeadline
          when m.status = :awaitingDelivery then d.inPersonDeadline
          when m.status = :awaitingShipment then coalesce(d.shippingDeadline, m.deliveryMethodDeadline)
          else null end asc nulls last, m.createdAt desc, m.id desc
        """,
            countQuery =
                    """
        select count(m) from DonationMatch m
        where m.necessity.institution.id = :institutionId
          and (:filterByStatus = false or m.status = :status)
          and (:searchPattern is null or lower(m.protocol) like :searchPattern escape '\\'
            or lower(m.donation.donor.fullName) like :searchPattern escape '\\'
            or lower(m.donation.donationPackage.item.title) like :searchPattern escape '\\'
            or lower(m.necessity.itemType.name) like :searchPattern escape '\\')
        """)
    Page<DonationMatch> findForInstitutionOrderedByDeadline(
            @Param("institutionId") Integer institutionId,
            @Param("filterByStatus") boolean filterByStatus,
            @Param("status") MatchStatus status,
            @Param("searchPattern") String searchPattern,
            @Param("awaitingAcceptance") MatchStatus awaitingAcceptance,
            @Param("awaitingDelivery") MatchStatus awaitingDelivery,
            @Param("awaitingShipment") MatchStatus awaitingShipment,
            Pageable pageable);
}
