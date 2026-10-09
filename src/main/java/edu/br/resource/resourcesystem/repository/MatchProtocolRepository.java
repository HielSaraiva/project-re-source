package edu.br.resource.resourcesystem.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchProtocolRepository {
    private final JdbcTemplate jdbc;

    public long nextNumber() {
        return jdbc.queryForObject("select nextval('match_protocol_number')", Long.class);
    }
}
