package edu.br.resource.resourcesystem.service.donor;

import java.util.Map;

/** Identificadores estáveis das doações usadas nas telas de apresentação. */
final class MockDonationProtocols {
    static final String PENDING_ASSOCIACAO_VIDA = "INT-2026-201";
    static final String DELIVERY_INSTITUTO_ESPERANCA = "INT-2026-202";
    static final String COMPLETED_ONG_RECOMECO = "INT-2026-203";

    static final String REJECTED_ASSOCIACAO_VIDA = "INT-2026-204";
    static final String COMPLETED_CARRIER = "INT-2026-205";

    static final Map<String, String> EXPIRED_DELIVERIES = Map.of(
            "choice", "INT-2026-206", "in_person", "INT-2026-207", "carrier", "INT-2026-208");

    private MockDonationProtocols() {}
}
