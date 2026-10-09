package edu.br.resource.resourcesystem.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchDeadlineRepository {
    private final JdbcTemplate jdbc;

    public List<String> findExpiredProtocols() {
        return jdbc.queryForList("""
            select m.protocol from matches m left join deliveries d on d.match_id=m.id
            where (m.status='awaiting_acceptance' and m.acceptance_deadline<=now())
               or (m.status in ('accepted','awaiting_shipment') and d.id is null and m.delivery_method_deadline<=now())
               or (m.status='awaiting_delivery' and d.in_person_deadline<=now())
               or (m.status='awaiting_shipment' and d.method='carrier' and d.shipping_deadline<=now())
            order by m.id limit 100
            """, String.class);
    }
}
