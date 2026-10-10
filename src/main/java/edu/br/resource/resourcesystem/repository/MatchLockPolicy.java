package edu.br.resource.resourcesystem.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchLockPolicy {
    private final JdbcTemplate jdbc;

    @Value("${resource.match.lock-timeout-ms:3000}")
    private int timeoutMs;

    public void apply() {

        jdbc.queryForObject(
                "select set_config('lock_timeout', ?, true)",
                String.class,
                Math.max(100, Math.min(timeoutMs, 10000)) + "ms");
    }
}
